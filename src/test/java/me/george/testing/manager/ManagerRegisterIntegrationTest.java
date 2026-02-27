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
 * Integration test for {@link Manager#registerManagers()} — verifies end-to-end that persisted
 * data is loaded and re-linked when the manager stack starts up.
 * Created by George K. on 2/26/2026.
 */
public class ManagerRegisterIntegrationTest {

    @BeforeEach
    public void setup() throws Exception {
        Manager.clearManagers();
        PlayerManager.clearCache();
        PokemonManager.clearCache();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
    }

    @AfterEach
    public void cleanup() throws Exception {
        Manager.clearManagers();
        PlayerManager.clearCache();
        PokemonManager.clearCache();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
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
    public void testRegisterManagersLoadsAndLinks() {
        Player player = PlayerManager.create("integ_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        PlayerFileRepository.save(player);
        PokemonFileRepository.save(pokemon);

        // Simulate fresh start: clear caches, keep files.
        PlayerManager.clearCache();
        PokemonManager.clearCache();

        Manager.registerManagers();

        Player loaded = PlayerManager.getPlayers().stream()
                .filter(p -> p.getUuid().equals(player.getUuid())).findFirst().orElse(null);
        assertNotNull(loaded, "Player should have been loaded by registerManagers");

        Pokemon loadedPoke = PokemonManager.getPokemons().stream()
                .filter(p -> p.getUuid().equals(pokemon.getUuid())).findFirst().orElse(null);
        assertNotNull(loadedPoke, "Pokemon should have been loaded by registerManagers");

        assertTrue(loaded.hasPokemon(loadedPoke));
        assertEquals(loaded.getUuid(), loadedPoke.getOwnerUuid());

        Manager.disableManagers();
    }
}
