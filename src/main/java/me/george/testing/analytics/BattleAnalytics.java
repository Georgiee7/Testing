package me.george.testing.analytics;

import me.george.testing.instanced.battle.Battle;
import me.george.testing.instanced.battle.damage.DamageCalculation;
import me.george.testing.instanced.battle.damage.DefaultDamageModifier;
import me.george.testing.instanced.battle.event.BattleFinishEvent;
import me.george.testing.instanced.battle.event.BattleHandler;
import me.george.testing.instanced.battle.event.BattleStartEvent;
import me.george.testing.instanced.battle.event.DamageEvent;
import me.george.testing.instanced.battle.event.EventPriority;
import me.george.testing.instanced.battle.event.FaintEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks what happens in battles: when they start and finish, the damage every hit deals and every Pokémon fainting.
 * It hears the events last, at monitor priority, so it tracks what really happened.
 * Created by George K. on 11/27/2026.
 */
class BattleAnalytics {
    private final Map<UUID, Long> startTimes = new ConcurrentHashMap<>(); // When each battle started, by its id, to track how long it took.

    @BattleHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onStart(BattleStartEvent event) {
        Battle battle = event.getBattle();
        this.startTimes.put(battle.getUuid(), System.currentTimeMillis());
        AnalyticsManager.track("battle_start")
                .with("battle", battle.getUuid())
                .with("parties", battle.getParties().size())
                .send();
    }

    @BattleHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDamage(DamageEvent event) {
        DamageCalculation calculation = event.getCalculation();
        AnalyticsManager.track("damage")
                .with("battle", event.getBattle().getUuid())
                .with("round", event.getBattle().getRound())
                .with("attacker", event.getAttacker().getType())
                .with("defender", event.getDefender().getType())
                .with("move", calculation.getMove().getType())
                .with("damage", event.getDamage())
                .with("critical", calculation.isCritical())
                .with("effectiveness", calculation.getMultiplier(DefaultDamageModifier.TYPE_EFFECTIVENESS))
                .send();
    }

    @BattleHandler(priority = EventPriority.MONITOR)
    private void onFaint(FaintEvent event) {
        AnalyticsManager.track("faint")
                .with("battle", event.getBattle().getUuid())
                .with("round", event.getBattle().getRound())
                .with("pokemon", event.getPokemon().getType())
                .with("cause", event.getCause() != null ? event.getCause().getType() : null)
                .send();
    }

    @BattleHandler(priority = EventPriority.MONITOR)
    private void onFinish(BattleFinishEvent event) {
        Battle battle = event.getBattle();
        Long startTime = this.startTimes.remove(battle.getUuid());
        AnalyticsManager.track("battle_finish")
                .with("battle", battle.getUuid())
                .with("rounds", battle.getRound())
                .with("turns", battle.getPastTurns().size())
                .with("draw", event.isDraw())
                .with("winner", !event.isDraw() ? event.getWinner().getLeader().getName() : null)
                .with("duration_ms", startTime != null ? System.currentTimeMillis() - startTime : null)
                .send();
    }
}
