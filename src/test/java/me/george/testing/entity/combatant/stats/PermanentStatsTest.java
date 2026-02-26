package me.george.testing.entity.combatant.stats;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the PermanentStats class, ensuring that stat calculations are deterministic with a given RNG seed and that custom calculators are properly utilized.
 * Created by George K. on 2/26/2026.
 */
public class PermanentStatsTest {

    @Test
    public void deterministicWithSeedProducesSameResults() {
        Map<PermanentStat, Integer> base = new EnumMap<>(PermanentStat.class);
        for (PermanentStat s : PermanentStat.values()) base.put(s, 50);

        long seed = 123456789L;
        PermanentStats a = new PermanentStats();
        PermanentStats b = new PermanentStats();

        a.calculateStats(100, base, 50, new Random(seed));
        b.calculateStats(100, base, 50, new Random(seed));

        assertEquals(a.asMap(), b.asMap(), "Stats must match for same RNG seed");
        assertTrue(!a.asMap().isEmpty() && a.asMap().values().stream().allMatch(v -> v >= PermanentStats.MIN_BASE_STAT));
    }

    @Test
    public void customCalculatorIsUsed() {
        PermanentStats stats = new PermanentStats();

        // Small test calculator that sets all stats to a known value.
        StatsCalculator fixed = (target, totalEVs, baseStats, level, rng) -> {
            for (PermanentStat s : PermanentStat.values())
                target.statsPut(s, 42);

            target.setInitialized(true);
        };

        stats.setCalculator(fixed);
        stats.calculateStats(0, Map.of(), 1, new Random(1));
        for (PermanentStat s : PermanentStat.values())
            assertEquals(42, stats.getStat(s));
    }
}
