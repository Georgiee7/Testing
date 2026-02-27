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
 * Tests for the file-backed repositories to ensure they can save, load, and handle corrupt files correctly.
 * Created by George K. on 2/26/2026.
 */
public class FileRepositoryTest {
    private final Path playersDir = PlayerFileRepository.getDir();
    private final Path pokemonsDir = PokemonFileRepository.getDir();

    @BeforeEach
    public void setup() throws Exception {
        // Ensure clean state before each test.
        PlayerManager.clearCache();
        PokemonManager.clearCache();
        deleteDirectory(this.playersDir);
        deleteDirectory(this.pokemonsDir);
    }

    @AfterEach
    public void cleanup() throws Exception {
        // Clear runtime caches first.
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);

        // Recursively delete test data directories if they exist.
        deleteDirectory(this.playersDir);
        deleteDirectory(this.pokemonsDir);
    }

    private void deleteDirectory(Path dir) throws Exception {
        if (Files.exists(dir)) {
            try (var walker = Files.walk(dir)) {
                walker.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (Exception ignored) {
                            }
                        });
            }
        }
    }

    @Test
    public void testPlayerSaveLoad() {
        Player player = PlayerManager.create("testfileplayer");
        PlayerFileRepository.save(player);

        Collection<Player> players = PlayerFileRepository.loadAll();
        assertTrue(players.stream().anyMatch(p -> p.getUuid().equals(player.getUuid())));

        PlayerFileRepository.remove(player);
        Collection<Player> playersAfterRemove = PlayerFileRepository.loadAll();
        assertFalse(playersAfterRemove.stream().anyMatch(p -> p.getUuid().equals(player.getUuid())));
    }

    @Test
    public void testPokemonSaveLoad() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonFileRepository.save(pokemon);

        Collection<Pokemon> pokemons = PokemonFileRepository.loadAll();
        assertTrue(pokemons.stream().anyMatch(p -> p.getUuid().equals(pokemon.getUuid())));

        PokemonFileRepository.remove(pokemon);
        Collection<Pokemon> pokemonsAfterRemove = PokemonFileRepository.loadAll();
        assertFalse(pokemonsAfterRemove.stream().anyMatch(p -> p.getUuid().equals(pokemon.getUuid())));
    }

    @Test
    public void testCorruptFileMoved() throws Exception {
        // Create players dir.
        Files.createDirectories(this.playersDir);
        Path corruptFile = this.playersDir.resolve("bad.json");
        Files.writeString(corruptFile, "THIS IS NOT JSON");

        Collection<Player> players = PlayerFileRepository.loadAll();
        assertTrue(players.isEmpty());

        Path corruptDir = this.playersDir.resolve("corrupt");
        assertTrue(Files.exists(corruptDir), "Corrupt directory should have been created");
        try (var stream = Files.list(corruptDir)) {
            assertTrue(stream.findAny().isPresent(), "Corrupt file should have been moved to corrupt directory");
        }
    }
}
