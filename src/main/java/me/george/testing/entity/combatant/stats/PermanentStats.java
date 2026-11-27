package me.george.testing.entity.combatant.stats;

import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around permanent stats a combatant may have.
 * Created by George K. on 4/13/2024.
 */
@Getter
public class PermanentStats {
    public static final int MIN_BASE_STAT = 5;
    public static final int MAX_BASE_STAT = 255;

    // Calculated stats go well past the base stat cap, a level 100 Pokémon with 255 base health has over 700 health.
    public static final int MIN_STAT = 1;
    public static final int MAX_STAT = 999;

    public static final int MIN_EV_AMOUNT = 0;
    public static final int MAX_EV_AMOUNT_PER_STAT = 252;
    public static final int MAX_EV_AMOUNT_PER_POKEMON = 510;

    public static final int MIN_IV_ROLL = 0;
    public static final int MAX_IV_ROLL = 31;

    private final Map<PermanentStat, Integer> stats;
    private boolean initialized;

    private StatsCalculator calculator = new DefaultStatsCalculator();

    public PermanentStats(Map<PermanentStat, Integer> stats) {
        if (stats instanceof EnumMap) {
            this.stats = new EnumMap<>((EnumMap<PermanentStat, Integer>) stats);
        } else if (stats != null) {
            this.stats = new EnumMap<>(PermanentStat.class);
            this.stats.putAll(stats);
        } else {
            this.stats = new EnumMap<>(PermanentStat.class);
        }
        this.initialized = !this.stats.isEmpty();
    }

    public PermanentStats() {
        this(new HashMap<>());
    }

    /**
     * Gets a specific stat's value
     *
     * @param stat The stat to get the value of.
     * @return statValue
     */
    public int getStat(PermanentStat stat) {
        return this.stats.getOrDefault(stat, MIN_STAT);
    }

    /**
     * Sets a stat to a specific value.
     *
     * @param stat  The stat to update.
     * @param value The value to update it with.
     */
    public void setStat(PermanentStat stat, int value) {
        this.stats.put(stat, clampStat(value));
    }

    /**
     * Increases a specific stat by a value, up to its maximum.
     *
     * @param stat  The stat to increase.
     * @param value The value to increase it by.
     */
    public void increaseStat(PermanentStat stat, int value) {
        setStat(stat, getStat(stat) + value);
    }

    /**
     * Decreases a specific stat by a value, down to its minimum.
     *
     * @param stat  The stat to decrease.
     * @param value The value to decrease it by.
     */
    public void decreaseStat(PermanentStat stat, int value) {
        setStat(stat, getStat(stat) - value);
    }

    /**
     * Calculate stats using the configured calculator.
     *
     * @param baseStats Base stats to use in the formula.
     * @param ivs       The IVs per stat to use in the formula.
     * @param evs       The EVs per stat to use in the formula.
     * @param level     The level to be used to calculate the bonus.
     */
    public void calculateStats(Map<PermanentStat, Integer> baseStats, Map<PermanentStat, Integer> ivs, Map<PermanentStat, Integer> evs, int level) {
        this.calculator.calculate(this, baseStats, ivs, evs, level);
    }

    /**
     * Returns an unmodifiable snapshot of the current stats map.
     */
    public Map<PermanentStat, Integer> asMap() {
        return Collections.unmodifiableMap(new EnumMap<>((EnumMap<PermanentStat, Integer>) this.stats));
    }

    /**
     * Replace stats from a provided map, clamping values into allowed range.
     */
    public void fromMap(Map<PermanentStat, Integer> map) {
        this.stats.clear();
        for (PermanentStat s : PermanentStat.values())
            this.stats.put(s, clampStat(map.getOrDefault(s, MIN_STAT)));

        this.initialized = !this.stats.isEmpty();
    }

    /**
     * Return a shallow copy of this PermanentStats.
     */
    public PermanentStats copy() {
        return new PermanentStats(new EnumMap<>((EnumMap<PermanentStat, Integer>) this.stats));
    }

    /**
     * Set the stats calculator to another implementation.
     */
    public void setCalculator(StatsCalculator calc) {
        this.calculator = calc != null ? calc : new DefaultStatsCalculator();
    }

    void statsPut(PermanentStat stat, int value) {
        this.stats.put(stat, value);
    }

    void setInitialized(boolean value) {
        this.initialized = value;
    }

    private static int clampStat(int value) {
        return Math.max(MIN_STAT, Math.min(MAX_STAT, value));
    }
}
