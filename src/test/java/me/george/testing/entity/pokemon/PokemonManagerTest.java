package me.george.testing.entity.pokemon;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the management of Pokémon.
 * Created by George K. on 4/18/2024.
 */
public class PokemonManagerTest {

    private Pokemon testPokemon;
    private Player testPlayer;

    @BeforeEach
    public void setup() {
        testPokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        testPlayer = PlayerManager.create("test_player");
    }

    @AfterEach
    public void cleanup() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
    }

    @Test
    public void testCreatePokemon() {
        assertNotNull(testPokemon);
        assertNotNull(testPokemon.getPermanentStats());
    }

    @Test
    public void testSetPokemonOwner() {
        PokemonManager.setPokemonOwner(testPokemon, testPlayer);
        assertEquals(testPlayer, testPokemon.getOwner());
        assertTrue(testPlayer.hasPokemon(testPokemon));
    }

    @Test
    public void testRemovePokemonOwner() {
        PokemonManager.setPokemonOwner(testPokemon, testPlayer);
        PokemonManager.removePokemonOwner(testPokemon, testPlayer);
        assertNull(testPokemon.getOwner());
        assertFalse(testPlayer.hasPokemon(testPokemon));
    }

    @Test
    public void testGetPokemon() {
        final UUID id = testPokemon.getUuid();
        assertEquals(testPokemon, PokemonManager.getPokemon(id));
    }

    @Test
    public void testRemovePokemon() {
        final UUID id = testPokemon.getUuid();
        PokemonManager.removePokemon(id);
        assertNull(PokemonManager.getPokemon(id));
    }

    @Test
    public void testGetPokemons() {
        final Collection<Pokemon> pokemons = PokemonManager.getPokemons();
        assertTrue(pokemons.contains(testPokemon));
    }

    @Test
    public void testIsValidPokemonName() {
        assertTrue(PokemonManager.isValidPokemonName("Pikachu"));
        assertTrue(PokemonManager.isValidPokemonName("charizard12"));
        assertFalse(PokemonManager.isValidPokemonName(""));
        assertFalse(PokemonManager.isValidPokemonName("12"));
        assertFalse(PokemonManager.isValidPokemonName("TooLongPokeName"));
        assertFalse(PokemonManager.isValidPokemonName("Name!Invalid"));
    }
}
