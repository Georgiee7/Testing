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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON serialization and deserialization of core entities.
 * Ensures round-trip fidelity (serialize → deserialize → key fields preserved).
 * Created by George K. on 2/26/2026.
 */
public class JsonSerializationTest {

    @AfterEach
    public void cleanup() {
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }

    @Test
    public void testPokemonJsonContainsExpectedFields() {
        Pokemon charmander = PokemonManager.create(PokemonType.CHARMANDER, null);
        charmander.addExp(1000);

        String json = JsonUtil.toJson(charmander);
        assertNotNull(json);
        assertTrue(json.contains("\"@type\""),   "Should have type discriminator");
        assertTrue(json.contains("charmander"),  "Should name the concrete type");
        assertTrue(json.contains("\"uuid\""));
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"level\""));
        assertTrue(json.contains("\"exp\""));
        assertTrue(json.contains("\"permanentStats\""));
        assertTrue(json.contains("\"type\""));
        assertFalse(json.contains("\"owner\""), "Runtime owner reference must not be serialized");
    }

    @Test
    public void testPokemonRoundTrip() {
        Pokemon original = PokemonManager.create(PokemonType.CHARMANDER, null);
        original.addExp(2000);

        String json = JsonUtil.toJson(original);
        Pokemon deserialized = JsonUtil.fromJson(json, Pokemon.class);

        assertNotNull(deserialized);
        assertEquals(original.getUuid(),   deserialized.getUuid());
        assertEquals(original.getName(),   deserialized.getName());
        assertEquals(original.getLevel(),  deserialized.getLevel());
        assertEquals(original.getExp(),    deserialized.getExp());
        assertEquals(original.getType(),   deserialized.getType());
        assertEquals(original.isShiny(),   deserialized.isShiny());
        assertEquals(original.isAlive(),   deserialized.isAlive());
    }

    @Test
    public void testPokemonRoundTripPreservesOwnerUuid() {
        Player owner = PlayerManager.create("ownertest");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, owner);

        String json = JsonUtil.toJson(pokemon);
        Pokemon deserialized = JsonUtil.fromJson(json, Pokemon.class);

        assertNotNull(deserialized);
        assertEquals(owner.getUuid(), deserialized.getOwnerUuid(),
                "ownerUuid must survive round-trip even though the owner object is not serialized");
        assertNull(deserialized.getOwner(),
                "Runtime owner reference should be null until re-linked by Manager.relinkPersistentEntities");
    }

    @Test
    public void testPlayerJsonContainsExpectedFields() {
        Player player = PlayerManager.create("jsonplayer");

        String json = JsonUtil.toJson(player);
        assertNotNull(json);
        assertTrue(json.contains("\"uuid\""));
        assertTrue(json.contains("\"name\""));
        // Runtime set must not leak into the JSON — only UUIDs are persisted.
        assertFalse(json.contains("\"pokemons\""),     "Runtime pokemons set must not be serialized");
        assertFalse(json.contains("\"activePokemon\""),"activePokemon must not be serialized");
        assertFalse(json.contains("\"pokedex\""),      "Runtime pokedex must not be serialized");
    }

    @Test
    public void testPlayerRoundTrip() {
        Player original = PlayerManager.create("roundtrip");

        String json = JsonUtil.toJson(original);
        Player deserialized = JsonUtil.fromJson(json, Player.class);

        assertNotNull(deserialized);
        assertEquals(original.getUuid(),  deserialized.getUuid());
        assertEquals(original.getName(),  deserialized.getName());
        assertEquals(original.getLevel(), deserialized.getLevel());
    }

    @Test
    public void testPlayerWithPokemonSerializesUuids() {
        Player player = PlayerManager.create("uuidtest");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        String json = JsonUtil.toJson(player);
        assertTrue(json.contains("\"pokemonUuids\""), "pokemonUuids list must be in JSON");
        assertFalse(json.contains("\"pokemons\""),    "Full pokemon objects must not be serialized");

        Player deserialized = JsonUtil.fromJson(json, Player.class);
        assertEquals(1, deserialized.getPersistedPokemonUuids().size());
        assertEquals(pokemon.getUuid(), deserialized.getPersistedPokemonUuids().get(0));
    }

    @Test
    public void testPermanentStatsRoundTrip() {
        PermanentStats stats = new PermanentStats();
        stats.setStat(PermanentStat.HP,     100);
        stats.setStat(PermanentStat.ATK,     80);
        stats.setStat(PermanentStat.DEF,     60);
        stats.setStat(PermanentStat.SP_ATK,  90);
        stats.setStat(PermanentStat.SP_DEF,  70);
        stats.setStat(PermanentStat.SPEED,   85);

        String json = JsonUtil.toJson(stats);
        assertFalse(json.contains("calculator"),  "Calculator must not be serialized");
        assertFalse(json.contains("initialized"), "Initialized flag must not be serialized");

        PermanentStats deserialized = JsonUtil.fromJson(json, PermanentStats.class);
        assertEquals(100, deserialized.getStat(PermanentStat.HP));
        assertEquals(80,  deserialized.getStat(PermanentStat.ATK));
        assertEquals(60,  deserialized.getStat(PermanentStat.DEF));
        assertEquals(90,  deserialized.getStat(PermanentStat.SP_ATK));
        assertEquals(70,  deserialized.getStat(PermanentStat.SP_DEF));
        assertEquals(85,  deserialized.getStat(PermanentStat.SPEED));
    }

    @Test
    public void testEnumSerializationAsName() {
        assertEquals("\"CHARMANDER\"", JsonUtil.toJson(PokemonType.CHARMANDER));
        assertEquals(PokemonType.CHARMANDER, JsonUtil.fromJson("\"CHARMANDER\"", PokemonType.class));
    }

    @Test
    public void testPrettyPrintIsMultiLine() {
        Pokemon charmander = PokemonManager.create(PokemonType.CHARMANDER, null);
        assertTrue(JsonUtil.toPrettyJson(charmander).contains("\n"));
    }
}
