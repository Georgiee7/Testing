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
 * Integration-style test that exercises {@link Manager#registerManagers()} end-to-end for persistence + relinking.
 * Created by George K. on 2/26/2026.
 */
public class ManagerRegisterIntegrationTest {
    private final Path playersDir = PlayerFileRepository.getDir();
    private final Path pokemonsDir = PokemonFileRepository.getDir();

    @BeforeEach
    public void setup() throws Exception {
        // Ensure clean state: clear manager registry and in-memory caches.
        Manager.clearManagers();
        clearCaches();
        deleteDirectory(this.playersDir);
        deleteDirectory(this.pokemonsDir);
    }

    @AfterEach
    public void cleanup() throws Exception {
        Manager.clearManagers();
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
    public void testRegisterManagersLoadsAndLinks() {
        // Create and persist a player + pokemon with ownership.
        Player player = PlayerManager.create("integ_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        PlayerFileRepository.save(player);
        PokemonFileRepository.save(pokemon);

        // Clear caches to simulate fresh start.
        clearCaches();

        // Now call registerManagers (this will start managers and perform relinking).
        Manager.registerManagers();

        // After registration, our persisted entities should be loaded and linked.
        Player loaded = PlayerManager.getPlayers().stream()
                .filter(pp -> pp.getName().equals("integ_player")).findAny().orElse(null);
        assertNotNull(loaded, "Player should have been loaded by registerManagers");

        Pokemon loadedPoke = PokemonManager.getPokemons().stream()
                .filter(pp -> pp.getType() == PokemonType.CHARMANDER).findAny().orElse(null);
        assertNotNull(loadedPoke, "Pokemon should have been loaded by registerManagers");

        assertTrue(loaded.hasPokemon(loadedPoke));
        assertEquals(loaded.getUuid(), loadedPoke.getOwnerUuid());

        // Stop managers to clean up background PersistenceManager.
        Manager.disableManagers();
    }
}
