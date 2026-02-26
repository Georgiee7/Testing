package me.george.testing.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.pokemon.types.t1.fire.Charmander;
import me.george.testing.parties.Party;

import java.util.UUID;

/**
 * A base entity with general traits an entity may have.
 * Created by George K. on 4/16/2024.
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Player.class, name = "player"),
        @JsonSubTypes.Type(value = Charmander.class, name = "charmander")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
public abstract class Entity {
    private final UUID uuid;

    @Setter
    private String name;

    @Setter(AccessLevel.PROTECTED)
    private boolean alive;

    private int health;

    @Setter(AccessLevel.PROTECTED)
    private int exp = 0;

    @Setter(AccessLevel.PROTECTED)
    private int level = 1;

    @JsonIgnore
    private Party party;

    protected Entity(String name) {
        this(UUID.randomUUID(), name);
    }

    protected Entity(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Whether the entity belongs to a party or not.
     *
     * @return isInParty
     */
    @JsonIgnore
    public boolean isInParty() {
        return this.party != null;
    }

    /**
     * Saves this entity's data.
     *
     * @return dataSaved
     */
    public abstract boolean save();

    /**
     * Sets this entity's health, killing it if below 0.
     *
     * @param amount The amount of health to set it at.
     */
    public void setHealth(int amount) {
        if (amount <= 0)
            kill();
        else
            this.health = amount;
    }

    /**
     * Kills the entity.
     */
    public void kill() {
        if (isAlive()) {
            this.health = 0;
            this.alive = false;
        }

        // TODO anything else
    }

    /**
     * Gets a one dimensional array of exp requirements per level-set.
     *
     * @return expRequirementArray
     */
    @JsonIgnore
    protected abstract int[] getCachedExpRequirement();

    /**
     * Gives exp to this entity, can proc level-up.
     * Uses a default formula, should be overridden if custom exp is desired.
     *
     * @param exp The exp to give.
     */
    public void addExp(int exp) {
        if (isMaxLevel() || exp <= 0)
            return;

        setExp(this.exp + exp);
        while (this.exp >= getNeededExp() && !(isMaxLevel()))
            levelUp();
    }

    /**
     * Calculates the needed exp to progress to a level.
     * Uses a default formula, should be overridden if custom exp is desired.
     *
     * @param level The level to use as a reference.
     * @return neededExp
     */
    @JsonIgnore
    protected abstract int getNeededExp(int level);

    /**
     * Makes this entity level up if below the max level.
     */
    public void levelUp() {
        setExp(getExp() - getNeededExp());
        setLevel(this.level + 1);
    }

    /**
     * Returns the amount of exp needed to level up.
     *
     * @return neededExp
     */
    @JsonIgnore
    public int getNeededExp() {
        return getNeededExp(getLevel());
    }

    /**
     * Whether this entity has reached max level or not.
     *
     * @return isMaxLevel
     */
    @JsonIgnore
    protected abstract boolean isMaxLevel();

    /**
     * Adds the entity to a party.
     *
     * @param party The party to add the entity to.
     */
    public void addToParty(Party party) {
        this.party = party;
    }

    /**
     * Removes the entity from their party.
     */
    public void removeFromParty() {
        this.party = null;
    }
}
