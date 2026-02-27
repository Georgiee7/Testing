package me.george.testing.manager;

import me.george.testing.data.storage.supported.PlayerFileRepository;
import me.george.testing.data.storage.supported.PokemonFileRepository;
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
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the linking process correctly restores relationships between entities after loading from disk.
 * Created by George K. on 2/26/2026.
 */
public class ManagerLinkingTest {
    private final Path playersDir = PlayerFileRepository.getDir();
    private final Path pokemonsDir = PokemonFileRepository.getDir();

    @BeforeEach
    public void setup() throws Exception {
        // Ensure clean state before each test.
        clearCaches();
        deleteDirectory(this.playersDir);
        deleteDirectory(this.pokemonsDir);
    }

    @AfterEach
    public void cleanup() throws Exception {
        clearCaches();
        deleteDirectory(this.playersDir);
        deleteDirectory(this.pokemonsDir);
    }

    private void clearCaches() {
        PlayerManager.clearCache();
        PokemonManager.clearCache();
    }

    private void deleteDirectory(Path dir) throws Exception {
        if (Files.exists(dir)) {
            try (var w = Files.walk(dir)) {
                w.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    @Test
    public void testLinkingRestoresOwners() {
        // Create a player and pokemon and persist them.
        Player player = PlayerManager.create("link_test_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);

        // Set ownership and persist both.
        PokemonManager.setPokemonOwner(pokemon, player);
        PlayerFileRepository.save(player);
        PokemonFileRepository.save(pokemon);

        // Clear in-memory caches to simulate a fresh start.
        clearCaches();

        // Load from disk via managers' onStart hooks.
        PlayerManager pm = new PlayerManager();
        PokemonManager pokm = new PokemonManager();
        pm.onStart();
        pokm.onStart();

        // Perform linking.
        Manager.relinkPersistentEntities();

        // Now the managers' caches should contain the linked objects.
        Player loadedPlayer = PlayerManager.getPlayers().stream()
                .filter(p -> p.getName().equals("link_test_player")).findAny().orElse(null);
        assertNotNull(loadedPlayer, "Player should have been loaded from disk");

        Pokemon loadedPokemon = PokemonManager.getPokemons().stream()
                .filter(p -> p.getType() == PokemonType.CHARMANDER).findAny().orElse(null);
        assertNotNull(loadedPokemon, "Pokemon should have been loaded from disk");

        // The owner should be set and player should have the pokemon in their runtime set.
        assertEquals(loadedPlayer.getUuid(), loadedPokemon.getOwnerUuid());
        assertTrue(loadedPlayer.hasPokemon(loadedPokemon));
    }
}
