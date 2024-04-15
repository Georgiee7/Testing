package me.george.testing.player;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
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

    private static final Pattern VALID_NAME_SYNTAX_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,12}$"); // Underscores, 3 chars min, 12 chars max, alphanumeric english characters.

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onStop() {
        Testing.logInfo("Unloading %d players...", PLAYERS_BY_UUID.size());
        getPlayers().forEach(PlayerManager::save);
        PLAYERS_BY_UUID.clear();
    }

    /**
     * Creates and returns a new player.
     *
     * @param name The type of Pokémon to create.
     * @return newPlayer
     */
    public static Player create(String name) {
        Utils.verify(!Utils.isNullOrEmpty(name), "Tried to create a player with an invalid name: %s", name);

        Player player = new Player(name);
        PLAYERS_BY_UUID.put(player.getUuid(), player);
        return player;
    }

    /**
     * Updates a player with a given action.
     *
     * @param player   The player to update.
     * @param callback The callback action.
     */
    public static void updatePlayer(Player player, Consumer<Player> callback) {
        Utils.verifyNotNull(player, "Tried to modify a null player.");
        Utils.verifyNotNull(callback, "Tried to modify a player with a null callback.");

        callback.accept(player);
    }

    /**
     * Deletes all data related to a player.
     *
     * @param player The player to erase all data of.
     */
    public static void deleteData(Player player) {
        // Remove any storage data and remove from cache.
        remove(player);
    }

    /**
     * Changes the player's name to a new valid name.
     *
     * @param player  The player to set the name of.
     * @param newName The name to set.
     * @return nameSetSuccessfully
     */
    public static boolean setName(Player player, String newName) {
        updatePlayer(player, p -> {
            Utils.verify(isValidPlayerName(newName), "Tried to set %s's name to an invalid name: '%s'", player.getName(), newName);
            player.setName(newName);
        });

        return true;
    }

    /**
     * Saves a player's data.
     *
     * @param player The player to save data for.
     * @return dataSaved
     */
    public static boolean save(Player player) {
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
     * Checks whether a string is a valid player name.
     *
     * @param string The string to check.
     * @return isValidPlayerName
     */
    public static boolean isValidPlayerName(String string) {
        return !Utils.isNullOrEmpty(string) && VALID_NAME_SYNTAX_PATTERN.matcher(string).matches();
    }
}
