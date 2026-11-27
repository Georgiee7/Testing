package me.george.testing.instanced.battle;

import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testStoppingAFinishedBattleDoesNothing() {
        BattleManager.finish(MOCK_BATTLE);
        assertDoesNotThrow(() -> BattleManager.stop(MOCK_BATTLE));
    }

    @Test
    public void testFinishedBattleIsUncached() {
        assertTrue(BattleManager.getBattles().contains(MOCK_BATTLE));
        BattleManager.finish(MOCK_BATTLE);
        assertFalse(BattleManager.getBattles().contains(MOCK_BATTLE));
    }

    @Test
    public void testWinner() {
        BattleManager.finish(MOCK_BATTLE); // Player 1's party has 2 alive combatants, player 2's has 1.
        assertEquals(MOCK_PLAYER_1_PARTY, MOCK_BATTLE.getWinner());
        assertFalse(MOCK_BATTLE.isDraw());
    }

    @Test
    public void testDraw() {
        Party party1 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("DRAW_PLAYER1"))), Set.of(PokemonManager.create(PokemonType.CHARMANDER, null)));
        Party party2 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("DRAW_PLAYER2"))), Set.of(PokemonManager.create(PokemonType.CHARMANDER, null)));

        Battle battle = BattleManager.createBattle(Arrays.asList(party1, party2));
        BattleManager.finish(battle);
        assertTrue(battle.isDraw());
        assertNull(battle.getWinner());
    }

    @Test
    public void testBattleEndsWhenAPartyIsDefeated() {
        BattleManager.start(MOCK_BATTLE);
        MOCK_PLAYER_2_TEAM.forEach(Combatant::kill);

        BattleManager.pause(MOCK_BATTLE);
        BattleManager.resume(MOCK_BATTLE); // Resuming processes the next turn, which notices player 2's party is out.
        assertTrue(MOCK_BATTLE.isFinished());
        assertEquals(MOCK_PLAYER_1_PARTY, MOCK_BATTLE.getWinner());
    }

    @AfterAll
    public static void tearDown() {
        BattleManager.stopBattles();
    }
}
