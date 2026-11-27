package me.george.testing.entity.combatant.stats;

import java.util.Map;

/**
 * Strategy interface for calculating permanent stats from base stats, IVs and EVs.
 * Created by George K. on 2/26/2026.
 */
public interface StatsCalculator {

    /**
     * Calculates the permanent stats for a combatant based on the given parameters.
     *
     * @param target    The target PermanentStats object to populate with calculated stats.
     * @param baseStats A map of base stats for the combatant, keyed by PermanentStat.
     * @param ivs       The combatant's Individual Values (IVs) per stat, rolled once when it's created.
     * @param evs       The combatant's Effort Values (EVs) per stat.
     * @param level     The level of the combatant, which may affect stat calculations.
     */
    void calculate(PermanentStats target, Map<PermanentStat, Integer> baseStats, Map<PermanentStat, Integer> ivs, Map<PermanentStat, Integer> evs, int level);
}
