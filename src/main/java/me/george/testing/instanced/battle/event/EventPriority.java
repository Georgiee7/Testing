package me.george.testing.instanced.battle.event;

/**
 * The order listeners hear an event in, from lowest to highest, so the highest priority has the final say.
 * Created by George K. on 11/27/2026.
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR // Hears the final outcome, should only watch and not change the event.
}
