package me.george.testing.challenge;

import me.george.testing.challenge.challenges.DealDamageChallenge;
import me.george.testing.challenge.challenges.KnockOutChallenge;
import me.george.testing.challenge.challenges.WinBattlesChallenge;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.entity.pokemon.spec.PokemonSpec;
import me.george.testing.instanced.battle.Battle;
import me.george.testing.instanced.battle.BattleManager;
import me.george.testing.instanced.battle.event.BattleEventManager;
import me.george.testing.instanced.battle.event.DamageEvent;
import me.george.testing.instanced.battle.event.EventPriority;
import me.george.testing.instanced.battle.event.RegisteredListener;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests challenges progressing from what players' Pokémon do in battles.
 * Created by George K. on 11/27/2026.
 */
public class ChallengeManagerTest {
    private final ChallengeManager manager = new ChallengeManager();

    private final Player MOCK_PLAYER = PlayerManager.create("CHAL_TRAINER");
    private final Player MOCK_RIVAL = PlayerManager.create("CHAL_RIVAL");
    private final Pokemon CHARMANDER = createOwnedPokemon(PokemonType.CHARMANDER, MOCK_PLAYER);
    private final Pokemon SQUIRTLE = createOwnedPokemon(PokemonType.SQUIRTLE, MOCK_RIVAL);
    private final Party MOCK_PARTY_1 = PartyManager.createParty(new ArrayList<>(List.of(MOCK_PLAYER)), Set.of(CHARMANDER));
    private final Party MOCK_PARTY_2 = PartyManager.createParty(new ArrayList<>(List.of(MOCK_RIVAL)), Set.of(SQUIRTLE));
    private final Battle MOCK_BATTLE = BattleManager.createBattle(Arrays.asList(MOCK_PARTY_1, MOCK_PARTY_2));

    private RegisteredListener<DamageEvent> oneHitKnockOuts;

    @BeforeEach
    public void setUp() {
        this.manager.onStart();
        this.oneHitKnockOuts = BattleEventManager.listen(DamageEvent.class, EventPriority.HIGHEST, event -> event.setDamage(event.getDefender().getHealth()));
    }

    @Test
    public void testChallengesProgressInBattle() {
        WinBattlesChallenge playerWin = new WinBattlesChallenge(1);
        KnockOutChallenge playerKnockOut = new KnockOutChallenge(1, PokemonSpec.parse("*[element=WATER]"));
        DealDamageChallenge playerDamage = new DealDamageChallenge(1000);
        WinBattlesChallenge rivalWin = new WinBattlesChallenge(1);
        ChallengeManager.give(MOCK_PLAYER, playerWin);
        ChallengeManager.give(MOCK_PLAYER, playerKnockOut);
        ChallengeManager.give(MOCK_PLAYER, playerDamage);
        ChallengeManager.give(MOCK_RIVAL, rivalWin);

        BattleManager.start(MOCK_BATTLE); // Charmander is faster, so it knocks Squirtle out with the first hit.
        assertTrue(MOCK_BATTLE.isFinished());
        assertEquals(MOCK_PARTY_1, MOCK_BATTLE.getWinner());

        assertTrue(playerWin.isComplete());
        assertTrue(playerKnockOut.isComplete());
        assertEquals(SQUIRTLE.getHP(), playerDamage.getProgress()); // The hit dealt all of Squirtle's health.
        assertFalse(rivalWin.isComplete());
        assertEquals(3, ChallengeManager.getChallenges(MOCK_PLAYER).size());
    }

    @Test
    public void testStoppingStopsListening() {
        WinBattlesChallenge playerWin = new WinBattlesChallenge(1);
        ChallengeManager.give(MOCK_PLAYER, playerWin);
        this.manager.onStop();

        BattleManager.start(MOCK_BATTLE);
        assertFalse(playerWin.isComplete());
        assertTrue(ChallengeManager.getChallenges(MOCK_PLAYER).isEmpty());
    }

    // At level 30, so Charmander is always faster. At level 1 their speeds can be the same, which makes the turn order random.
    private static Pokemon createOwnedPokemon(PokemonType type, Player owner) {
        Pokemon pokemon = PokemonManager.create(type, null);
        while (pokemon.getLevel() < 30)
            pokemon.addExp(pokemon.getNeededExp() - pokemon.getExp());

        PokemonManager.setPokemonOwner(pokemon, owner);
        return pokemon;
    }

    @AfterEach
    public void tearDown() {
        this.manager.onStop();
        BattleEventManager.unlisten(this.oneHitKnockOuts);
        BattleManager.stopBattles();
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
