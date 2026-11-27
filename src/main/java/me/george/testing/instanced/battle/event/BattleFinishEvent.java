package me.george.testing.instanced.battle.event;

import me.george.testing.instanced.battle.Battle;
import me.george.testing.parties.Party;

/**
 * Fired when a battle finishes, after its winner is decided.
 * Created by George K. on 11/27/2026.
 */
public class BattleFinishEvent extends BattleEvent {

    /**
     * Makes the event.
     *
     * @param battle The battle that finished.
     */
    public BattleFinishEvent(Battle battle) {
        super(battle);
    }

    /**
     * Gets the party that won the battle.
     *
     * @return winningParty, null if it was a draw.
     */
    public Party getWinner() {
        return getBattle().getWinner();
    }

    /**
     * Whether the battle ended in a draw.
     *
     * @return isDraw
     */
    public boolean isDraw() {
        return getBattle().isDraw();
    }
}
