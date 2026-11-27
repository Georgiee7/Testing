package me.george.testing.instanced.battle.event;

import me.george.testing.Testing;
import me.george.testing.manager.Manager;
import me.george.testing.utils.GeneralException;
import me.george.testing.utils.ReflectionUtil;
import me.george.testing.utils.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final Map<Class<?>, List<HandlerMethod>> HANDLER_METHODS = new ConcurrentHashMap<>(); // The handler methods of every listener class registered, found once.

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
     * Registers every method of a listener marked with {@link BattleHandler}, like Bukkit's event handlers.
     * Each method must take one battle event, the event it listens to, which is checked here rather than when an event is called.
     * A listener's class is only scanned once, then its handler methods are reused for every listener of that class.
     *
     * @param listener The object whose handler methods to register.
     * @return registeredListeners, to stop listening later.
     */
    public static List<RegisteredListener<?>> register(Object listener) {
        Utils.verifyNotNull(listener, "Tried to register a null listener.");
        List<RegisteredListener<?>> registered = new ArrayList<>();
        for (HandlerMethod handlerMethod : HANDLER_METHODS.computeIfAbsent(listener.getClass(), BattleEventManager::findHandlerMethods))
            registered.add(handlerMethod.listen(listener));

        return registered;
    }

    // Finds the handler methods of a class and of the classes above it. A method that overrides one above it decides on its own whether it's a handler.
    private static List<HandlerMethod> findHandlerMethods(Class<?> listenerClass) {
        List<HandlerMethod> handlerMethods = new ArrayList<>();
        Set<String> foundMethods = new HashSet<>();
        for (Class<?> clazz = listenerClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                BattleHandler settings = method.getAnnotation(BattleHandler.class);
                if (!foundMethods.add(method.getName() + Arrays.toString(method.getParameterTypes())) || settings == null)
                    continue; // Overridden below, or not a handler.

                String methodName = Utils.getSimpleName(clazz) + "#" + method.getName();
                Utils.verify(!Modifier.isStatic(method.getModifiers()), "%s is a battle handler, so it can't be static.", methodName);
                Utils.verify(method.getParameterCount() == 1 && BattleEvent.class.isAssignableFrom(method.getParameterTypes()[0]),
                        "%s is a battle handler, so it must take exactly one battle event.", methodName);

                handlerMethods.add(new HandlerMethod(method.getParameterTypes()[0].asSubclass(BattleEvent.class), settings, ReflectionUtil.unreflect(method)));
            }
        }

        return handlerMethods;
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

    /**
     * A handler method found on a listener class, with the event it listens to and its settings.
     *
     * @param eventClass The class of the event the method takes.
     * @param settings   The method's annotation, with its priority.
     * @param handle     The method, not bound to any listener yet.
     */
    private record HandlerMethod(Class<? extends BattleEvent> eventClass, BattleHandler settings, MethodHandle handle) {

        // Listens to the method's event, calling it on a listener.
        private RegisteredListener<?> listen(Object listener) {
            return listenWith(this.eventClass, this.handle.bindTo(listener));
        }

        private <T extends BattleEvent> RegisteredListener<T> listenWith(Class<T> eventClass, MethodHandle boundHandle) {
            return BattleEventManager.listen(eventClass, this.settings.priority(), this.settings.ignoreCancelled(), event -> invoke(boundHandle, event));
        }

        private static void invoke(MethodHandle boundHandle, BattleEvent event) {
            try {
                boundHandle.invoke(event);
            } catch (RuntimeException | Error exception) {
                throw exception; // As it is, so the error handling in call sees the real exception.
            } catch (Throwable throwable) {
                throw new GeneralException(throwable, "A battle handler threw %s.", Utils.getSimpleName(throwable));
            }
        }
    }
}
