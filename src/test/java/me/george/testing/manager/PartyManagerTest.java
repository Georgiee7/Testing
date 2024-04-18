package me.george.testing.manager;

import me.george.testing.entity.Combatant;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the management of parties.
 * Created by George K. on 4/18/2024.
 */
public class PartyManagerTest {

    private final Player MOCK_PLAYER_1 = PlayerManager.create("PLAYER1");
    private final Player MOCK_PLAYER_1_TEAM_MATE = PlayerManager.create("PLAYER1_MATE");
    private final Player MOCK_PLAYER_2 = PlayerManager.create("PLAYER2");

    private final Player MOCK_INVITED_PLAYER = PlayerManager.create("INVITED_PLAYER");

    private final Collection<Combatant> MOCK_PLAYER_1_TEAM = Set.of(PokemonManager.create(PokemonType.CHARMANDER, null), PokemonManager.create(PokemonType.CHARMANDER, null));
    private final Collection<Combatant> MOCK_PLAYER_2_TEAM = Set.of(PokemonManager.create(PokemonType.CHARMANDER, null));

    // Wrapping in a modifiable ArrayList to prevent UnsupportedOperationException.
    private final Party MOCK_PLAYER_1_PARTY = PartyManager.createParty(new ArrayList<>(Arrays.asList(MOCK_PLAYER_1, MOCK_PLAYER_1_TEAM_MATE)), MOCK_PLAYER_1_TEAM, MOCK_PLAYER_1);
    private final Party MOCK_PLAYER_2_PARTY = PartyManager.createParty(new ArrayList<>(Collections.singletonList(MOCK_PLAYER_2)), MOCK_PLAYER_2_TEAM, MOCK_PLAYER_2);

    @Test
    public void testCreateParty() {
        assertNotNull(MOCK_PLAYER_1_PARTY);
        assertNotNull(MOCK_PLAYER_2_PARTY);

        assertEquals(4, MOCK_PLAYER_1_PARTY.getSize());
        assertEquals(2, MOCK_PLAYER_2_PARTY.getSize());

        assertTrue(MOCK_PLAYER_1_PARTY.isMember(MOCK_PLAYER_1));
        assertTrue(MOCK_PLAYER_1_PARTY.isMember(MOCK_PLAYER_1_TEAM_MATE));
        assertTrue(MOCK_PLAYER_2_PARTY.isMember(MOCK_PLAYER_2));

        assertTrue(MOCK_PLAYER_1_PARTY.isLeader(MOCK_PLAYER_1));
        assertTrue(MOCK_PLAYER_2_PARTY.isLeader(MOCK_PLAYER_2));
        assertEquals(MOCK_PLAYER_1, MOCK_PLAYER_1_PARTY.getLeader());
        assertEquals(MOCK_PLAYER_2, MOCK_PLAYER_2_PARTY.getLeader());

        assertEquals(2, MOCK_PLAYER_1_PARTY.getCombatants().size());
        assertEquals(1, MOCK_PLAYER_2_PARTY.getCombatants().size());

        MOCK_PLAYER_1_PARTY.getCombatants().forEach(combatant -> assertInstanceOf(Combatant.class, combatant));
    }

    @Test
    public void testAddToParty() {
        PartyManager.addToParty(MOCK_INVITED_PLAYER, MOCK_PLAYER_1_PARTY);
        assertTrue(MOCK_PLAYER_1_PARTY.isMember(MOCK_INVITED_PLAYER));
        assertEquals(5, MOCK_PLAYER_1_PARTY.getSize());
    }

    @Test
    public void testRemoveFromParty() {
        PartyManager.removeFromParty(MOCK_PLAYER_1, MOCK_PLAYER_1_PARTY);
        assertFalse(MOCK_PLAYER_1_PARTY.isMember(MOCK_PLAYER_1));
        assertTrue(MOCK_PLAYER_1_PARTY.isLeader(MOCK_PLAYER_1_TEAM_MATE));
        assertEquals(3, MOCK_PLAYER_1_PARTY.getSize());
    }

    @Test
    public void testSetLeader() {
        PartyManager.setLeader(MOCK_PLAYER_1_PARTY, MOCK_PLAYER_1_TEAM_MATE);
        assertEquals(MOCK_PLAYER_1_TEAM_MATE, MOCK_PLAYER_1_PARTY.getLeader());
        assertFalse(MOCK_PLAYER_1_PARTY.isLeader(MOCK_PLAYER_1));
    }

    @Test
    public void testDisbandParty() {
        PartyManager.disbandParty(MOCK_PLAYER_1_PARTY);
        assertTrue(MOCK_PLAYER_1_PARTY.isEmpty());
    }

    @AfterAll
    public static void tearDown() {
       PartyManager.getParties().forEach(PartyManager::disbandParty);
       PlayerManager.getPlayers().forEach(PlayerManager::deleteData);
       PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
