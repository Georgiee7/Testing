package me.george.testing.entity.combatant.stats;

import lombok.Getter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A wrapper around permanent stats a combatant may have.
 * Created by George K. on 4/13/2024.
 */
@Getter
public class PermanentStats {
    public static final int MIN_BASE_STAT = 5;
    public static final int MAX_BASE_STAT = 255;

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
        return this.stats.getOrDefault(stat, MIN_BASE_STAT);
    }

    /**
     * Sets a stat to a specific value.
     *
     * @param stat  The stat to update.
     * @param value The value to update it with.
     */
    public void setStat(PermanentStat stat, int value) {
        int newValue = Math.min(value, MAX_BASE_STAT);
        this.stats.put(stat, Math.max(newValue, MIN_BASE_STAT));
    }

    /**
     * Increases a specific stat by a value, up to its maximum.
     *
     * @param stat  The stat to increase.
     * @param value The value to increase it by.
     */
    public void increaseStat(PermanentStat stat, int value) {
        int newValue = Math.min(getStat(stat) + value, MAX_BASE_STAT);
        this.stats.put(stat, Math.max(newValue, MIN_BASE_STAT));
    }

    /**
     * Decreases a specific stat by a value, up to 0.
     *
     * @param stat  The stat to decrease.
     * @param value The value to decrease it by.
     */
    public void decreaseStat(PermanentStat stat, int value) {
        int newValue = Math.max(getStat(stat) - value, MIN_BASE_STAT);
        this.stats.put(stat, Math.min(newValue, MAX_BASE_STAT));
    }

    /**
     * Calculate stats using the configured calculator.
     *
     * @param totalEVs  The total EVs to account for in the calculation.
     * @param baseStats Base stats to use in the formula.
     * @param level     The level to be used to calculate the bonus.
     */
    public void calculateStats(int totalEVs, Map<PermanentStat, Integer> baseStats, int level) {
        calculateStats(totalEVs, baseStats, level, ThreadLocalRandom.current());
    }

    /**
     * Calculate stats using the configured calculator.
     *
     * @param totalEVs  The total EVs to account for in the calculation.
     * @param baseStats Base stats to use in the formula.
     * @param level     The level to be used to calculate the bonus.
     * @param rng       The random number generator to use for IV rolls.
     */
    public void calculateStats(int totalEVs, Map<PermanentStat, Integer> baseStats, int level, Random rng) {
        // Validate and clamp EVs before handing over to the calculator.
        int clampedEVs = clampTotalEVs(totalEVs);
        this.calculator.calculate(this, clampedEVs, baseStats, level, rng);
    }

    /**
     * Convenience overload to match existing external usage that passes a Map of EVs per stat.
     *
     * @param totalEVs  Map of EVs per stat to account for.
     * @param baseStats Base stats to use in the formula.
     * @param level     The level to be used to calculate the bonus.
     */
    public void calculateStats(Map<PermanentStat, Integer> totalEVs, Map<PermanentStat, Integer> baseStats, int level) {
        calculateStats(totalEVs.values().stream().mapToInt(Integer::intValue).sum(), baseStats, level);
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
        for (PermanentStat s : PermanentStat.values()) {
            int v = map.getOrDefault(s, MIN_BASE_STAT);
            this.stats.put(s, Math.max(MIN_BASE_STAT, Math.min(MAX_BASE_STAT, v)));
        }

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

    private int clampTotalEVs(int evs) {
        if (evs < MIN_EV_AMOUNT)
            return 0;

        return Math.min(evs, MAX_EV_AMOUNT_PER_POKEMON);
    }

    private static int calculateHPStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + ev / 4) * level) / 100 + level + 10;
    }

    private static int calculateOtherStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + ev / 4) * level) / 100 + 5;
    }
}
