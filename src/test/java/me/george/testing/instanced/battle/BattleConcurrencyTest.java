package me.george.testing.instanced.battle;

import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency stress tests for battle creation/management.
 * Created by George K. on 2/26/2026.
 */
public class BattleConcurrencyTest {

    @Test
    public void stressCreateAndStopBattlesConcurrently() throws InterruptedException {
        final int threads = 8;
        final int iterationsPerThread = 200;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        try {
                            // Create two players and parties for each iteration.
                            Player p1 = PlayerManager.create("T1_" + Math.abs(ThreadLocalRandom.current().nextInt()));
                            Player p2 = PlayerManager.create("T2_" + Math.abs(ThreadLocalRandom.current().nextInt()));

                            Party party1 = PartyManager.createParty(List.of(p1), null, p1);
                            Party party2 = PartyManager.createParty(List.of(p2), null, p2);

                            Battle b = BattleManager.createBattle(Arrays.asList(party1, party2));
                            if (ThreadLocalRandom.current().nextBoolean())
                                BattleManager.start(b);

                            if (ThreadLocalRandom.current().nextBoolean())
                                BattleManager.stop(b);
                        } catch (Throwable th) {
                            StringWriter sw = new StringWriter();
                            th.printStackTrace(new PrintWriter(sw));
                            errors.add(sw.toString());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdownNow();

        assertTrue(errors.isEmpty(), "Concurrent execution produced exceptions; first error:\n" + (errors.peek() != null ? errors.peek() : "none"));
    }
}
