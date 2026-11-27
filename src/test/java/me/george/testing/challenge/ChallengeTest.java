package me.george.testing.challenge;

import me.george.testing.challenge.challenges.DealDamageChallenge;
import me.george.testing.challenge.challenges.KnockOutChallenge;
import me.george.testing.challenge.challenges.WinBattlesChallenge;
import me.george.testing.challenge.composite.AllChallenge;
import me.george.testing.challenge.composite.AnyChallenge;
import me.george.testing.challenge.composite.SequenceChallenge;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.entity.pokemon.spec.PokemonSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests challenges counting actions, combining into trees, and saving and restoring their progress.
 * Created by George K. on 11/27/2026.
 */
public class ChallengeTest {
    private final Player MOCK_PLAYER = PlayerManager.create("CHAL_PLAYER");
    private final Pokemon CHARMANDER = PokemonManager.create(PokemonType.CHARMANDER, null);
    private final Pokemon PIKACHU = PokemonManager.create(PokemonType.PIKACHU, null);

    @Test
    public void testCountingAndCallbacks() {
        AtomicInteger activations = new AtomicInteger();
        AtomicInteger completions = new AtomicInteger();
        WinBattlesChallenge challenge = new WinBattlesChallenge(2)
                .onActivate(activations::incrementAndGet)
                .onComplete(completions::incrementAndGet);

        challenge.activate();
        challenge.activate();
        assertEquals(1, activations.get());

        assertFalse(challenge.offer(knockOut(CHARMANDER))); // Not an action it counts.
        assertTrue(challenge.offer(win()));
        assertTrue(challenge.offer(win()));
        assertFalse(challenge.offer(win())); // Already complete.
        assertEquals(2, challenge.getProgress());
        assertEquals(1, completions.get());
    }

    @Test
    public void testAllOf() {
        AtomicInteger completions = new AtomicInteger();
        Challenge challenge = new AllChallenge(new WinBattlesChallenge(1), new WinBattlesChallenge(1)).onComplete(completions::incrementAndGet);
        challenge.activate();

        challenge.offer(win());
        assertEquals(1, challenge.getProgress()); // One win only counts for one of them.
        assertFalse(challenge.isComplete());

        challenge.offer(win());
        assertTrue(challenge.isComplete());
        assertEquals(1, completions.get());
    }

    @Test
    public void testAnyOf() {
        AtomicInteger winCompletions = new AtomicInteger();
        AtomicInteger knockOutCompletions = new AtomicInteger();
        Challenge challenge = new AnyChallenge(
                new WinBattlesChallenge(2).onComplete(winCompletions::incrementAndGet),
                new KnockOutChallenge(1, PokemonSpec.parse("*[element=FIRE]")).onComplete(knockOutCompletions::incrementAndGet));
        challenge.activate();

        challenge.offer(win());
        challenge.offer(knockOut(PIKACHU)); // Not a Fire type.
        assertFalse(challenge.isComplete());

        challenge.offer(knockOut(CHARMANDER));
        assertTrue(challenge.isComplete());

        challenge.offer(win());
        assertEquals(0, winCompletions.get()); // The other part never completes once one has.
        assertEquals(1, knockOutCompletions.get());
    }

    @Test
    public void testSequence() {
        AtomicInteger knockOutActivations = new AtomicInteger();
        SequenceChallenge challenge = new SequenceChallenge(new WinBattlesChallenge(1), new KnockOutChallenge(2).onActivate(knockOutActivations::incrementAndGet));
        challenge.activate();
        assertEquals(0, knockOutActivations.get());
        assertFalse(challenge.listensTo(ActionType.KNOCK_OUT)); // Only the current part counts.

        assertFalse(challenge.offer(knockOut(CHARMANDER)));
        challenge.offer(win());
        assertEquals(1, knockOutActivations.get());
        assertTrue(challenge.listensTo(ActionType.KNOCK_OUT));

        challenge.offer(knockOut(CHARMANDER));
        challenge.offer(knockOut(PIKACHU));
        assertTrue(challenge.isComplete());
    }

    @Test
    public void testSavingAndRestoring() {
        AtomicInteger completions = new AtomicInteger();
        SequenceChallenge saved = buildTree(completions);
        saved.activate();
        saved.offer(win());
        saved.offer(knockOut(CHARMANDER));
        assertEquals(1, completions.get());

        List<Integer> state = new ArrayList<>();
        saved.saveState(state);

        AtomicInteger restoredCompletions = new AtomicInteger();
        SequenceChallenge restored = buildTree(restoredCompletions);
        restored.restoreState(state.iterator());
        restored.activate();
        assertEquals(0, restoredCompletions.get()); // The part already done doesn't complete again.
        assertInstanceOf(KnockOutChallenge.class, restored.getCurrent());
        assertEquals(1, restored.getProgress());

        restored.offer(knockOut(PIKACHU));
        restored.offer(knockOut(PIKACHU));
        assertTrue(restored.isComplete());
    }

    @Test
    public void testDescriptions() {
        assertEquals("Knock out 5 Fire Pokémon.", new KnockOutChallenge(5, PokemonSpec.parse("*[element=FIRE]")).getDescription());

        WinBattlesChallenge wins = new WinBattlesChallenge(3).describedAs("Win {left} more battles ({progress}/{goal}).");
        wins.offer(win());
        assertEquals("Win 2 more battles (1/3).", wins.getDescription());

        assertEquals("Win a battle OR Deal 100 damage with Fire moves.", new AnyChallenge(new WinBattlesChallenge(1), new DealDamageChallenge(100, Element.FIRE)).getDescription());
        assertEquals("Win a battle, Knock out 2 Pokémon.", new AllChallenge(new WinBattlesChallenge(1), new KnockOutChallenge(2)).getDescription());
    }

    @Test
    public void testDamageByElement() {
        DealDamageChallenge challenge = new DealDamageChallenge(50, Element.FIRE);
        challenge.offer(damage(MoveType.EMBER, 30));
        challenge.offer(damage(MoveType.TACKLE, 30)); // Not a Fire move.
        assertEquals(30, challenge.getProgress());

        challenge.offer(damage(MoveType.EMBER, 30));
        assertEquals(50, challenge.getProgress());
        assertTrue(challenge.isComplete());
    }

    private static SequenceChallenge buildTree(AtomicInteger completions) {
        return new SequenceChallenge(new WinBattlesChallenge(1).onComplete(completions::incrementAndGet), new KnockOutChallenge(3));
    }

    private ChallengeAction win() {
        return new ChallengeAction(ActionType.WIN_BATTLE, MOCK_PLAYER, null, null, null, null, 1);
    }

    private ChallengeAction knockOut(Pokemon target) {
        return new ChallengeAction(ActionType.KNOCK_OUT, MOCK_PLAYER, null, null, target, null, 1);
    }

    private ChallengeAction damage(MoveType move, int amount) {
        return new ChallengeAction(ActionType.DEAL_DAMAGE, MOCK_PLAYER, null, null, null, move, amount);
    }

    @AfterEach
    public void tearDown() {
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
