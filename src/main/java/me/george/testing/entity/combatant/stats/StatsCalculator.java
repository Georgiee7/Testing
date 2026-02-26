package me.george.testing.entity.combatant.stats;

import java.util.Map;
import java.util.Random;

/**
 * Strategy interface for calculating permanent stats from base stats, EVs and IVs.
 * Created by George K. on 2/26/2026.
 */
public interface StatsCalculator {

    /**
     * Calculates the permanent stats for a combatant based on the given parameters.
     *
     * @param target    The target PermanentStats object to populate with calculated stats.
     * @param totalEVs  The total EVs to apply across all stats.
     * @param baseStats A map of base stats for the combatant, keyed by PermanentStat.
     * @param level     The level of the combatant, which may affect stat calculations.
     * @param rng       A Random instance to use for any random elements in the calculation (e.g. IV generation).
     */
    void calculate(PermanentStats target, int totalEVs, Map<PermanentStat, Integer> baseStats, int level, Random rng);

    /**
     * Convenience method to calculate permanent stats directly from an EV map, summing the EVs before calling the main calculate method.
     *
     * @param target    The target PermanentStats object to populate with calculated stats.
     * @param evMap     A map of EVs for each stat, keyed by PermanentStat.
     * @param baseStats A map of base stats for the combatant, keyed by PermanentStat.
     * @param level     The level of the combatant, which may affect stat calculations.
     * @param rng       A Random instance to use for any random elements in the calculation (e.g. IV generation).
     */
    default void calculateFromMap(PermanentStats target, Map<PermanentStat, Integer> evMap, Map<PermanentStat, Integer> baseStats, int level, Random rng) {
        int sum = evMap.values().stream().mapToInt(Integer::intValue).sum();
        calculate(target, sum, baseStats, level, rng);
    }
}
