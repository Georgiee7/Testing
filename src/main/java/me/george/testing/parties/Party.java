package me.george.testing.parties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a team/party that consists of players, which have their own teams of Pokémon.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Party {

    private final List<Entity> members;

    @Setter(AccessLevel.PROTECTED)
    private Entity leader;

    protected Party(Collection<Entity> members, Collection<Combatant> combatants, Entity leader) {
        this.members = (List<Entity>) members;
        if (combatants == null)
            combatants = new ArrayList<>();

        this.members.addAll(combatants); // Wrapping the collection with an arraylist to prevent UnsupportedOperationException
        this.leader = leader;
    }

    /**
     * Whether the party is empty or not.
     *
     * @return partyIsEmpty
     */
    public boolean isEmpty() {
        return this.members.isEmpty();
    }

    /**
     * Adds an entity to the party.
     *
     * @param entity The entity to add.
     */
    protected void addEntity(Entity entity) {
        this.members.add(entity);
    }

    /**
     * Removes an entity from the party.
     *
     * @param entity The entity to remove.
     */
    protected void remove(Entity entity) {
        this.members.remove(entity);
    }

    /**
     * Whether an entity is a member of the party or not.
     *
     * @param entity The entity in question.
     * @return isPartyMember
     */
    public boolean isMember(Entity entity) {
        return this.members.contains(entity);
    }

    /**
     * Whether a member of this party is combatant.
     *
     * @param entity The entity in question.
     * @return isCombatantEntity
     */
    public boolean isCombatant(Entity entity) {
        return isMember(entity) && entity instanceof Combatant;
    }

    /**
     * Whether an entity is the leader of the party.
     *
     * @param entity The entity in question.
     * @return isPartyLeader
     */
    public boolean isLeader(Entity entity) {
        return isMember(entity) && this.leader == entity;
    }

    /**
     * Gets the amount of members in the party.
     *
     * @return memberCount
     */
    public int getSize() {
        return getMembers().size();
    }

    /**
     * Gets the combatant members of this party.
     *
     * @return combatantMembers
     */
    public List<Entity> getCombatants() {
        return this.members.stream().filter(Combatant.class::isInstance).toList();
    }

    /**
     * Gets the non-combatant members of this party.
     *
     * @return nonCombatantMembers
     */
    public List<Entity> getNonCombatants() {
        return this.members.stream().filter(combatant -> !(combatant instanceof Combatant)).toList();
    }
}
