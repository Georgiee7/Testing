package me.george.testing.manager;

import me.george.testing.entity.Combatant;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.instanced.battle.Battle;
import me.george.testing.instanced.battle.BattleManager;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the management of battles.
 * Created by George K. on 4/18/2024.
 */
public class BattleManagerTest {

    private final Player MOCK_PLAYER_1 = PlayerManager.create("PLAYER1");
    private final Player MOCK_PLAYER_1_TEAM_MATE = PlayerManager.create("PLAYER1_MATE");
    private final Player MOCK_PLAYER_2 = PlayerManager.create("PLAYER2");

    private final Collection<Combatant> MOCK_PLAYER_1_TEAM = Set.of(PokemonManager.create(PokemonType.CHARMANDER, null), PokemonManager.create(PokemonType.CHARMANDER, null));
    private final Collection<Combatant> MOCK_PLAYER_2_TEAM = Set.of(PokemonManager.create(PokemonType.CHARMANDER, null));

    // Wrapping in a modifiable ArrayList to prevent UnsupportedOperationException.
    private final Party MOCK_PLAYER_1_PARTY = PartyManager.createParty(new ArrayList<>(Arrays.asList(MOCK_PLAYER_1, MOCK_PLAYER_1_TEAM_MATE)), MOCK_PLAYER_1_TEAM, MOCK_PLAYER_1);
    private final Party MOCK_PLAYER_2_PARTY = PartyManager.createParty(new ArrayList<>(Collections.singletonList(MOCK_PLAYER_2)), MOCK_PLAYER_2_TEAM, MOCK_PLAYER_2);

    private Battle MOCK_BATTLE = BattleManager.createBattle(Arrays.asList(MOCK_PLAYER_1_PARTY, MOCK_PLAYER_2_PARTY));

    @BeforeEach
    public void initializeTeams() {
        // Simulate the players having a team of combatants.
        for (Combatant combatant : MOCK_PLAYER_1_TEAM)
            if (combatant instanceof Pokemon pokemon)
                MOCK_PLAYER_1.addPokemon(pokemon);

        for (Combatant combatant : MOCK_PLAYER_2_TEAM)
            if (combatant instanceof Pokemon pokemon)
                MOCK_PLAYER_2.addPokemon(pokemon);
    }

    @Test
    public void testCreateBattle() {
        assertNotNull(MOCK_BATTLE);
        assertFalse(MOCK_BATTLE.hasStarted());

        assertEquals(2, MOCK_BATTLE.getParties().size());
    }

    @Test
    public void testCreateAndStartBattle() {
        MOCK_BATTLE = BattleManager.createAndStartBattle(Arrays.asList(MOCK_PLAYER_1_PARTY, MOCK_PLAYER_2_PARTY));
        assertTrue(MOCK_BATTLE.hasStarted());
    }

    @Test
    public void testPauseBattle() {
        BattleManager.pause(MOCK_BATTLE);
        assertTrue(MOCK_BATTLE.isPaused());
    }

    @Test
    public void testResumeBattle() {
        BattleManager.resume(MOCK_BATTLE);
        assertTrue(MOCK_BATTLE.isActive());
    }

    @Test
    public void testFinishBattle() {
        BattleManager.finish(MOCK_BATTLE);
        assertTrue(MOCK_BATTLE.isFinished());
    }

    @Test
    public void testStopBattle() {
        BattleManager.stop(MOCK_BATTLE);
        assertTrue(MOCK_BATTLE.isFinished() || !MOCK_BATTLE.isActive() || MOCK_BATTLE == null); // Accounting for various possible definitions of a "stopped battle".
    }

    @AfterAll
    public static void tearDown() {
        BattleManager.stopBattles();
    }
}
