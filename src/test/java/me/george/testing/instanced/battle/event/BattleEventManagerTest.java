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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests listening to and calling battle events.
 * Created by George K. on 11/27/2026.
 */
public class BattleEventManagerTest {
    private final List<RegisteredListener<?>> listeners = new ArrayList<>();

    private final Pokemon MOCK_POKEMON_1 = PokemonManager.create(PokemonType.CHARMANDER, null);
    private final Pokemon MOCK_POKEMON_2 = PokemonManager.create(PokemonType.SQUIRTLE, null);
    private final Party MOCK_PARTY_1 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("EVENT_PLAYER1"))), Set.of(MOCK_POKEMON_1));
    private final Party MOCK_PARTY_2 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("EVENT_PLAYER2"))), Set.of(MOCK_POKEMON_2));
    private final Battle MOCK_BATTLE = BattleManager.createBattle(Arrays.asList(MOCK_PARTY_1, MOCK_PARTY_2));

    @Test
    public void testListenerHearsEvent() {
        AtomicInteger turns = new AtomicInteger();
        listen(TurnStartEvent.class, EventPriority.NORMAL, false, event -> turns.incrementAndGet());

        BattleManager.start(MOCK_BATTLE);
        assertEquals(1, turns.get());
    }

    @Test
    public void testPriorityOrder() {
        List<EventPriority> heard = new ArrayList<>();
        listen(BattleStartEvent.class, EventPriority.MONITOR, false, event -> heard.add(EventPriority.MONITOR));
        listen(BattleStartEvent.class, EventPriority.HIGH, false, event -> heard.add(EventPriority.HIGH));
        listen(BattleStartEvent.class, EventPriority.LOWEST, false, event -> heard.add(EventPriority.LOWEST));
        listen(BattleStartEvent.class, EventPriority.NORMAL, false, event -> heard.add(EventPriority.NORMAL));

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertEquals(List.of(EventPriority.LOWEST, EventPriority.NORMAL, EventPriority.HIGH, EventPriority.MONITOR), heard);
    }

    @Test
    public void testIgnoreCancelled() {
        AtomicInteger heard = new AtomicInteger();
        listen(BattleStartEvent.class, EventPriority.LOW, false, event -> event.setCancelled(true));
        listen(BattleStartEvent.class, EventPriority.HIGH, true, event -> heard.incrementAndGet()); // Skips cancelled events.
        listen(BattleStartEvent.class, EventPriority.MONITOR, false, event -> heard.incrementAndGet());

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertEquals(1, heard.get());
    }

    @Test
    public void testParentEventHearsEverything() {
        List<Class<?>> heard = new ArrayList<>();
        listen(BattleEvent.class, EventPriority.MONITOR, false, event -> heard.add(event.getClass()));

        BattleManager.start(MOCK_BATTLE);
        assertEquals(BattleStartEvent.class, heard.get(0));
        assertTrue(heard.contains(TurnStartEvent.class));
    }

    @Test
    public void testUnlisten() {
        AtomicInteger heard = new AtomicInteger();
        RegisteredListener<BattleStartEvent> listener = BattleEventManager.listen(BattleStartEvent.class, event -> heard.incrementAndGet());
        BattleEventManager.unlisten(listener);

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertEquals(0, heard.get());
    }

    @Test
    public void testBrokenListenerDoesntStopOthers() {
        AtomicInteger heard = new AtomicInteger();
        listen(BattleStartEvent.class, EventPriority.LOW, false, event -> {
            throw new IllegalStateException("Broken listener, on purpose.");
        });
        listen(BattleStartEvent.class, EventPriority.HIGH, false, event -> heard.incrementAndGet());

        BattleEventManager.call(new BattleStartEvent(MOCK_BATTLE));
        assertEquals(1, heard.get());
    }

    @Test
    public void testCancellingBattleStart() {
        listen(BattleStartEvent.class, EventPriority.NORMAL, false, event -> event.setCancelled(true));

        BattleManager.start(MOCK_BATTLE);
        assertFalse(MOCK_BATTLE.hasStarted());
    }

    @Test
    public void testCancellingTurns() {
        listen(TurnStartEvent.class, EventPriority.NORMAL, false, event -> event.setCancelled(true)); // Nobody gets to attack.

        BattleManager.start(MOCK_BATTLE);
        MOCK_BATTLE.simulate(20);
        assertEquals(MOCK_POKEMON_1.getHP(), MOCK_POKEMON_1.getHealth());
        assertEquals(MOCK_POKEMON_2.getHP(), MOCK_POKEMON_2.getHealth());
        assertTrue(MOCK_BATTLE.isDraw());
    }

    @Test
    public void testChangingDamage() {
        listen(DamageEvent.class, EventPriority.HIGHEST, false, event -> event.setDamage(event.getDefender().getHealth())); // Every hit is a knockout.

        AtomicInteger faints = new AtomicInteger();
        listen(FaintEvent.class, EventPriority.MONITOR, false, event -> faints.incrementAndGet());

        AtomicInteger finishes = new AtomicInteger();
        listen(BattleFinishEvent.class, EventPriority.MONITOR, false, event -> finishes.incrementAndGet());

        BattleManager.start(MOCK_BATTLE);
        MOCK_BATTLE.simulate(20);
        assertEquals(1, faints.get());
        assertEquals(1, finishes.get());
        assertFalse(MOCK_BATTLE.isDraw());
    }

    private <T extends BattleEvent> void listen(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<T> handler) {
        this.listeners.add(BattleEventManager.listen(eventClass, priority, ignoreCancelled, handler));
    }

    @AfterEach
    public void tearDown() {
        this.listeners.forEach(BattleEventManager::unlisten);
        BattleManager.stopBattles();
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
