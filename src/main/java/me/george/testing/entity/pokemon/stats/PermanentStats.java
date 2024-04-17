package me.george.testing.entity.pokemon.stats;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A wrapper around permanent stats a Pokémon may have.
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

    public PermanentStats(Map<PermanentStat, Integer> stats) {
        this.stats = stats;
        this.initialized = this.stats != null && !this.stats.isEmpty();
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
     * Calculates and re-assigns a combatant's stats according to Pokémon's base stat, EV and IV distribution rules.
     * Original formula used.
     *
     * @param totalEVs  Total EVs to account for.
     * @param baseStats Base stats to use in the formula.
     * @param level     The level to be used to calculate the bonus.
     */
    public void calculateStats(int totalEVs, Map<PermanentStat, Integer> baseStats, int level) {
        // Distribute EVs among stats, ensuring they don't exceed the maximum allowed per stat.
        int remainingEVs = Math.min(totalEVs, MAX_EV_AMOUNT_PER_POKEMON);
        for (PermanentStat stat : PermanentStat.values()) {
            int maxEVForStat = Math.min(remainingEVs, MAX_EV_AMOUNT_PER_STAT);
            int evForStat = ThreadLocalRandom.current().nextInt(maxEVForStat + 1);

            this.stats.put(stat, baseStats.getOrDefault(stat, MIN_BASE_STAT)); // Initialize to the stat, or otherwise the minimum base stat.
            increaseStat(stat, evForStat); // Increase by random EVs.
            remainingEVs -= evForStat;
        }

        // Generate IVs (Individual Values) for each stat.
        for (PermanentStat stat : PermanentStat.values()) {
            int iv = ThreadLocalRandom.current().nextInt(MIN_IV_ROLL, MAX_IV_ROLL + 1);
            int baseStat = getStat(stat);
            int ev = getStat(stat) - MIN_BASE_STAT;

            setStat(stat, stat == PermanentStat.HP ? calculateHPStat(baseStat, iv, ev, level) : calculateOtherStat(baseStat, iv, ev, level));
        }

        this.initialized = true;
    }

    /**
     * Calculates and re-assigns a combatant's stats according to Pokémon's base stat, EV and IV distribution rules.
     * Original formula used.
     *
     * @param totalEVs  Total EVs to account for.
     * @param baseStats Base stats to use in the formula.
     * @param level     The level to be used to calculate the bonus.
     */
    public void calculateStats(Map<PermanentStat, Integer> totalEVs, Map<PermanentStat, Integer> baseStats, int level) {
        calculateStats(totalEVs.values().stream().mapToInt(Integer::intValue).sum(), baseStats, level);
    }

    private static int calculateHPStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + ev / 4) * level) / 100 + level + 10;
    }

    private static int calculateOtherStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + ev / 4) * level) / 100 + 5;
    }
}
