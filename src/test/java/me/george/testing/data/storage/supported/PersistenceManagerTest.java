package me.george.testing.data.storage.supported;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for PersistenceManager and its interaction with repositories.
 * Created by George K. on 2/26/2026.
 */
public class PersistenceManagerTest {
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
                        }
                );
            }
        }

        if (Files.exists(this.pokemonsDir)) {
            try (var w = Files.walk(this.pokemonsDir)) {
                w.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignored) {
                    }
                });
            }
        }

        // Clear runtime caches.
        for (Player p : PlayerManager.getPlayers())
            PlayerManager.removePlayer(p.getUuid());

        for (Pokemon pk : PokemonManager.getPokemons())
            PokemonManager.removePokemon(pk.getUuid());
    }

    @Test
    public void testFlushNowPersistsData() {
        Player player = PlayerManager.create("pm_test_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        // Remove files if present to ensure flush truly writes them.
        PlayerFileRepository.remove(player);
        PokemonFileRepository.remove(pokemon);

        // Ensure files are absent.
        Path playerFile = this.playersDir.resolve(player.getUuid().toString() + ".json");
        Path pokemonFile = this.pokemonsDir.resolve(pokemon.getUuid().toString() + ".json");
        assertFalse(Files.exists(playerFile));
        assertFalse(Files.exists(pokemonFile));

        // Trigger immediate flush.
        PersistenceManager.flushNow();

        // files should now exist.
        assertTrue(Files.exists(playerFile));
        assertTrue(Files.exists(pokemonFile));

        // And loading should return the objects.
        Collection<Player> players = PlayerFileRepository.loadAll();
        assertTrue(players.stream().anyMatch(p -> p.getUuid().equals(player.getUuid())));

        Collection<Pokemon> pokemons = PokemonFileRepository.loadAll();
        assertTrue(pokemons.stream().anyMatch(p -> p.getUuid().equals(pokemon.getUuid())));
    }
}
