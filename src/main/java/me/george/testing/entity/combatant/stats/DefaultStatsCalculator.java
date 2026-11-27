package me.george.testing.entity.combatant.stats;

import java.util.Map;

/**
 * Default implementation of the stats calculator.
 * Created by George K. on 2/26/2026.
 */
public class DefaultStatsCalculator implements StatsCalculator {

    @Override
    public void calculate(PermanentStats target, Map<PermanentStat, Integer> baseStats, Map<PermanentStat, Integer> ivs, Map<PermanentStat, Integer> evs, int level) {
        for (PermanentStat stat : PermanentStat.values()) {
            int baseStat = baseStats.getOrDefault(stat, PermanentStats.MIN_BASE_STAT);
            int iv = ivs.getOrDefault(stat, PermanentStats.MIN_IV_ROLL);
            int ev = evs.getOrDefault(stat, PermanentStats.MIN_EV_AMOUNT);

            if (stat == PermanentStat.HP)
                target.setStat(stat, DefaultStatsCalculator.calculateHPStat(baseStat, iv, ev, level));
            else
                target.setStat(stat, DefaultStatsCalculator.calculateOtherStat(baseStat, iv, ev, level));
        }

        target.setInitialized(true);
    }

    private static int calculateHPStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + ev / 4) * level) / 100 + level + 10;
    }

    private static int calculateOtherStat(int baseStat, int iv, int ev, int level) {
        return ((2 * baseStat + iv + ev / 4) * level) / 100 + 5;
    }
}
