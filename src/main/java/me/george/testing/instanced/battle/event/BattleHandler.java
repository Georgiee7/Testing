package me.george.testing.instanced.battle.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method of a listener as a battle event handler, registered with {@link BattleEventManager#register(Object)}.
 * The method must take one parameter, the battle event it listens to, and hears every subclass of it too.
 * Created by George K. on 11/27/2026.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BattleHandler {

    /**
     * When to hear the event, compared to other listeners.
     *
     * @return priority
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Whether to skip the event if a listener before this one cancelled it.
     *
     * @return ignoreCancelled
     */
    boolean ignoreCancelled() default false;
}
