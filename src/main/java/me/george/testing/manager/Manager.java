package me.george.testing.manager;

import me.george.testing.data.storage.DataStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by George K. on 4/13/2024.
 */
public abstract class Manager<K, V> {

    private static final Map<Class<? extends Manager<?, ?>>, Manager<?, ?>> INSTANCES = new HashMap<>();

    protected final DataStore<K, V> dataStore;

    @SuppressWarnings("unchecked")
    protected Manager(DataStore<K, V> dataStore) {
        this.dataStore = dataStore;
        INSTANCES.put((Class<? extends Manager<?, ?>>) this.getClass(), this);
    }

    /**
     * Method to be called when the program starts.
     */
    public abstract void onStart();

    /**
     * Method to be called when the program shuts down.
     */
    public abstract void onStop();

    /**
     * Get the instance of a manager by its class.
     *
     * @param managerClass The class of the manager.
     * @param <T>          The type of manager.
     * @return The instance of the manager.
     */
    public static <T extends Manager<?, ?>> T getInstance(Class<T> managerClass) {
        return managerClass.cast(INSTANCES.get(managerClass));
    }
}
