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
 * Tests that the linking process correctly restores relationships between entities after loading from disk.
 * Created by George K. on 2/26/2026.
 */
public class ManagerLinkingTest {
    private final Path playersDir = PlayerFileRepository.getDir();
    private final Path pokemonsDir = PokemonFileRepository.getDir();

    @AfterEach
    public void cleanup() throws Exception {
        // Simple recursive delete.
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
                    } catch (Exception ignored) {
                    }
                });
            }
        }

        // Clear in-memory managers: deleteData removes persisted files as well, ensure we only clear caches.
        for (Player pl : PlayerManager.getPlayers())
            PlayerManager.removePlayer(pl.getUuid());

        for (Pokemon pk : PokemonManager.getPokemons())
            PokemonManager.removePokemon(pk.getUuid());
    }

    @Test
    public void testLinkingRestoresOwners() {
        // Create a player and pokemon and persist them.
        Player player = PlayerManager.create("link_test_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);

        // set ownership and persist both.
        PokemonManager.setPokemonOwner(pokemon, player);
        PlayerFileRepository.save(player);
        PokemonFileRepository.save(pokemon);

        // Clear in-memory caches to simulate a fresh start.
        for (Player p : PlayerManager.getPlayers())
            PlayerManager.removePlayer(p.getUuid());

        for (Pokemon pk : PokemonManager.getPokemons())
            PokemonManager.removePokemon(pk.getUuid());

        // Load from disk via managers' onStart hooks.
        PlayerManager pm = new PlayerManager();
        PokemonManager pokm = new PokemonManager();

        pm.onStart();
        pokm.onStart();

        // perform linking.
        Manager.relinkPersistentEntities();

        // Now the managers' caches should contain the linked objects.
        Player loadedPlayer = PlayerManager.getPlayers().stream().filter(p -> p.getName().equals("link_test_player")).findAny().orElse(null);
        assertNotNull(loadedPlayer);

        Pokemon loadedPokemon = PokemonManager.getPokemons().stream().filter(p -> p.getType() == PokemonType.CHARMANDER).findAny().orElse(null);
        assertNotNull(loadedPokemon);

        // The owner should be set and player should have the pokemon in their runtime set.
        assertEquals(loadedPlayer.getUuid(), loadedPokemon.getOwnerUuid());
        assertTrue(loadedPlayer.hasPokemon(loadedPokemon));
    }
}
