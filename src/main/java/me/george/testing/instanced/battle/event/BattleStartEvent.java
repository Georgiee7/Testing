package me.george.testing.instanced.battle.event;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.instanced.battle.Battle;

/**
 * Fired when a battle is about to start. Cancelling it keeps the battle from starting.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class BattleStartEvent extends BattleEvent implements Cancellable {
    @Setter
    private boolean cancelled;

    /**
     * Makes the event.
     *
     * @param battle The battle starting.
     */
    public BattleStartEvent(Battle battle) {
        super(battle);
    }
}
