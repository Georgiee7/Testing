package me.george.testing.manager;

import me.george.testing.Testing;
import me.george.testing.entity.player.PlayerManager;
import me.george.testing.entity.pokemon.PokemonManager;
import me.george.testing.instanced.battle.BattleManager;
import me.george.testing.parties.PartyManager;
import me.george.testing.utils.ReflectionUtil;
import me.george.testing.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a base manager responsible for the handling of a system.
 * Created by George K. on 4/13/2024.
 */
public class Manager {
    private static final List<Manager> managers = new CopyOnWriteArrayList<>();
    private static final Map<Class<? extends Manager>, Manager> INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * Method to be called when the program starts.
     */
    public void onStart() {

    }

    /**
     * Method to be called when the program shuts down.
     */
    public void onStop() {

    }

    /**
     * Register managers.
     */
    private static void register() {
        addManager(PokemonManager.class);
        addManager(PlayerManager.class);
        addManager(CombatManager.class);
        addManager(BattleManager.class);
        addManager(PartyManager.class);
    }

    /**
     * Enables all managers after registering them, executing their starting hook.
     */
    public static void registerManagers() {
        Testing.logInfo("Registering managers...");
        register();
        for (Manager manager : getManagers()) {
            manager.log("Enabling...");
            manager.onStart();
        }

        Testing.logInfo("Registered %d managers.", getManagers().size());
    }

    /**
     * Disables all managers after executing their disabling hook.
     */
    public static void disableManagers() {
        Testing.logInfo("Disabling managers...");
        for (Manager manager : getManagers()) {
            manager.log("Disabling...");
            manager.onStop();
        }

        Testing.logInfo("Managers have been disabled.");
    }

    /**
     * Get a manager's instance from its class.
     *
     * @param managerClass The class of the manager to get an instance of.
     * @return moduleInstance
     */
    @SuppressWarnings("unchecked")
    public static <T extends Manager> T getInstance(Class<T> managerClass) {
        return (T) INSTANCE_MAP.get(managerClass);
    }

    /**
     * Register a manager class.
     *
     * @param managerClass The manager class to register.
     */
    public static void addManager(Class<? extends Manager> managerClass) {
        addManager(ReflectionUtil.construct(managerClass));
    }

    /**
     * Register a manager.
     *
     * @param m The manager instance to add.
     */
    public static void addManager(Manager m) {
        Manager existing = INSTANCE_MAP.putIfAbsent(m.getClass(), m);
        Utils.verify(existing == null, "%s is already registered!", m.getName());

        managers.add(m);
    }

    /**
     * Returns an unmodifiable view of the registered managers.
     *
     * @return managers
     */
    public static List<Manager> getManagers() {
        return Collections.unmodifiableList(managers);
    }

    /**
     * Gets the simple class name of this manager, can be overridden for custom names.
     *
     * @return managerName
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Logs a message as this manager.
     *
     * @param message The message to log.
     * @param args    The optional arguments to format the message with.
     */
    public void log(String message, Object... args) {
        Testing.logInfo(getName() + " - " + message, args);
    }

    /**
     * Logs a formatted message as a specific manager.
     *
     * @param managerClass The class of the manager to use as a logger source.
     * @param message      The message to log.
     * @param args         The optional arguments to format the message with.
     */
    public static <TManagerClass extends Manager> void log(Class<TManagerClass> managerClass, String message, Object... args) {
        Testing.logInfo(Utils.getSimpleName(managerClass) + " - " + message, args);
    }
}
