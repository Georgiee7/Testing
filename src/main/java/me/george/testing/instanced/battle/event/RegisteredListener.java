package me.george.testing.instanced.battle.event;

import lombok.Getter;

import java.util.function.Consumer;

/**
 * A listener registered for an event class, with its priority. Keep it to stop listening later.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class RegisteredListener<T extends BattleEvent> {
    private final Class<T> eventClass;
    private final EventPriority priority;
    private final boolean ignoreCancelled; // Skips events a listener before it cancelled.
    private final Consumer<T> handler;

    RegisteredListener(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<T> handler) {
        this.eventClass = eventClass;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
        this.handler = handler;
    }

    /**
     * Passes an event to the handler.
     *
     * @param event The event, of this listener's event class or a subclass of it.
     */
    void call(BattleEvent event) {
        this.handler.accept(this.eventClass.cast(event));
    }
}
