package me.george.testing.entity.pokemon;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.combatant.stats.PermanentStats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the management of Pokémon.
 * Created by George K. on 4/18/2024.
 */
public class PokemonManagerTest {
    private final Pokemon MOCK_POKEMON = PokemonManager.create(PokemonType.CHARMANDER, mock(PermanentStats.class));
    private final Player MOCK_PLAYER = PlayerManager.create("test_player");

    @Test
    public void testCreatePokemon() {
        assertNotNull(MOCK_POKEMON);
        assertNotNull(MOCK_POKEMON.getPermanentStats());
    }

    @Test
    public void testCreateEveryType() {
        for (PokemonType type : PokemonType.values()) {
            Pokemon pokemon = PokemonManager.create(type, null);
            assertEquals(type, pokemon.getType());
            assertEquals(pokemon.getHP(), pokemon.getHealth());
            PokemonManager.removePokemon(pokemon);
        }
    }

    @Test
    public void testSetPokemonOwner() {
        PokemonManager.setPokemonOwner(MOCK_POKEMON, MOCK_PLAYER);
        assertEquals(MOCK_PLAYER, MOCK_POKEMON.getOwner());
        assertTrue(MOCK_PLAYER.hasPokemon(MOCK_POKEMON));
    }

    @Test
    public void testRemovePokemonOwner() {
        PokemonManager.setPokemonOwner(MOCK_POKEMON, MOCK_PLAYER);
        PokemonManager.removePokemonOwner(MOCK_POKEMON, MOCK_PLAYER);
        assertNull(MOCK_POKEMON.getOwner());
        assertFalse(MOCK_PLAYER.hasPokemon(MOCK_POKEMON));
    }

    @Test
    public void testGetPokemon() {
        final UUID id = MOCK_POKEMON.getUuid();
        assertEquals(MOCK_POKEMON, PokemonManager.getPokemon(id));
    }

    @Test
    public void testRemovePokemon() {
        final UUID id = MOCK_POKEMON.getUuid();

        PokemonManager.removePokemon(id);
        assertNull(PokemonManager.getPokemon(id));
    }

    @Test
    public void testGetPokemons() {
        final Collection<Pokemon> pokemons = PokemonManager.getPokemons();
        assertTrue(pokemons.contains(MOCK_POKEMON));
    }

    @Test
    public void testIsValidPokemonName() {
        assertTrue(PokemonManager.isValidPokemonName("Pikachu"));
        assertTrue(PokemonManager.isValidPokemonName("charizard123"));
        assertFalse(PokemonManager.isValidPokemonName(""));
        assertFalse(PokemonManager.isValidPokemonName("12"));
        assertFalse(PokemonManager.isValidPokemonName("Name_With_Spaces"));
        assertFalse(PokemonManager.isValidPokemonName("NameWith!Invalid@Chars"));
    }

    @AfterAll
    public static void tearDown() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
