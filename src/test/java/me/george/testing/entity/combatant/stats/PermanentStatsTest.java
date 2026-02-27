package me.george.testing.entity.combatant.stats;

import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PermanentStats class — determinism, custom calculators, EV system, clamping.
 * Created by George K. on 2/26/2026.
 */
public class PermanentStatsTest {

    @AfterEach
    public void cleanup() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }

    @Test
    public void deterministicWithSeedProducesSameResults() {
        Map<PermanentStat, Integer> base = new EnumMap<>(PermanentStat.class);
        for (PermanentStat s : PermanentStat.values()) base.put(s, 50);

        long seed = 123456789L;
        PermanentStats a = new PermanentStats();
        PermanentStats b = new PermanentStats();

        a.calculateStats(100, base, 50, new Random(seed));
        b.calculateStats(100, base, 50, new Random(seed));

        assertEquals(a.asMap(), b.asMap(), "Stats must match for the same RNG seed");
        a.asMap().values().forEach(v ->
                assertTrue(v >= PermanentStats.MIN_BASE_STAT, "All stats must be >= minimum"));
    }

    @Test
    public void customCalculatorIsUsed() {
        PermanentStats stats = new PermanentStats();
        StatsCalculator fixed = (target, totalEVs, baseStats, level, rng) -> {
            for (PermanentStat s : PermanentStat.values())
                target.statsPut(s, 42);
            target.setInitialized(true);
        };

        stats.setCalculator(fixed);
        stats.calculateStats(0, Map.of(), 1, new Random(1));

        for (PermanentStat s : PermanentStat.values())
            assertEquals(42, stats.getStat(s), "Custom calculator value must be returned for " + s);
    }

    @Test
    public void resettingCalculatorToNullFallsBackToDefault() {
        PermanentStats stats = new PermanentStats();
        stats.setCalculator(null);

        Map<PermanentStat, Integer> base = new EnumMap<>(PermanentStat.class);
        for (PermanentStat s : PermanentStat.values()) base.put(s, 50);

        assertDoesNotThrow(() -> stats.calculateStats(0, base, 10, new Random(0)));
    }

    @Test
    public void setStatClampsToMinimum() {
        PermanentStats stats = new PermanentStats();
        stats.setStat(PermanentStat.ATK, -999);
        assertEquals(PermanentStats.MIN_BASE_STAT, stats.getStat(PermanentStat.ATK));
    }

    @Test
    public void setStatClampsToMaximum() {
        PermanentStats stats = new PermanentStats();
        stats.setStat(PermanentStat.ATK, Integer.MAX_VALUE);
        assertEquals(PermanentStats.MAX_BASE_STAT, stats.getStat(PermanentStat.ATK));
    }

    @Test
    public void increaseStatRespectsCap() {
        PermanentStats stats = new PermanentStats();
        stats.setStat(PermanentStat.HP, PermanentStats.MAX_BASE_STAT);
        stats.increaseStat(PermanentStat.HP, 100);
        assertEquals(PermanentStats.MAX_BASE_STAT, stats.getStat(PermanentStat.HP));
    }

    @Test
    public void decreaseStatRespectsFloor() {
        PermanentStats stats = new PermanentStats();
        stats.setStat(PermanentStat.HP, PermanentStats.MIN_BASE_STAT);
        stats.decreaseStat(PermanentStat.HP, 100);
        assertEquals(PermanentStats.MIN_BASE_STAT, stats.getStat(PermanentStat.HP));
    }

    @Test
    public void copyProducesIndependentInstance() {
        PermanentStats original = new PermanentStats();
        original.setStat(PermanentStat.ATK, 80);

        PermanentStats copy = original.copy();
        copy.setStat(PermanentStat.ATK, 50);

        assertEquals(80, original.getStat(PermanentStat.ATK),
                "Mutating the copy must not affect the original");
    }

    @Test
    public void addEvIncreasesStatForPokemon() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        int atkBefore = pokemon.getAttack();
        pokemon.addEv(PermanentStat.ATK, 100);
        assertTrue(pokemon.getAttack() >= atkBefore,
                "Adding ATK EVs must not decrease attack stat");
    }

    @Test
    public void totalEvsCannotExceedCap() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        for (PermanentStat stat : PermanentStat.values())
            pokemon.addEv(stat, PermanentStats.MAX_EV_AMOUNT_PER_STAT);

        int total = pokemon.getEvs().values().stream().mapToInt(Integer::intValue).sum();
        assertTrue(total <= PermanentStats.MAX_EV_AMOUNT_PER_POKEMON,
                "Total EVs must never exceed MAX_EV_AMOUNT_PER_POKEMON (" +
                PermanentStats.MAX_EV_AMOUNT_PER_POKEMON + "), got " + total);
    }

    @Test
    public void zeroEvAmountIsIgnored() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        Map<PermanentStat, Integer> evsBefore = Map.copyOf(pokemon.getEvs());
        pokemon.addEv(PermanentStat.ATK, 0);
        assertEquals(evsBefore, pokemon.getEvs(), "Adding 0 EVs should not change the EV map");
    }
}
