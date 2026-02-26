package me.george.testing.manager;

import me.george.testing.data.storage.supported.PlayerFileRepository;
import me.george.testing.data.storage.supported.PokemonFileRepository;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    public void cleanup() throws Exception {
        if (Files.exists(this.playersDir)) {
            try (var w = Files.walk(this.playersDir)) {
                w.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignored) {
                    }
                });
            }
        }

        if (Files.exists(this.pokemonsDir)) {
            try (var w = Files.walk(this.pokemonsDir)) {
                w.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignored) {}
                });
            }
        }

        // clear managers.
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
        PokemonManager.getPokemons().forEach(p -> PokemonManager.removePokemon(p.getUuid()));
    }

    @Test
    public void testRegisterManagersLoadsAndLinks() {
        Player player = PlayerManager.create("integration_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        PlayerFileRepository.save(player);
        PokemonFileRepository.save(pokemon);

        // Clear caches.
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
        PokemonManager.getPokemons().forEach(p -> PokemonManager.removePokemon(p.getUuid()));

        // Now call registerManagers (this will start managers and perform relinking).
        Manager.registerManagers();

        // After registration, our persisted entities should be loaded and linked.
        Player loaded = PlayerManager.getPlayers().stream().filter(pp -> pp.getName().equals("integration_player")).findAny().orElse(null);
        assertNotNull(loaded);

        Pokemon loadedPoke = PokemonManager.getPokemons().stream().filter(pp -> pp.getType() == PokemonType.CHARMANDER).findAny().orElse(null);
        assertNotNull(loadedPoke);

        assertTrue(loaded.hasPokemon(loadedPoke));
        assertEquals(loaded.getUuid(), loadedPoke.getOwnerUuid());

        // Stop managers to clean up background PersistenceManager.
        Manager.disableManagers();
    }
}
