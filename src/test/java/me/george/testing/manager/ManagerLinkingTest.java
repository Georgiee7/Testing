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
 * Tests that {@link Manager#relinkPersistentEntities()} correctly restores owner relationships
 * between Pokemon and Players after a simulated restart (clear → load from disk → relink).
 * Created by George K. on 2/26/2026.
 */
public class ManagerLinkingTest {

    @BeforeEach
    public void setup() throws Exception {
        clearCaches();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
    }

    @AfterEach
    public void cleanup() throws Exception {
        clearCaches();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
    }

    private void clearCaches() {
        PlayerManager.clearCache();
        PokemonManager.clearCache();
    }

    private void wipe(Path dir) throws Exception {
        if (!Files.exists(dir)) return;
        try (var w = Files.walk(dir)) {
            w.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        }
    }

    @Test
    public void testLinkingRestoresOwners() {
        Player player = PlayerManager.create("link_player");
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonManager.setPokemonOwner(pokemon, player);

        // Persist both.
        PlayerFileRepository.save(player);
        PokemonFileRepository.save(pokemon);

        // Simulate a fresh start: clear caches WITHOUT deleting files.
        clearCaches();

        // Re-load via onStart hooks (mirrors what Manager.registerManagers does).
        new PlayerManager().onStart();
        new PokemonManager().onStart();
        Manager.relinkPersistentEntities();

        Player loadedPlayer = PlayerManager.getPlayers().stream()
                .filter(p -> p.getUuid().equals(player.getUuid())).findFirst().orElse(null);
        assertNotNull(loadedPlayer, "Player should have been loaded from disk");

        Pokemon loadedPokemon = PokemonManager.getPokemons().stream()
                .filter(p -> p.getUuid().equals(pokemon.getUuid())).findFirst().orElse(null);
        assertNotNull(loadedPokemon, "Pokemon should have been loaded from disk");

        assertEquals(loadedPlayer.getUuid(), loadedPokemon.getOwnerUuid(),
                "Pokemon's ownerUuid should match the player's UUID");
        assertTrue(loadedPlayer.hasPokemon(loadedPokemon),
                "Player should have the pokemon in their runtime set after relinking");
    }

    @Test
    public void testLinkingWithNoOwnerIsNoop() {
        // A pokemon with no owner should load fine and have null ownerUuid.
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonFileRepository.save(pokemon);

        clearCaches();
        new PokemonManager().onStart();
        Manager.relinkPersistentEntities();

        Pokemon loaded = PokemonManager.getPokemons().stream()
                .filter(p -> p.getUuid().equals(pokemon.getUuid())).findFirst().orElse(null);
        assertNotNull(loaded);
        assertNull(loaded.getOwnerUuid(), "Unowned pokemon should have null ownerUuid");
        assertNull(loaded.getOwner(), "Unowned pokemon should have null owner reference");
    }
}
