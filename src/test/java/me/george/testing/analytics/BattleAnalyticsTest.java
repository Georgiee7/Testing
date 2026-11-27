package me.george.testing.analytics;

import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.entity.pokemon.PokemonType;
import me.george.testing.instanced.battle.Battle;
import me.george.testing.instanced.battle.BattleManager;
import me.george.testing.parties.Party;
import me.george.testing.parties.PartyManager;
import me.george.testing.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests battles being tracked by analytics, and analytics doing nothing while it's off.
 * Created by George K. on 11/27/2026.
 */
public class BattleAnalyticsTest {
    private final List<String> rows = new CopyOnWriteArrayList<>();

    private final Pokemon MOCK_POKEMON_1 = PokemonManager.create(PokemonType.CHARMANDER, null);
    private final Pokemon MOCK_POKEMON_2 = PokemonManager.create(PokemonType.SQUIRTLE, null);
    private final Party MOCK_PARTY_1 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("STATS_PLAYER1"))), Set.of(MOCK_POKEMON_1));
    private final Party MOCK_PARTY_2 = PartyManager.createParty(new ArrayList<>(List.of(PlayerManager.create("STATS_PLAYER2"))), Set.of(MOCK_POKEMON_2));
    private final Battle MOCK_BATTLE = BattleManager.createBattle(Arrays.asList(MOCK_PARTY_1, MOCK_PARTY_2));

    @Test
    public void testBattlesAreTracked() {
        AnalyticsManager.enable((batchId, batch) -> this.rows.addAll(batch));
        BattleManager.start(MOCK_BATTLE);
        MOCK_BATTLE.simulate(500);
        AnalyticsManager.disable(); // Sends what's still queued.

        assertTrue(this.rows.get(0).contains("\"event\":\"battle_start\""));
        assertTrue(this.rows.stream().anyMatch(row -> row.contains("\"event\":\"damage\"")));
        assertTrue(Utils.getLast(this.rows).contains("\"event\":\"battle_finish\""));
    }

    @Test
    public void testOffDoesNothing() {
        assertFalse(AnalyticsManager.isEnabled());
        assertSame(AnalyticsRow.NONE, AnalyticsManager.track("damage").with("damage", 5));

        BattleManager.start(MOCK_BATTLE);
        MOCK_BATTLE.simulate(500);
        assertTrue(this.rows.isEmpty());
    }

    @AfterEach
    public void tearDown() {
        AnalyticsManager.disable();
        BattleManager.stopBattles();
        PokemonManager.getPokemons().forEach(PokemonManager::removePokemon);
    }
}
