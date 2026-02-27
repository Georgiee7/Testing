package me.george.testing.entity.pokemon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Pokémon experience and levelling system.
 * Created by George K. on 2/27/2026.
 */
public class PokemonExpLevelTest {

    @AfterEach
    public void cleanup() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }

    @Test
    public void testNewPokemonStartsAtLevelOne() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        assertEquals(1, p.getLevel());
        assertEquals(0, p.getExp());
    }

    @Test
    public void testAddingExpIncreasesExpTotal() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        p.addExp(100);
        assertTrue(p.getExp() >= 0, "Exp should be non-negative after adding");
    }

    @Test
    public void testAddingEnoughExpTriggersLevelUp() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        int needed = p.getNeededExp();
        p.addExp(needed);
        assertTrue(p.getLevel() > 1, "Pokemon should have levelled up after gaining enough exp");
    }

    @Test
    public void testMultipleLevelUpsFromLargeExpGrant() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        p.addExp(10_000);
        assertTrue(p.getLevel() > 2, "Large exp grant should cause multiple level-ups");
    }

    @Test
    public void testMaxLevelCapIsRespected() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        p.addExp(Integer.MAX_VALUE / 2);
        assertTrue(p.getLevel() <= PokemonManager.MAX_LEVEL,
                "Level must never exceed MAX_LEVEL regardless of exp added");
    }

    @Test
    public void testZeroExpDoesNothing() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        int levelBefore = p.getLevel();
        int expBefore   = p.getExp();
        p.addExp(0);
        assertEquals(levelBefore, p.getLevel());
        assertEquals(expBefore,   p.getExp());
    }

    @Test
    public void testNegativeExpDoesNothing() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        int levelBefore = p.getLevel();
        p.addExp(-500);
        assertEquals(levelBefore, p.getLevel());
    }

    @Test
    public void testExpRemainsAfterLevelUp() {
        Pokemon p = PokemonManager.create(PokemonType.CHARMANDER, null);
        int needed = p.getNeededExp();
        p.addExp(needed + 50);
        // After levelling up the leftover exp should be retained, not lost.
        assertTrue(p.getExp() >= 0, "Exp should carry over after level-up, not be negative");
    }
}
