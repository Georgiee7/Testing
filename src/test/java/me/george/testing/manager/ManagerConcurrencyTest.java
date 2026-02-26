package me.george.testing.manager;

import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests concurrent access to the Manager class to ensure thread safety and absence of structural issues.
 * Created by George K. on 2/26/2026.
 */
public class ManagerConcurrencyTest {

    @Test
    public void stressAddManagersConcurrently() throws InterruptedException {
        int threads = 8;
        int iterations = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        try {
                            Manager.addManager(Manager.class); // attempting to add same manager concurrently may throw GeneralException - that's acceptable, we just want to ensure no structural corruption.
                        } catch (Throwable th) {
                            // We tolerate exceptions here but capture them for debugging.
                            errors.add(th);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdownNow();

        // We assert only that no structural exceptions occurred (e.g., ConcurrentModificationException). Other exceptions (like duplicate registration) are acceptable.
        boolean structuralIssue = errors.stream().anyMatch(e -> e instanceof ConcurrentModificationException);
        assertFalse(structuralIssue, "Structural concurrent modification detected: " + (errors.peek() != null ? errors.peek().toString() : "none"));
    }
}
