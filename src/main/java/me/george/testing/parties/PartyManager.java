package me.george.testing.parties;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
import me.george.testing.manager.Manager;
import me.george.testing.player.Player;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handles mechanics related to player owned parties.
 * Created by George K. on 4/15/2024.
 */
public class PartyManager extends Manager {

    private static final InMemoryCache<UUID, Party> PARTIES_BY_UUID = new InMemoryCache<>();

    @Override
    public void onStop() {
        Testing.logInfo("Unloading %d parties...", PARTIES_BY_UUID.size());
        getParties().forEach(PartyManager::disbandParty);
        PARTIES_BY_UUID.clear();
    }

    /**
     * Creates a new party of players, the leader of which is assigned at random.
     *
     * @param players The members of the party.
     * @return newParty
     */
    public static Party createParty(Collection<Player> players) {
        return createParty(players, Utils.randElement(players));
    }

    /**
     * Creates a new party of players with a specified leader.
     *
     * @param players The members of the party.
     * @param leader  The leader of the party.
     * @return newParty
     */
    public static Party createParty(Collection<Player> players, Player leader) {
        Utils.verifyNotNull(players, "Tried to create a party with null players.");
        Utils.verifyNotNull(leader, "Tried to create a party with a null leader.");

        return new Party(players, leader);
    }

    /**
     * Disbands a party, removing all player members.
     *
     * @param party The party to disband.
     */
    public static void disbandParty(Party party) {
        updateParty(party, p -> party.getPlayers().forEach(player -> removeFromParty(player, party)));
    }

    /**
     * Adds a player to a party.
     *
     * @param player The player to add to the party.
     * @param party  The party to add the player to.
     */
    public static void addToParty(Player player, Party party) {
        updateParty(party, p -> {
            if (p.isMember(player))
                return;

            p.addPlayer(player);
            player.addToParty(party);
        });
    }

    /**
     * Removes a player from a party.
     *
     * @param player The player to remove from the party.
     * @param party  The party to remove the player from.
     */
    public static void removeFromParty(Player player, Party party) {
        updateParty(party, p -> {
            if (!p.isMember(player))
                return;

            if (p.isLeader(player) && p.getSize() - 1 > 0)
                setLeader(p, Utils.randElement(p.getPlayers()));

            player.removeFromParty();
            p.removePlayer(player);
        });
    }

    /**
     * Assigns a player as the leader of their party, if they belong to one.
     *
     * @param party  The assumed party of the player.
     * @param player The player member of the party.
     */
    public static void setLeader(Party party, Player player) {
        updateParty(party, p -> {
            if (!party.isMember(player) || party.isLeader(player))
                return;

            p.setLeader(player);
        });
    }

    /**
     * Updates a party with a given action.
     *
     * @param party    The party to update.
     * @param callback The action to update it with.
     */
    public static void updateParty(Party party, Consumer<Party> callback) {
        Utils.verifyNotNull(party, "Tried to update a null party.");
        Utils.verifyNotNull(callback, "Tried to update a party with a null callback action.");

        callback.accept(party);
    }

    /**
     * Gets all active cached parties.
     *
     * @return activeCachedParties
     */
    public Collection<Party> getParties() {
        return PARTIES_BY_UUID.getValues();
    }
}
