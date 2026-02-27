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
 * Tests for the file-backed repositories: save/load round-trip and corrupt-file quarantine.
 * All I/O goes to {@code target/test-data} (set via the {@code data.dir} system property in
 * pom.xml) so it never touches the real {@code data/} directory.
 * Created by George K. on 2/26/2026.
 */
public class FileRepositoryTest {

    @BeforeEach
    public void setup() throws Exception {
        PlayerManager.clearCache();
        PokemonManager.clearCache();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
    }

    @AfterEach
    public void cleanup() throws Exception {
        PlayerManager.clearCache();
        PokemonManager.clearCache();
        wipe(PlayerFileRepository.getDir());
        wipe(PokemonFileRepository.getDir());
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
    public void testPlayerSaveLoad() {
        Player player = PlayerManager.create("saveloaduser");
        PlayerFileRepository.save(player);

        Collection<Player> loaded = PlayerFileRepository.loadAll();
        assertTrue(loaded.stream().anyMatch(p -> p.getUuid().equals(player.getUuid())));
    }

    @Test
    public void testPlayerRemove() {
        Player player = PlayerManager.create("removeuser");
        PlayerFileRepository.save(player);

        PlayerFileRepository.remove(player);

        Collection<Player> loaded = PlayerFileRepository.loadAll();
        assertFalse(loaded.stream().anyMatch(p -> p.getUuid().equals(player.getUuid())));
    }

    @Test
    public void testPokemonSaveLoad() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonFileRepository.save(pokemon);

        Collection<Pokemon> loaded = PokemonFileRepository.loadAll();
        assertTrue(loaded.stream().anyMatch(p -> p.getUuid().equals(pokemon.getUuid())));
    }

    @Test
    public void testPokemonRemove() {
        Pokemon pokemon = PokemonManager.create(PokemonType.CHARMANDER, null);
        PokemonFileRepository.save(pokemon);

        PokemonFileRepository.remove(pokemon);

        Collection<Pokemon> loaded = PokemonFileRepository.loadAll();
        assertFalse(loaded.stream().anyMatch(p -> p.getUuid().equals(pokemon.getUuid())));
    }

    @Test
    public void testCorruptFileIsQuarantined() throws Exception {
        Files.createDirectories(PlayerFileRepository.getDir());
        Files.writeString(PlayerFileRepository.getDir().resolve("bad.json"), "THIS IS NOT JSON");

        Collection<Player> players = PlayerFileRepository.loadAll();

        assertTrue(players.isEmpty(), "Corrupt file should not produce a player");

        Path quarantine = PlayerFileRepository.getDir().resolve("corrupt");
        assertTrue(Files.exists(quarantine), "Quarantine directory should be created");
        try (var stream = Files.list(quarantine)) {
            assertTrue(stream.findAny().isPresent(), "Corrupt file should be moved to quarantine");
        }
    }

    @Test
    public void testInvalidPlayerFileIsQuarantined() throws Exception {
        // Valid JSON but missing required fields → fails isValid().
        Files.createDirectories(PlayerFileRepository.getDir());
        Files.writeString(PlayerFileRepository.getDir().resolve("invalid.json"),
                "{\"uuid\":null,\"name\":null}");

        Collection<Player> players = PlayerFileRepository.loadAll();
        assertTrue(players.isEmpty(), "Invalid player (null uuid/name) should not be returned");

        Path quarantine = PlayerFileRepository.getDir().resolve("corrupt");
        assertTrue(Files.exists(quarantine));
    }
}
