package me.george.testing.entity.combatant.stats;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the PermanentStats class, ensuring that stat calculations follow the formula and that custom calculators are properly utilized.
 * Created by George K. on 2/26/2026.
 */
public class PermanentStatsTest {

    @Test
    public void calculatesKnownValues() {
        PermanentStats stats = new PermanentStats();
        Map<PermanentStat, Integer> evs = new EnumMap<>(PermanentStat.class);
        evs.put(PermanentStat.ATK, 252);

        stats.calculateStats(allStats(50), allStats(31), evs, 50);
        assertEquals(125, stats.getStat(PermanentStat.HP));
        assertEquals(102, stats.getStat(PermanentStat.ATK)); // 252 EVs.
        assertEquals(70, stats.getStat(PermanentStat.DEF));
        assertTrue(stats.isInitialized());
    }

    @Test
    public void sameInputsGiveSameStats() {
        PermanentStats a = new PermanentStats();
        PermanentStats b = new PermanentStats();

        a.calculateStats(allStats(80), allStats(15), allStats(10), 37);
        b.calculateStats(allStats(80), allStats(15), allStats(10), 37);
        assertEquals(a.asMap(), b.asMap(), "Stats must match for the same base stats, IVs, EVs and level");
    }

    @Test
    public void calculatedStatsCanGoPastTheBaseStatCap() {
        PermanentStats stats = new PermanentStats();
        Map<PermanentStat, Integer> evs = new EnumMap<>(PermanentStat.class);
        evs.put(PermanentStat.HP, 252);

        stats.calculateStats(allStats(PermanentStats.MAX_BASE_STAT), allStats(PermanentStats.MAX_IV_ROLL), evs, 100);
        assertEquals(714, stats.getStat(PermanentStat.HP));
    }

    @Test
    public void customCalculatorIsUsed() {
        PermanentStats stats = new PermanentStats();

        // Small test calculator that sets all stats to a known value.
        StatsCalculator fixed = (target, baseStats, ivs, evs, level) -> {
            for (PermanentStat s : PermanentStat.values())
                target.statsPut(s, 42);

            target.setInitialized(true);
        };

        stats.setCalculator(fixed);
        stats.calculateStats(Map.of(), Map.of(), Map.of(), 1);
        for (PermanentStat s : PermanentStat.values())
            assertEquals(42, stats.getStat(s));
    }

    private static Map<PermanentStat, Integer> allStats(int value) {
        Map<PermanentStat, Integer> stats = new EnumMap<>(PermanentStat.class);
        for (PermanentStat s : PermanentStat.values())
            stats.put(s, value);

        return stats;
    }
}
