package me.george.testing.challenge;

import me.george.testing.entity.Entity;
import me.george.testing.entity.player.Player;
import me.george.testing.instanced.battle.event.BattleFinishEvent;
import me.george.testing.instanced.battle.event.BattleHandler;
import me.george.testing.instanced.battle.event.DamageEvent;
import me.george.testing.instanced.battle.event.EventPriority;
import me.george.testing.instanced.battle.event.FaintEvent;

/**
 * Turns battle events into challenge actions, for the players whose Pokémon did them.
 * It hears the events last, at monitor priority, once nothing can change them anymore.
 * Created by George K. on 11/27/2026.
 */
class ChallengeListener {

    @BattleHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDamage(DamageEvent event) {
        if (event.getDamage() > 0)
            ChallengeManager.offer(new ChallengeAction(ActionType.DEAL_DAMAGE, event.getAttacker().getOwner(), event.getBattle(),
                    event.getAttacker(), event.getDefender(), event.getCalculation().getMove().getType(), event.getDamage()));
    }

    @BattleHandler(priority = EventPriority.MONITOR)
    private void onFaint(FaintEvent event) {
        if (event.getCause() != null)
            ChallengeManager.offer(new ChallengeAction(ActionType.KNOCK_OUT, event.getCause().getOwner(), event.getBattle(),
                    event.getCause(), event.getPokemon(), null, 1));
    }

    @BattleHandler(priority = EventPriority.MONITOR)
    private void onFinish(BattleFinishEvent event) {
        if (event.isDraw())
            return;

        for (Entity member : event.getWinner().getMembers())
            if (member instanceof Player player)
                ChallengeManager.offer(new ChallengeAction(ActionType.WIN_BATTLE, player, event.getBattle(), null, null, null, 1));
    }
}
