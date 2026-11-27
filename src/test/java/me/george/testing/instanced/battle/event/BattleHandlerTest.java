package me.george.testing.instanced.battle.event;

import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.instanced.battle.Battle;
import me.george.testing.instanced.battle.BattleManager;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests listeners registered through their annotated handler methods.
 * Created by George K. on 11/27/2026.
 */
public class BattleHandlerTest {
    private final List<RegisteredListener<?>> listeners = new ArrayList<>();

    private final Pokemon MOCK_POKEMON_1 = PokemonManager.create(PokemonType.CHARMANDER, null);
    private final Pokemon MOCK_POKEMON_2 = PokemonManager.create(PokemonType.SQUIRTLE, null);
    private final Party MOCK_PARTY_1 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("HANDLER_PLAYER1"))), Set.of(MOCK_POKEMON_1));
    private final Party MOCK_PARTY_2 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("HANDLER_PLAYER2"))), Set.of(MOCK_POKEMON_2));
    private final Battle MOCK_BATTLE = BattleManager.createBattle(Arrays.asList(MOCK_PARTY_1, MOCK_PARTY_2));

    @Test
    public void testHandlersHearEvents() {
        OrderListener listener = new OrderListener();
        this.listeners.addAll(BattleEventManager.register(listener));
        assertEquals(3, this.listeners.size());

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertEquals(List.of("Cancel", "Monitor"), listener.heard); // The one ignoring cancelled events skipped it.
    }

    @Test
    public void testUnlistening() {
        OrderListener listener = new OrderListener();
        BattleEventManager.register(listener).forEach(BattleEventManager::unlisten);

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertTrue(listener.heard.isEmpty());
    }

    @Test
    public void testInheritedHandlers() {
        ChildListener listener = new ChildListener();
        this.listeners.addAll(BattleEventManager.register(listener));
        assertEquals(2, this.listeners.size()); // The overridden handler is only registered once.

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertEquals(List.of("Child start", "Parent any event"), listener.heard);
    }

    @Test
    public void testHandlerParentEvent() {
        ParentEventListener listener = new ParentEventListener();
        this.listeners.addAll(BattleEventManager.register(listener));

        BattleManager.start(MOCK_BATTLE);
        assertEquals(BattleStartEvent.class, listener.heard.get(0));
        assertTrue(listener.heard.contains(TurnStartEvent.class));
    }

    @Test
    public void testInvalidHandlers() {
        // Utils.verify asserts first, so this is an AssertionError when assertions are on (like in tests) and a GeneralException when they're off.
        assertThrows(Throwable.class, () -> BattleEventManager.register(new TwoParameterListener()));
        assertThrows(Throwable.class, () -> BattleEventManager.register(new NotAnEventListener()));
    }

    @AfterEach
    public void tearDown() {
        this.listeners.forEach(BattleEventManager::unlisten);
        BattleManager.stopBattles();
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }

    private static class OrderListener {
        private final List<String> heard = new ArrayList<>();

        @BattleHandler(priority = EventPriority.MONITOR)
        private void onMonitor(BattleStartEvent event) {
            this.heard.add("Monitor");
        }

        @BattleHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        private void onIgnoringCancelled(BattleStartEvent event) {
            this.heard.add("Ignoring cancelled");
        }

        @BattleHandler(priority = EventPriority.LOW)
        private void onCancel(BattleStartEvent event) {
            this.heard.add("Cancel");
            event.setCancelled(true);
        }
    }

    private static class ParentListener {
        protected final List<String> heard = new ArrayList<>();

        @BattleHandler
        public void onStart(BattleStartEvent event) {
            this.heard.add("Parent start");
        }

        @BattleHandler(priority = EventPriority.MONITOR)
        public void onAnyEvent(BattleEvent event) {
            this.heard.add("Parent any event");
        }
    }

    private static class ChildListener extends ParentListener {

        @Override
        @BattleHandler
        public void onStart(BattleStartEvent event) {
            this.heard.add("Child start");
        }
    }

    private static class ParentEventListener {
        private final List<Class<?>> heard = new ArrayList<>();

        @BattleHandler(priority = EventPriority.MONITOR)
        private void onEvent(BattleEvent event) {
            this.heard.add(event.getClass());
        }
    }

    private static class TwoParameterListener {

        @BattleHandler
        private void onStart(BattleStartEvent event, Battle battle) {
        }
    }

    private static class NotAnEventListener {

        @BattleHandler
        private void onStart(String event) {
        }
    }
}
