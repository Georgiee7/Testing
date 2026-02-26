package me.george.testing.entity.combatant.stats;

import java.util.Map;
import java.util.Random;

/**
 * Default implementation of the stats calculator.
 * Created by George K. on 2/26/2026.
 */
public class DefaultStatsCalculator implements StatsCalculator {

    @Override
    public void calculate(PermanentStats target, int totalEVs, Map<PermanentStat, Integer> baseStats, int level, Random rng) {
        int remainingEVs = Math.min(totalEVs, PermanentStats.MAX_EV_AMOUNT_PER_POKEMON);
        for (PermanentStat stat : PermanentStat.values()) {
            int maxEVForStat = Math.min(remainingEVs, PermanentStats.MAX_EV_AMOUNT_PER_STAT);
            int evForStat = (maxEVForStat > 0) ? rng.nextInt(maxEVForStat + 1) : 0;

            target.statsPut(stat, baseStats.getOrDefault(stat, PermanentStats.MIN_BASE_STAT));
            target.increaseStat(stat, evForStat);
            remainingEVs -= evForStat;
        }

        for (PermanentStat stat : PermanentStat.values()) {
            int iv = rng.nextInt(PermanentStats.MIN_IV_ROLL, PermanentStats.MAX_IV_ROLL + 1);
            int baseStat = target.getStat(stat);
            int ev = target.getStat(stat) - PermanentStats.MIN_BASE_STAT;

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
