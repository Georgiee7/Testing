package me.george.testing.data.storage.supported;

import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.manager.Manager;
import me.george.testing.Testing;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background persistence manager that periodically flushes in-memory caches to disk.
 * It's intentionally lightweight: runs every N seconds and invokes a save snapshot for players & pokemons.
 * Created by George K. on 2/26/2026.
 */
public class PersistenceManager extends Manager {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "persistence-manager");
        t.setDaemon(true);
        return t;
    });

    private static final int FLUSH_INTERVAL_SECONDS = 30;

    @Override
    public void onStart() {
        Testing.logInfo("Starting PersistenceManager (autosave every %ds)...", FLUSH_INTERVAL_SECONDS);
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                flushNow();
            } catch (Throwable th) {
                Testing.warn("PersistenceManager flush error: %s", th.getMessage());
            }
        }, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Flush current in-memory state of players and pokemons to disk immediately.
     * Can be used in tests to trigger persistence without waiting for the scheduled interval.
     */
    public static void flushNow() {
        PlayerManager.getPlayers().forEach(PlayerManager::save);
        PokemonManager.getPokemons().forEach(PokemonFileRepository::save);
    }

    @Override
    public void onStop() {
        Testing.logInfo("Stopping PersistenceManager...");
        try {
            SCHEDULER.shutdownNow();
            boolean terminated = SCHEDULER.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated)
                Testing.warn("PersistenceManager did not terminate within timeout, forcing shutdown.");
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
