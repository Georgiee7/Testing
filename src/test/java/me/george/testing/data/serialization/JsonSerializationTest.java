package me.george.testing.data.serialization;

import me.george.testing.entity.combatant.stats.PermanentStat;
import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for JSON serialization and deserialization of core entities.
 * Ensures round-trip fidelity (serialize → deserialize → assert equality of key fields).
 * Created by George K. on 2/26/2026.
 */
public class JsonSerializationTest {

    @AfterEach
    public void cleanup() {
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }

    @Test
    public void testPokemonSerialization() {
        Pokemon charmander = PokemonManager.create(PokemonType.CHARMANDER, null);
        charmander.addExp(1000);

        String json = JsonUtil.toJson(charmander);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Verify key fields are present in JSON.
        assertTrue(json.contains("\"@type\""));
        assertTrue(json.contains("charmander"));
        assertTrue(json.contains("\"uuid\""));
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"level\""));
        assertTrue(json.contains("\"exp\""));
        assertTrue(json.contains("\"permanentStats\""));
        assertTrue(json.contains("\"type\""));

        // Verify owner is NOT serialized (it's @JsonIgnored).
        assertFalse(json.contains("\"owner\""));
    }

    @Test
    public void testPokemonRoundTrip() {
        Pokemon original = PokemonManager.create(PokemonType.CHARMANDER, null);
        original.addExp(2000);

        String json = JsonUtil.toJson(original);
        Pokemon deserialized = JsonUtil.fromJson(json, Pokemon.class);

        assertNotNull(deserialized);
        assertEquals(original.getUuid(), deserialized.getUuid());
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getLevel(), deserialized.getLevel());
        assertEquals(original.getExp(), deserialized.getExp());
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.isShiny(), deserialized.isShiny());
        assertEquals(original.isAlive(), deserialized.isAlive());
    }

    @Test
    public void testPlayerSerialization() {
        Player player = PlayerManager.create("TestPlayer1");

        String json = JsonUtil.toJson(player);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        assertTrue(json.contains("\"@type\""));
        assertTrue(json.contains("player"));
        assertTrue(json.contains("\"uuid\""));
        assertTrue(json.contains("\"name\""));

        // activePokemon is @JsonIgnored.
        assertFalse(json.contains("\"activePokemon\""));
        // The runtime pokemons set should NOT be serialized; only pokemonUuids.
        assertFalse(json.contains("\"pokemons\""));
    }

    @Test
    public void testPlayerRoundTrip() {
        Player original = PlayerManager.create("RoundTrip1");

        String json = JsonUtil.toJson(original);
        Player deserialized = JsonUtil.fromJson(json, Player.class);

        assertNotNull(deserialized);
        assertEquals(original.getUuid(), deserialized.getUuid());
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getLevel(), deserialized.getLevel());
    }

    @Test
    public void testPermanentStatsSerialization() {
        PermanentStats stats = new PermanentStats();
        stats.setStat(PermanentStat.HP, 100);
        stats.setStat(PermanentStat.ATK, 80);
        stats.setStat(PermanentStat.DEF, 60);
        stats.setStat(PermanentStat.SP_ATK, 90);
        stats.setStat(PermanentStat.SP_DEF, 70);
        stats.setStat(PermanentStat.SPEED, 85);

        String json = JsonUtil.toJson(stats);
        assertNotNull(json);

        // Verify calculator is NOT serialized.
        assertFalse(json.contains("calculator"));
        assertFalse(json.contains("initialized"));

        // Round-trip.
        PermanentStats deserialized = JsonUtil.fromJson(json, PermanentStats.class);
        assertNotNull(deserialized);

        assertEquals(100, deserialized.getStat(PermanentStat.HP));
        assertEquals(80, deserialized.getStat(PermanentStat.ATK));
        assertEquals(60, deserialized.getStat(PermanentStat.DEF));
        assertEquals(90, deserialized.getStat(PermanentStat.SP_ATK));
        assertEquals(70, deserialized.getStat(PermanentStat.SP_DEF));
        assertEquals(85, deserialized.getStat(PermanentStat.SPEED));
    }

    @Test
    public void testPrettyJsonOutput() {
        Pokemon charmander = PokemonManager.create(PokemonType.CHARMANDER, null);
        String prettyJson = JsonUtil.toPrettyJson(charmander);

        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\n")); // Pretty print produces multi-line output.
    }

    @Test
    public void testPlayerWithPokemonSerializesUuids() {
        Player player = PlayerManager.create("OwnerTest1");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        String playerJson = JsonUtil.toJson(player);
        assertNotNull(playerJson);

        // Player should contain pokemonUuids (not full pokemon objects).
        assertTrue(playerJson.contains("\"pokemonUuids\""));
        assertFalse(playerJson.contains("\"pokemons\""));

        // Deserialize and verify the persisted UUID list.
        Player deserializedPlayer = JsonUtil.fromJson(playerJson, Player.class);
        assertNotNull(deserializedPlayer);
        assertEquals(player.getUuid(), deserializedPlayer.getUuid());
        assertEquals(1, deserializedPlayer.getPersistedPokemonUuids().size());
        assertEquals(pokemon.getUuid(), deserializedPlayer.getPersistedPokemonUuids().get(0));
    }

    @Test
    public void testEnumSerializationAsName() {
        // PokemonType should serialize as its enum constant name.
        String json = JsonUtil.toJson(PokemonType.CHARMANDER);
        assertEquals("\"CHARMANDER\"", json);

        PokemonType deserialized = JsonUtil.fromJson(json, PokemonType.class);
        assertEquals(PokemonType.CHARMANDER, deserialized);
    }
}
