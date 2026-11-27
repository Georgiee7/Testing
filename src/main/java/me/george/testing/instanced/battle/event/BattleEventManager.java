package me.george.testing.instanced.battle.event;

import me.george.testing.Testing;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Handles battle events: listening to them and calling them.
 * Listeners hear the events of their class and every subclass of it, in order of priority, and can change or cancel them.
 * Created by George K. on 11/27/2026.
 */
public class BattleEventManager extends Manager {

    private static final Map<Class<? extends BattleEvent>, List<RegisteredListener<?>>> LISTENERS = new ConcurrentHashMap<>();

    @Override
    public void onStop() {
        LISTENERS.clear();
    }

    /**
     * Listens to an event at normal priority.
     *
     * @param eventClass The class of the event to listen to.
     * @param handler    What to do when it happens.
     * @return registeredListener
     */
    public static <T extends BattleEvent> RegisteredListener<T> listen(Class<T> eventClass, Consumer<T> handler) {
        return listen(eventClass, EventPriority.NORMAL, false, handler);
    }

    /**
     * Listens to an event.
     *
     * @param eventClass The class of the event to listen to.
     * @param priority   When to hear it, compared to other listeners.
     * @param handler    What to do when it happens.
     * @return registeredListener
     */
    public static <T extends BattleEvent> RegisteredListener<T> listen(Class<T> eventClass, EventPriority priority, Consumer<T> handler) {
        return listen(eventClass, priority, false, handler);
    }

    /**
     * Listens to an event.
     *
     * @param eventClass      The class of the event to listen to.
     * @param priority        When to hear it, compared to other listeners.
     * @param ignoreCancelled Whether to skip the event if a listener before this one cancelled it.
     * @param handler         What to do when it happens.
     * @return registeredListener
     */
    public static <T extends BattleEvent> RegisteredListener<T> listen(Class<T> eventClass, EventPriority priority, boolean ignoreCancelled, Consumer<T> handler) {
        Utils.verifyNotNull(eventClass, "Tried to listen to a null event class.");
        Utils.verifyNotNull(priority, "Tried to listen to an event with a null priority.");
        Utils.verifyNotNull(handler, "Tried to listen to an event with a null handler.");

        RegisteredListener<T> listener = new RegisteredListener<>(eventClass, priority, ignoreCancelled, handler);
        LISTENERS.computeIfAbsent(eventClass, key -> new CopyOnWriteArrayList<>()).add(listener);
        return listener;
    }

    /**
     * Stops a listener from hearing any more events.
     *
     * @param listener The listener to remove.
     */
    public static void unlisten(RegisteredListener<?> listener) {
        Utils.verifyNotNull(listener, "Tried to remove a null listener.");
        List<RegisteredListener<?>> listeners = LISTENERS.get(listener.getEventClass());
        if (listeners != null)
            listeners.remove(listener);
    }

    /**
     * Calls an event, passing it to every listener that hears it.
     * A listener that throws an exception is logged and skipped, so it can't break the battle or the listeners after it.
     *
     * @param event The event to call.
     * @return event, to check whether it got cancelled or read what the listeners changed.
     */
    public static <T extends BattleEvent> T call(T event) {
        Utils.verifyNotNull(event, "Tried to call a null event.");
        for (RegisteredListener<?> listener : getListeners(event.getClass())) {
            if (listener.isIgnoreCancelled() && event instanceof Cancellable cancellable && cancellable.isCancelled())
                continue;

            try {
                listener.call(event);
            } catch (Exception exception) {
                Testing.handleError(exception, "A listener of %s threw an exception.", Utils.getSimpleName(event));
            }
        }

        return event;
    }

    // The listeners of an event's class and of every event class above it, lowest priority first.
    private static List<RegisteredListener<?>> getListeners(Class<?> eventClass) {
        List<RegisteredListener<?>> listeners = new ArrayList<>();
        for (Class<?> clazz = eventClass; clazz != null && BattleEvent.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
            List<RegisteredListener<?>> classListeners = LISTENERS.get(clazz);
            if (classListeners != null)
                listeners.addAll(classListeners);
        }

        listeners.sort(Comparator.comparing(RegisteredListener::getPriority)); // Stable, so listeners of the same priority keep their order.
        return listeners;
    }
}
