package me.george.testing.entity.combatant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.Entity;
import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.utils.Utils;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents an entity that may participate in combat, being a part of Parties.
 * Created by George K. on 4/16/2024.
 */
@Getter
public abstract class Combatant extends Entity {

    private final PermanentStats permanentStats;

    private final Map<PermanentStat, Integer> ivs;

    @Setter(AccessLevel.PROTECTED)
    private Map<PermanentStat, Integer> evs;

    protected Combatant(String name, PermanentStats permanentStats) {
        super(name);
        this.ivs = rollIVs();
        this.evs = new EnumMap<>(PermanentStat.class);
        this.permanentStats = permanentStats == null ? new PermanentStats() : permanentStats;
        setAlive(true);
    }

    /**
     * Calculates this combatant's stats (unless it was given them) and fully heals it.
     * Subclasses must call this once they can provide their base stats, calling it from this constructor would run before their fields are set.
     */
    protected void initializeStats() {
        if (!this.permanentStats.isInitialized())
            calculateStats();

        heal();
    }

    /**
     * Re-calculates this combatant's stats from its base stats, IVs, EVs and level.
     */
    protected void calculateStats() {
        this.permanentStats.calculateStats(getBaseStats(), this.ivs, this.evs, getLevel());
        if (getHealth() > getHP())
            setHealth(getHP());
    }

    /**
     * Gets this combatant's base stats from its type.
     *
     * @return baseStats
     */
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

        // Ensure neither the stat's EVs nor the total EVs exceed the maximum allowed per combatant.
        int statEVs = this.evs.getOrDefault(stat, 0);
        int addedEVs = Math.min(amount, Math.min(PermanentStats.MAX_EV_AMOUNT_PER_STAT - statEVs, PermanentStats.MAX_EV_AMOUNT_PER_POKEMON - getTotalEVs()));
        if (addedEVs <= 0)
            return;

        this.evs.put(stat, statEVs + addedEVs);
        calculateStats();
    }

    private int getTotalEVs() {
        return this.evs.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Sets this combatant's health, up to its health stat. Killing it if at or below 0.
     *
     * @param amount The amount of health to set it at.
     */
    @Override
    public void setHealth(int amount) {
        super.setHealth(Math.min(amount, getHP()));
    }

    /**
     * Fully heals this combatant.
     */
    public void heal() {
        setHealth(getHP());
    }

    /**
     * Gets this combatant's health stat, the most health it can have.
     *
     * @return hp
     */
    public int getHP() {
        return getStat(PermanentStat.HP);
    }

    /**
     * Decreases the combatant's health by an amount, may kill it.
     *
     * @param amount The amount to decrease it by.
     */
    public void decreaseHP(int amount) {
        if (amount <= 0)
            return;

        setHealth(getHealth() - amount);
    }

    /**
     * Sets the combatant's health stat to an amount.
     *
     * @param amount The amount to set it to.
     */
    public void setHP(int amount) {
        this.permanentStats.setStat(PermanentStat.HP, amount);
        if (getHealth() > getHP())
            setHealth(getHP());
    }

    /**
     * Gets this combatant's attack.
     *
     * @return attack
     */
    public int getAttack() {
        return getStat(PermanentStat.ATK);
    }

    /**
     * Gets this combatant's special attack.
     *
     * @return specialAttack
     */
    public int getSpAttack() {
        return getStat(PermanentStat.SP_ATK);
    }

    /**
     * Gets this combatant's defense.
     *
     * @return defense
     */
    public int getDefense() {
        return getStat(PermanentStat.DEF);
    }

    /**
     * Gets this combatant's special defense.
     *
     * @return specialDefense
     */
    public int getSpDefense() {
        return getStat(PermanentStat.SP_DEF);
    }

    /**
     * Gets this combatant's speed.
     *
     * @return speed
     */
    public int getSpeed() {
        return getStat(PermanentStat.SPEED);
    }

    private int getStat(PermanentStat permanentStat) {
        return this.permanentStats.getStat(permanentStat);
    }

    /**
     * Rolls a combatant's Individual Values (IVs), they never change after.
     *
     * @return ivs
     */
    private static Map<PermanentStat, Integer> rollIVs() {
        Map<PermanentStat, Integer> ivs = new EnumMap<>(PermanentStat.class);
        for (PermanentStat stat : PermanentStat.values())
            ivs.put(stat, Utils.randInt(PermanentStats.MIN_IV_ROLL, PermanentStats.MAX_IV_ROLL));

        return ivs;
    }
}
