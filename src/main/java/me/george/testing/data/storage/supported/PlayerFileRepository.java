package me.george.testing.data.storage.supported;

import me.george.testing.entity.player.Player;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Simple file-backed repository for {@link Player} objects using JSON.
 * <p>
 * The storage directory defaults to {@code data/json/players} but can be overridden
 * via the {@code data.dir} system property — set this in tests to a temp directory
 * so test data never touches the real application data.
 * </p>
 * Created by George K. on 2/26/2026.
 */
public final class PlayerFileRepository extends AbstractFileRepository<Player> {
    public static final String DATA_DIR_PROPERTY = "data.dir";
    private static final String DEFAULT_DATA_DIR = "data";

    private static final PlayerFileRepository INSTANCE = new PlayerFileRepository();

    private PlayerFileRepository() {
        super(resolveDir(), Player.class);
    }

    private static Path resolveDir() {
        return Path.of(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR), "json", "players");
    }

    public static PlayerFileRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the repository directory path used to store player JSON files.
     */
    public static Path getDir() {
        return getInstance().dir;
    }

    @Override
    protected boolean isValid(Player player) {
        return player != null && player.getUuid() != null
                && player.getName() != null && !player.getName().isEmpty();
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
