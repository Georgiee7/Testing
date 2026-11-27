package me.george.testing.entity.pokemon;

import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.combatant.stats.PermanentStats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests a Pokémon's health, stats and how they change when levelling up.
 * Created by George K. on 11/27/2026.
 */
public class PokemonStatsTest {

    @Test
    public void testStartsAtFullHealth() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        assertTrue(pokemon.isAlive());
        assertEquals(pokemon.getHP(), pokemon.getHealth());
    }

    @Test
    public void testDamageCanKill() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        pokemon.decreaseHP(1);
        assertTrue(pokemon.isAlive()); // A single point of damage used to kill it.
        assertEquals(pokemon.getHP() - 1, pokemon.getHealth());

        pokemon.decreaseHP(pokemon.getHP());
        assertFalse(pokemon.isAlive());
        assertEquals(0, pokemon.getHealth());

        pokemon.heal();
        assertTrue(pokemon.isAlive());
    }

    @Test
    public void testHealthCantGoAboveHPStat() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        pokemon.setHealth(9999);
        assertEquals(pokemon.getHP(), pokemon.getHealth());
    }

    @Test
    public void testLevellingUpNeverLowersStats() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        Map<PermanentStat, Integer> previousStats = pokemon.getPermanentStats().asMap();
        while (pokemon.getLevel() < 50) {
            levelUp(pokemon);
            Map<PermanentStat, Integer> stats = pokemon.getPermanentStats().asMap();
            for (PermanentStat stat : PermanentStat.values())
                assertTrue(stats.get(stat) >= previousStats.get(stat), stat + " went down on level " + pokemon.getLevel());

            previousStats = stats;
        }

        // Level 50 Charmander with no EVs: 99-114 health, depending on its IVs.
        assertTrue(pokemon.getHP() >= 99 && pokemon.getHP() <= 114, "Unexpected health stat: " + pokemon.getHP());
    }

    @Test
    public void testLevellingUpKeepsDamageTaken() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        while (pokemon.getLevel() < 10)
            levelUp(pokemon);

        pokemon.decreaseHP(5);
        levelUp(pokemon);
        assertEquals(pokemon.getHP() - 5, pokemon.getHealth());
    }

    @Test
    public void testEVLimits() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        pokemon.addEv(PermanentStat.ATK, 300);
        assertEquals(PermanentStats.MAX_EV_AMOUNT_PER_STAT, pokemon.getEvs().get(PermanentStat.ATK));

        pokemon.addEv(PermanentStat.DEF, 252);
        pokemon.addEv(PermanentStat.SPEED, 252);
        assertEquals(PermanentStats.MAX_EV_AMOUNT_PER_POKEMON - 2 * PermanentStats.MAX_EV_AMOUNT_PER_STAT, pokemon.getEvs().get(PermanentStat.SPEED));
    }

    @Test
    public void testExpRequirementsAreCached() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        int neededExp = pokemon.getNeededExp(10);
        assertEquals(neededExp, pokemon.getCachedExpRequirement()[9]);
    }

    private static void levelUp(Pokemon pokemon) {
        pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());
    }

    @AfterAll
    public static void tearDown() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
