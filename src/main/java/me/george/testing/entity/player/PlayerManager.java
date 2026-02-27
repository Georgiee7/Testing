package me.george.testing.entity.player;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
import me.george.testing.data.storage.supported.PlayerFileRepository;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Handles functionality related to players.
 * Created by George K. on 4/15/2024.
 */
public class PlayerManager extends Manager {
    private static final InMemoryCache<UUID, Player> PLAYERS_BY_UUID = new InMemoryCache<>();

    private static final Pattern VALID_NAME_SYNTAX_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$"); // Underscores, 3 chars min, 16 chars max, alphanumeric english characters.

    public static final int MAX_LEVEL = 100;

    @Override
    public void onStart() {
        // Load persisted players from disk into memory on startup.
        Testing.logInfo("Loading players from disk...");
        var players = PlayerFileRepository.loadAll();
        players.forEach(p -> PLAYERS_BY_UUID.put(p.getUuid(), p));
        Testing.logInfo("Loaded %d players.", PLAYERS_BY_UUID.size());
    }

    @Override
    public void onStop() {
        int players = PLAYERS_BY_UUID.size();
        Testing.logInfo("Unloading %d players...", players);
        getPlayers().forEach(PlayerManager::save);
        PLAYERS_BY_UUID.clear();
        Testing.logInfo("Cleared %d players.", players);
    }

    /**
     * Creates and returns a new player.
     *
     * @param name The name of the player.
     * @return newPlayer
     */
    public static Player create(String name) {
        Utils.verify(isValidPlayerName(name), "Tried to create a player with an invalid name: %s", name);

        Player player = new Player(name);
        PLAYERS_BY_UUID.put(player.getUuid(), player);
        save(player);
        return player;
    }

    /**
     * Updates a player with a given action and persists the changes.
     *
     * @param player   The player to update (must not be null).
     * @param callback The callback action that mutates the player (must not be null).
     */
    public static void updatePlayer(Player player, Consumer<Player> callback) {
        Utils.verifyNotNull(player, "Tried to modify a null player.");
        Utils.verifyNotNull(callback, "Tried to modify a player with a null callback.");

        callback.accept(player);
        save(player);
    }

    /**
     * Deletes all data related to a player from the persistence layer and removes it from cache.
     *
     * @param player The player to erase all data of.
     * @return true when deletion was executed (note: this method currently always returns true).
     */
    public static boolean deleteData(Player player) {
        // Remove any storage data and remove from cache.
        PlayerFileRepository.remove(player);
        remove(player);
        return true;
    }

    /**
     * Changes the player's name to a new valid name and persists the change.
     *
     * @param player  The player to set the name of.
     * @param newName The new name to set.
     * @return true on success
     */
    public static boolean setName(Player player, String newName) {
        updatePlayer(player, p -> {
            Utils.verify(isValidPlayerName(newName), "Tried to set %s's name to an invalid name: '%s'", player.getName(), newName);
            player.setName(newName);
        });

        return true;
    }

    /**
     * Saves a player's data to the configured persistence layer and updates the in-memory cache.
     *
     * @param player The player to save data for.
     * @return true when saved
     */
    public static boolean save(Player player) {
        if (player == null)
            return false;

        PLAYERS_BY_UUID.put(player.getUuid(), player);
        PlayerFileRepository.save(player);
        return true;
    }

    private static void remove(Player player) {
        PLAYERS_BY_UUID.remove(player.getUuid());
    }

    /**
     * Gets all active cached players.
     *
     * @return activeCachedPlayers
     */
    public static Collection<Player> getPlayers() {
        return PLAYERS_BY_UUID.getValues();
    }

    /**
     * Clears the in-memory player cache without deleting persisted files.
     * Intended for test isolation — use {@link #deleteData(Player)} to fully delete.
     */
    public static void clearCache() {
        PLAYERS_BY_UUID.clear();
    }

    /**
     * Gets a player by their id, or null if one does not exist.
     *
     * @param id The id to search for.
     * @return playerById
     */
    public static Player getPlayer(UUID id) {
        return PLAYERS_BY_UUID.get(id);
    }

    /**
     * Removes a player by their id from the runtime cache (does not delete persisted data).
     *
     * @param id The id to search for.
     */
    public static void removePlayer(UUID id) {
        PLAYERS_BY_UUID.remove(id);
    }

    /**
     * Checks whether a string is a valid player name.
     *
     * @param string The string to check.
     * @return isValidPlayerName
     */
    public static boolean isValidPlayerName(String string) {
        return !Utils.isNullOrEmpty(string) && VALID_NAME_SYNTAX_PATTERN.matcher(string).matches();
    }
}
