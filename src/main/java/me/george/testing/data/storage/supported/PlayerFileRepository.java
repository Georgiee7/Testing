package me.george.testing.data.storage.supported;

import me.george.testing.entity.player.Player;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Simple file-backed repository for Player objects using JSON.
 * Created by George K. on 2/26/2026.
 */
public final class PlayerFileRepository extends AbstractFileRepository<Player> {
    private static final Path PLAYERS_DIR = Path.of("data", "json", "players");
    private static final PlayerFileRepository INSTANCE = new PlayerFileRepository();

    private PlayerFileRepository() {
        super(PLAYERS_DIR, Player.class);
    }

    public static PlayerFileRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the repository directory path used to store player JSON files.
     */
    public static Path getDir() {
        return PLAYERS_DIR;
    }

    @Override
    protected boolean isValid(Player player) {
        return player != null && player.getUuid() != null && player.getName() != null && !player.getName().isEmpty();
    }

    /**
     * Persist the given player to disk in the players directory.
     *
     * @param player player to persist
     */
    public static void save(Player player) {
        getInstance().save(player, player.getUuid().toString() + ".json");
    }

    /**
     * Loads and returns all valid player instances from disk.
     *
     * @return collection of players
     */
    public static Collection<Player> loadAll() {
        return getInstance().loadAllInternal();
    }

    /**
     * Removes a persisted player file from disk.
     *
     * @param player player to remove
     */
    public static void remove(Player player) {
        getInstance().remove(player.getUuid().toString() + ".json");
    }
}
