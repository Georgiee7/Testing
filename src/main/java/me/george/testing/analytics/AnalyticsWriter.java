package me.george.testing.analytics;

import me.george.testing.Testing;
import me.george.testing.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sends analytics rows to a sink. Rows wait in a bounded queue and one background thread sends them every few seconds as a batch,
 * so battles only ever pay for adding a row to the queue. When the sink is slow or down nothing waits on it: the batch is tried
 * again with a growing delay, rows keep queueing up to the limit, and past it new rows are dropped and counted. An outage then
 * costs a fixed amount of memory, and loses only the rows that didn't fit.
 * Created by George K. on 11/27/2026.
 */
public class AnalyticsWriter {
    public static final int DEFAULT_QUEUE_LIMIT = 50_000;
    public static final long DEFAULT_FLUSH_MILLIS = 5_000;

    private static final int BATCH_LIMIT = 1_000;
    private static final long MAX_RETRY_MILLIS = 60_000;
    private static final int MAX_REJECTIONS = 3; // The sink refused a batch this many times, so it's dropped.
    private static final long STOP_WAIT_MILLIS = 3_000;

    private final AnalyticsSink sink;
    private final int queueLimit;
    private final long flushMillis;
    private final ArrayBlockingQueue<String> queue;
    private final ScheduledExecutorService thread;
    private final AtomicLong dropped = new AtomicLong();
    private volatile long sent; // Only the writer thread changes it, other threads just read it.

    // Only the writer thread uses these.
    private final List<String> batch = new ArrayList<>();
    private String batchId; // The same on every try of a batch, so the sink can skip one it already saved.
    private int failures;
    private int rejections;
    private long nextAttempt;
    private boolean reachable = true;

    public AnalyticsWriter(AnalyticsSink sink) {
        this(sink, DEFAULT_QUEUE_LIMIT, DEFAULT_FLUSH_MILLIS);
    }

    public AnalyticsWriter(AnalyticsSink sink, int queueLimit, long flushMillis) {
        Utils.verifyNotNull(sink, "Tried to make an analytics writer with no sink.");
        Utils.verify(queueLimit > 0 && flushMillis > 0, "An analytics writer needs a positive queue limit and flush time.");
        this.sink = sink;
        this.queueLimit = queueLimit;
        this.flushMillis = flushMillis;
        this.queue = new ArrayBlockingQueue<>(queueLimit);
        this.thread = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "Testing Analytics");
            thread.setDaemon(true);
            return thread;
        });

        this.thread.scheduleWithFixedDelay(() -> flushSafely(false), flushMillis, flushMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Queues a row to be sent. Never blocks: if the queue is full, the row is dropped and counted.
     *
     * @param row The row, a JSON object.
     */
    public void offer(String row) {
        if (!this.queue.offer(row))
            this.dropped.incrementAndGet();
    }

    /**
     * Gets how many rows are waiting to be sent.
     *
     * @return queued
     */
    public int getQueued() {
        return this.queue.size();
    }

    /**
     * Gets how many rows were sent since the writer started.
     *
     * @return sent
     */
    public long getSent() {
        return this.sent;
    }

    /**
     * Gets how many rows were dropped since the writer started, because the queue was full or the sink refused them.
     *
     * @return dropped
     */
    public long getDropped() {
        return this.dropped.get();
    }

    /**
     * Sends everything queued, with one last try of a few seconds at most, and stops the writer thread.
     */
    public void stop() {
        Future<?> lastFlush = this.thread.submit(() -> {
            this.nextAttempt = 0; // Even if it's waiting to try again.
            flushSafely(true);
        });
        this.thread.shutdown();

        String reason = "the sink couldn't be reached";
        try {
            lastFlush.get(STOP_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            reason = "interrupted";
        } catch (Exception exception) {
            reason = Utils.getSimpleName(exception);
        }

        int unsent = getQueued() + this.batch.size(); // Safe to read once the last flush is done or given up on.
        if (unsent > 0)
            Testing.warn("Analytics stopped without sending %d rows (%s).", unsent, reason);

        this.thread.shutdownNow();
    }

    private void flushSafely(boolean everything) {
        try {
            boolean sentBatch = flush();
            while (sentBatch && (everything ? !this.queue.isEmpty() : this.queue.size() >= BATCH_LIMIT))
                sentBatch = flush(); // A full batch waiting means rows come in faster than they're sent, so keep sending.
        } catch (Throwable throwable) { // Never let an error end the scheduled task, it wouldn't run again.
            this.dropped.addAndGet(this.batch.size());
            this.batch.clear();
            onFailure(throwable.toString());
        }
    }

    // Sends the batch waiting to be tried again, or a new one from the queue. Returns whether a batch was sent.
    private boolean flush() {
        if (this.batch.isEmpty()) { // A batch that failed stays here, to be tried again first.
            this.queue.drainTo(this.batch, BATCH_LIMIT);
            this.batchId = UUID.randomUUID().toString();
        }

        if (this.batch.isEmpty() || System.currentTimeMillis() < this.nextAttempt)
            return false;

        try {
            this.sink.send(this.batchId, List.copyOf(this.batch));
        } catch (IOException exception) { // Down or unreachable: keep the batch, and wait longer each time.
            onFailure(exception.toString());
            return false;
        } catch (Exception exception) { // Reached, but it refused the batch. One refused a few times in a row is dropped, so a bad row can't hold up the rest.
            if (++this.rejections >= MAX_REJECTIONS) {
                Testing.warn("Analytics: the sink refused %d rows %d times, dropping them (%s).", this.batch.size(), this.rejections, exception);
                this.dropped.addAndGet(this.batch.size());
                this.batch.clear();
                this.rejections = 0;
            }

            onFailure(exception.toString());
            return false;
        }

        this.sent += this.batch.size();
        this.batch.clear();
        this.failures = 0;
        this.rejections = 0;
        this.nextAttempt = 0;
        if (!this.reachable) {
            this.reachable = true;
            Testing.logInfo("Analytics: sending rows again (%d dropped so far).", getDropped());
        }

        return true;
    }

    private void onFailure(String reason) {
        this.failures++;
        this.nextAttempt = System.currentTimeMillis() + Math.min(MAX_RETRY_MILLIS, this.flushMillis << Math.min(this.failures - 1, 4));
        if (this.reachable) { // Logged once per outage, not on every try.
            this.reachable = false;
            Testing.warn("Analytics can't send rows (%s). Battles aren't affected, keeping up to %d rows and trying again.", reason, this.queueLimit);
        }
    }
}
