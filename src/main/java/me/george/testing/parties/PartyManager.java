package me.george.testing.parties;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
import me.george.testing.entity.Combatant;
import me.george.testing.entity.Entity;
import me.george.testing.manager.Manager;
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
        int parties = PARTIES_BY_UUID.size();
        Testing.logInfo("Unloading %d parties...", parties);
        getParties().forEach(PartyManager::disbandParty);
        PARTIES_BY_UUID.clear();
        Testing.logInfo("Cleared %d parties.", parties);
    }

    /**
     * Creates a new party of entities, the leader of which is assigned at random.
     *
     * @param entities The members of the party.
     * @return newParty
     */
    public static Party createParty(Collection<Entity> entities, Collection<Combatant> combatants) {
        return createParty(entities, combatants, Utils.randElement(entities));
    }

    /**
     * Creates a new party of entities with a specified leader.
     *
     * @param entities The members of the party.
     * @param leader  The leader of the party.
     * @return newParty
     */
    public static Party createParty(Collection<Entity> entities, Collection<Combatant> combatants, Entity leader) {
        Utils.verifyNotNull(entities, "Tried to create a party with null entities.");
        Utils.verifyNotNull(leader, "Tried to create a party with a null leader.");
        Utils.verify(!leader.isInParty() && entities.stream().noneMatch(Entity::isInParty), "Tried to create a party when the entities and/or the leader already belong to one.");

        return new Party(entities, combatants, leader);
    }

    /**
     * Disbands a party, removing all entity members.
     *
     * @param party The party to disband.
     */
    public static void disbandParty(Party party) {
        updateParty(party, p -> party.getMembers().forEach(entity -> removeFromParty(entity, party)));
    }

    /**
     * Adds an entity to a party.
     *
     * @param entity The entity to add to the party.
     * @param party  The party to add the entity to.
     */
    public static void addToParty(Entity entity, Party party) {
        updateParty(party, p -> {
            if (p.isMember(entity))
                return;

            p.addEntity(entity);
            entity.addToParty(party);
        });
    }

    /**
     * Removes an entity from a party.
     *
     * @param entity The entity to remove from the party.
     * @param party  The party to remove the entity from.
     */
    public static void removeFromParty(Entity entity, Party party) {
        updateParty(party, p -> {
            if (!p.isMember(entity))
                return;

            if (p.isLeader(entity) && p.getSize() - 1 > 0)
                setLeader(p, Utils.randElement(p.getMembers()));

            entity.removeFromParty();
            p.removePlayer(entity);
            if (p.isEmpty())
                disbandParty(p);
        });
    }

    /**
     * Assigns an entity as the leader of their party, if they belong to one.
     *
     * @param party  The assumed party of the entity.
     * @param entity The entity member of the party.
     */
    public static void setLeader(Party party, Entity entity) {
        updateParty(party, p -> {
            if (!party.isMember(entity) || party.isLeader(entity))
                return;

            p.setLeader(entity);
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
