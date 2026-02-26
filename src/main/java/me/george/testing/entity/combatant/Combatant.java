package me.george.testing.entity.combatant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.Entity;
import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.combatant.stats.PermanentStats;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an entity that may participate in combat, being a part of Parties.
 * Created by George K. on 4/16/2024.
 */
@Getter
public abstract class Combatant extends Entity {
    private final PermanentStats permanentStats;

    @Setter(AccessLevel.PROTECTED)
    private Map<PermanentStat, Integer> evs;

    protected Combatant(String name, PermanentStats permanentStats) {
        super(name);
        this.evs = new HashMap<>();
        this.permanentStats = permanentStats == null ? new PermanentStats() : permanentStats;
        setAlive(true);
        if (!this.permanentStats.isInitialized())
            calculateStats();
    }

    /**
     * Re-calculates this combatant's stats.
     */
    protected void calculateStats() {
        this.permanentStats.calculateStats(this.evs, this.permanentStats.getStats(), getLevel());
    }

    /**
     * Gets this combatant's base stats from its type.
     *
     * @return baseStats
     */
    @JsonIgnore
    public abstract Map<PermanentStat, Integer> getBaseStats();

    /**
     * Adds Effort Values (EVs) to a specific stat of the combatant.
     *
     * @param stat   The stat to which EVs should be added.
     * @param amount The amount of EVs to add. Must be a positive integer.
     */
    public void addEv(PermanentStat stat, int amount) {
        if (amount <= PermanentStats.MIN_EV_AMOUNT)
            return;

        // Ensure the total EVs do not exceed the maximum allowed per combatant.
        int remainingEVs = Math.min(PermanentStats.MAX_EV_AMOUNT_PER_POKEMON - getTotalEVs(), amount);
        this.evs.put(stat, this.evs.getOrDefault(stat, 0) + remainingEVs);
        calculateStats();
    }

    @JsonIgnore
    private int getTotalEVs() {
        return this.evs.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Gets this combatant's health stat.
     *
     * @return hp
     */
    @JsonIgnore
    public int getHP() {
        return getStat(PermanentStat.HP);
    }

    /**
     * Decreases the combatant's HP by an amount, may kill it.
     *
     * @param amount The amount to decrease it by.
     */
    public void decreaseHP(int amount) {
        this.permanentStats.decreaseStat(PermanentStat.HP, amount);
        if (getHealth() <= 0)
            kill();
    }

    /**
     * Sets the combatant's hp to an amount.
     *
     * @param amount The amount to set it to.
     */
    public void setHP(int amount) {
        this.permanentStats.setStat(PermanentStat.HP, amount);
    }

    /**
     * Gets this combatant's attack.
     *
     * @return attack
     */
    @JsonIgnore
    public int getAttack() {
        return getStat(PermanentStat.ATK);
    }

    /**
     * Gets this combatant's special attack.
     *
     * @return specialAttack
     */
    @JsonIgnore
    public int getSpAttack() {
        return getStat(PermanentStat.SP_ATK);
    }

    /**
     * Gets this combatant's defense.
     *
     * @return defense
     */
    @JsonIgnore
    public int getDefense() {
        return getStat(PermanentStat.DEF);
    }

    /**
     * Gets this combatant's special defense.
     *
     * @return specialDefense
     */
    @JsonIgnore
    public int getSpDefense() {
        return getStat(PermanentStat.SP_DEF);
    }

    /**
     * Gets this combatant's speed.
     *
     * @return speed
     */
    @JsonIgnore
    public int getSpeed() {
        return getStat(PermanentStat.SPEED);
    }

    private int getStat(PermanentStat permanentStat) {
        return this.permanentStats.getStat(permanentStat);
    }
}
