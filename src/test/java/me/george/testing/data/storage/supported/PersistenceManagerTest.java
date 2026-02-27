package me.george.testing.data.storage.supported;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PersistenceManager} — verifies that flushNow() actually writes
 * in-memory state to disk.
 * I/O goes to {@code target/test-data} (via the {@code data.dir} system property).
 * Created by George K. on 2/26/2026.
 */
public class PersistenceManagerTest {

    @BeforeEach
    public void setup() throws Exception {
        PlayerManager.clearCache();
        PokemonManager.clearCache();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
    }

    @AfterEach
    public void cleanup() throws Exception {
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
        PlayerManager.clearCache();
        PokemonManager.clearCache();
    }

    private void wipe(Path dir) throws Exception {
        if (!Files.exists(dir))
            return;

        try (var w = Files.walk(dir)) {
            w.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        }
    }

    @Test
    public void testFlushNowPersistsData() {
        Player player = PlayerManager.create("pm_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        // Remove any files already written by create/save so flushNow is the thing being tested.
        PlayerFileRepository.remove(player);
        PokemonFileRepository.remove(pokemon);

        Path playerFile = PlayerFileRepository.getDir().resolve(player.getUuid() + ".json");
        Path pokemonFile = PokemonFileRepository.getDir().resolve(pokemon.getUuid() + ".json");
        assertFalse(Files.exists(playerFile), "Player file should not exist before flush");
        assertFalse(Files.exists(pokemonFile), "Pokemon file should not exist before flush");

        PersistenceManager.flushNow();

        assertTrue(Files.exists(playerFile), "Player file should exist after flush");
        assertTrue(Files.exists(pokemonFile), "Pokemon file should exist after flush");

        // Loading should return both objects.
        Collection<Player> players = PlayerFileRepository.loadAll();
        assertTrue(players.stream().anyMatch(p -> p.getUuid().equals(player.getUuid())));

        Collection<Pokemon> pokemons = PokemonFileRepository.loadAll();
        assertTrue(pokemons.stream().anyMatch(p -> p.getUuid().equals(pokemon.getUuid())));
    }
}
