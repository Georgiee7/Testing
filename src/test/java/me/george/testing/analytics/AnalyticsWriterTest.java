package me.george.testing.analytics;

import me.george.testing.entity.pokemon.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests sending analytics rows in the background, when the sink works, is down, or refuses them.
 * Created by George K. on 11/27/2026.
 */
public class AnalyticsWriterTest {

    @Test
    public void testRowsAreSent() {
        List<String> received = new CopyOnWriteArrayList<>();
        AnalyticsWriter writer = new AnalyticsWriter((batchId, rows) -> received.addAll(rows), 100, 60_000); // Doesn't send on its own during the test.
        writer.offer("{\"row\":1}");
        writer.offer("{\"row\":2}");
        assertEquals(2, writer.getQueued());

        writer.stop(); // Sends what's queued.
        assertEquals(List.of("{\"row\":1}", "{\"row\":2}"), received);
        assertEquals(2, writer.getSent());
    }

    @Test
    public void testFullQueueDropsRows() {
        AnalyticsWriter writer = new AnalyticsWriter((batchId, rows) -> {}, 2, 60_000);
        for (int i = 0; i < 5; i++)
            writer.offer("{}"); // Never waits, even when full.

        assertEquals(2, writer.getQueued());
        assertEquals(3, writer.getDropped());

        writer.stop();
        assertEquals(2, writer.getSent());
    }

    @Test
    public void testUnreachableSinkIsTriedAgain() throws InterruptedException {
        List<String> batchIds = new CopyOnWriteArrayList<>();
        AtomicInteger tries = new AtomicInteger();
        AnalyticsWriter writer = new AnalyticsWriter((batchId, rows) -> {
            batchIds.add(batchId);
            if (tries.incrementAndGet() <= 2)
                throw new IOException("Down, on purpose.");
        }, 100, 10);

        writer.offer("{\"row\":1}");
        writer.offer("{\"row\":2}");
        waitFor(() -> writer.getSent() == 2);
        writer.stop();

        assertEquals(0, writer.getDropped());
        assertEquals(batchIds.get(0), batchIds.get(1)); // The batch that failed is the one tried again, with the same id.
        assertEquals(batchIds.get(0), batchIds.get(2));
    }

    @Test
    public void testRefusedBatchIsDropped() throws InterruptedException {
        AtomicInteger tries = new AtomicInteger();
        AnalyticsWriter writer = new AnalyticsWriter((batchId, rows) -> {
            tries.incrementAndGet();
            throw new IllegalArgumentException("Refused, on purpose.");
        }, 100, 10);

        writer.offer("{}");
        waitFor(() -> writer.getDropped() == 1);
        writer.stop();

        assertEquals(3, tries.get());
        assertEquals(0, writer.getSent());
    }

    @Test
    public void testRowJson() {
        String json = new AnalyticsRow("test")
                .with("number", 5)
                .with("text", "a \"quote\"")
                .with("element", Element.FIRE)
                .with("nothing", null)
                .toJson();

        assertTrue(json.startsWith("{\"event\":\"test\",\"time\":"));
        assertTrue(json.endsWith(",\"number\":5,\"text\":\"a \\\"quote\\\"\",\"element\":\"FIRE\",\"nothing\":null}"));
    }

    @Test
    public void testFileSink(@TempDir Path folder) throws IOException {
        Path file = folder.resolve("analytics").resolve("battles.jsonl");
        JsonLinesFileSink sink = new JsonLinesFileSink(file);
        sink.send("1", List.of("{\"row\":1}"));
        sink.send("2", List.of("{\"row\":2}"));
        assertEquals(List.of("{\"row\":1}", "{\"row\":2}"), Files.readAllLines(file));
    }

    private static void waitFor(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline)
            Thread.sleep(5);

        assertTrue(condition.getAsBoolean(), "Timed out waiting for the writer.");
    }
}
