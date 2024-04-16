package me.george.testing.parties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.Entity;

import java.util.Collection;
import java.util.List;

/**
 * Represents a team/party that consists of players, which have their own teams of Pokémon.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Party {

    private final List<Entity> entities;

    @Setter(AccessLevel.PROTECTED)
    private Entity leader;

    protected Party(Collection<Entity> entities, Entity leader) {
        this.entities = (List<Entity>) entities;
        this.leader = leader;
    }

    /**
     * Adds an entity to the party.
     *
     * @param entity The entity to add.
     */
    protected void addEntity(Entity entity) {
        this.entities.add(entity);
    }

    /**
     * Removes an entity from the party.
     *
     * @param entity The entity to remove.
     */
    protected void removePlayer(Entity entity) {
        this.entities.remove(entity);
    }

    /**
     * Whether an entity is a member of the party or not.
     *
     * @param entity The entity in question.
     * @return isPartyMember
     */
    public boolean isMember(Entity entity) {
        return this.entities.contains(entity);
    }

    /**
     * Whether a entity is the leader of the party.
     *
     * @param entity The entity in question.
     * @return isPartyLeader
     */
    protected boolean isLeader(Entity entity) {
        return isMember(entity) && this.leader == entity;
    }

    /**
     * Gets the amount of members in the party.
     *
     * @return memberCount
     */
    protected int getSize() {
        return getEntities().size();
    }
}
