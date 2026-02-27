package me.george.testing.entity.player;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the management of players.
 * Created by George K. on 4/18/2024.
 */
public class PlayerManagerTest {

    private Player testPlayer;

    @BeforeEach
    public void setup() {
        testPlayer = PlayerManager.create("test_player");
    }

    @AfterEach
    public void cleanup() {
        PlayerManager.getPlayers().forEach(p -> PlayerManager.removePlayer(p.getUuid()));
    }

    @Test
    public void testCreatePlayer() {
        assertNotNull(testPlayer);
        assertNotNull(testPlayer.getName());
    }

    @Test
    public void testSavePlayer() {
        assertTrue(PlayerManager.save(testPlayer));
    }

    @Test
    public void testDeletePlayer() {
        assertTrue(PlayerManager.deleteData(testPlayer));
    }

    @Test
    public void testGetPlayer() {
        final UUID id = testPlayer.getUuid();
        assertEquals(testPlayer, PlayerManager.getPlayer(id));
    }

    @Test
    public void testRemovePlayer() {
        final UUID id = testPlayer.getUuid();
        PlayerManager.removePlayer(id);
        assertNull(PlayerManager.getPlayer(id));
    }

    @Test
    public void testGetPlayers() {
        final Collection<Player> players = PlayerManager.getPlayers();
        assertTrue(players.contains(testPlayer));
    }

    @Test
    public void testIsValidPlayerName() {
        assertTrue(PlayerManager.isValidPlayerName("player"));
        assertTrue(PlayerManager.isValidPlayerName("asdf123"));
        assertFalse(PlayerManager.isValidPlayerName(""));
        assertFalse(PlayerManager.isValidPlayerName("12"));
        assertFalse(PlayerManager.isValidPlayerName("Name With Spaces"));
        assertFalse(PlayerManager.isValidPlayerName("NameWith!Invalid@Chars"));
    }
}
