package me.george.testing.instanced.battle;

import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.instanced.battle.damage.DamageModifier;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests battles playing out: turn order, damage and finishing.
 * Created by George K. on 11/27/2026.
 */
public class BattleSimulationTest {
    private final Pokemon PIKACHU = createPokemon(PokemonType.PIKACHU, 20);
    private final Pokemon SNORLAX = createPokemon(PokemonType.SNORLAX, 20);

    private final Party MOCK_PARTY_1 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("SIM_PLAYER1"))), Set.of(PIKACHU));
    private final Party MOCK_PARTY_2 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("SIM_PLAYER2"))), Set.of(SNORLAX));
    private final Battle MOCK_BATTLE = BattleManager.createBattle(Arrays.asList(MOCK_PARTY_1, MOCK_PARTY_2));

    @Test
    public void testFasterPokemonGoesFirst() {
        BattleManager.start(MOCK_BATTLE); // Plays the first turn.
        assertEquals(PIKACHU, MOCK_BATTLE.getPastTurns().peek().attacker()); // Base speed 90 against 30.

        MOCK_BATTLE.nextTurn();
        assertEquals(SNORLAX, MOCK_BATTLE.getCurrentTurn().attacker());
        assertEquals(1, MOCK_BATTLE.getRound());

        MOCK_BATTLE.nextTurn();
        assertEquals(2, MOCK_BATTLE.getRound());
    }

    @Test
    public void testTurnsDealDamage() {
        BattleManager.start(MOCK_BATTLE);
        Battle.Turn turn = MOCK_BATTLE.getCurrentTurn();
        assertNotNull(turn.move());
        assertTrue(turn.damage() > 0);
        assertEquals(SNORLAX.getHP() - turn.damage(), SNORLAX.getHealth());
    }

    @Test
    public void testSimulatedBattleFinishes() {
        BattleManager.start(MOCK_BATTLE);
        MOCK_BATTLE.simulate(500);
        assertTrue(MOCK_BATTLE.isFinished());
        assertFalse(BattleManager.getBattles().contains(MOCK_BATTLE));

        if (!MOCK_BATTLE.isDraw()) {
            Pokemon loser = MOCK_BATTLE.getWinner() == MOCK_PARTY_1 ? SNORLAX : PIKACHU;
            assertFalse(loser.isAlive());
        }
    }

    @Test
    public void testFaintedPokemonLoseTheirTurn() {
        BattleManager.start(MOCK_BATTLE); // Pikachu's turn.
        SNORLAX.kill(); // Faints before its turn.

        BattleManager.pause(MOCK_BATTLE);
        BattleManager.resume(MOCK_BATTLE);
        assertTrue(MOCK_BATTLE.isFinished());
        assertEquals(MOCK_PARTY_1, MOCK_BATTLE.getWinner());
        assertEquals(1, MOCK_BATTLE.getTotalTurns()); // Snorlax never played.
    }

    @Test
    public void testBattleDamageModifiers() {
        MOCK_BATTLE.getDamagePipeline().addModifier(DamageModifier.of("No Damage", calculation -> 0F)); // Only this battle.

        BattleManager.start(MOCK_BATTLE);
        MOCK_BATTLE.simulate(10);
        assertEquals(SNORLAX.getHP(), SNORLAX.getHealth());
        assertTrue(MOCK_BATTLE.isDraw());
    }

    @Test
    public void testStartingMoves() {
        assertEquals(List.of(MoveType.TACKLE, MoveType.THUNDER_SHOCK), PIKACHU.getMoves());
        assertEquals(List.of(MoveType.TACKLE), SNORLAX.getMoves()); // Tackle is its Normal move.

        assertTrue(PIKACHU.learnMove(MoveType.GUST));
        assertFalse(PIKACHU.learnMove(MoveType.GUST)); // Already knows it.
        assertTrue(PIKACHU.learnMove(MoveType.BITE));
        assertFalse(PIKACHU.learnMove(MoveType.LICK)); // Knows the most it can.
    }

    private static Pokemon createPokemon(PokemonType type, int level) {
        Pokemon pokemon = PokemonManager.create(type, null);
        while (pokemon.getLevel() < level)
            pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());

        return pokemon;
    }

    @AfterEach
    public void tearDown() {
        BattleManager.stopBattles();
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
