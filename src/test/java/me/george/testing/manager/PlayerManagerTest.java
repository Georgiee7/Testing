package me.george.testing.manager;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the management of parties.
 * Created by George K. on 4/18/2024.
 */
public class PlayerManagerTest {

    private final Player MOCK_PLAYER = PlayerManager.create("test_player");

    @Test
    public void testCreatePlayer() {
        assertNotNull(MOCK_PLAYER);
        assertNotNull(MOCK_PLAYER.getName());
    }

    @Test
    public void testSavePlayer() {
        assertTrue(PlayerManager.save(MOCK_PLAYER));
    }

    @Test
    public void testDeletePlayer() {
        assertTrue(PlayerManager.deleteData(MOCK_PLAYER));
    }

    @Test
    public void testGetPlayer() {
        final UUID id = MOCK_PLAYER.getUuid();
        assertEquals(MOCK_PLAYER, PlayerManager.getPlayer(id));
    }

    @Test
    public void testRemovePlayer() {
        final UUID id = MOCK_PLAYER.getUuid();

        PlayerManager.removePlayer(id);
        assertNull(PlayerManager.getPlayer(id));
    }

    @Test
    public void testGetPlayers() {
        final Collection<Player> players = PlayerManager.getPlayers();
        assertTrue(players.contains(MOCK_PLAYER));
    }

    @Test
    public void testIsValidPlayerName() {
        assertTrue(PlayerManager.isValidPlayerName("player"));
        assertTrue(PlayerManager.isValidPlayerName("asdf123"));
        assertFalse(PlayerManager.isValidPlayerName(""));
        assertFalse(PlayerManager.isValidPlayerName("12"));
        assertFalse(PlayerManager.isValidPlayerName("Name_With_Spaces"));
        assertFalse(PlayerManager.isValidPlayerName("NameWith!Invalid@Chars"));
    }

    @AfterAll
    public static void tearDown() {
        PlayerManager.getPlayers().forEach(PlayerManager::deleteData);
    }
}
