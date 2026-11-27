package me.george.testing.instanced.battle.event;

import lombok.Getter;
import me.george.testing.instanced.battle.Battle;

/**
 * The base of every event that happens in a battle. Listen to this class to hear all of them.
 * Created by George K. on 11/27/2026.
 */
@Getter
public abstract class BattleEvent {
    private final Battle battle;

    /**
     * Makes the event.
     *
     * @param battle The battle it happened in.
     */
    protected BattleEvent(Battle battle) {
        this.battle = battle;
    }
}
