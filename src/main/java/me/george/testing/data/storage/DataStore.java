package me.george.testing.data.storage;

/**
 * A datastore contains a set of instructions all storage types should follow.
 * Created by George K. on 4/13/2024.
 */
public interface DataStore<K, V> {

    /**
     * Allocates a key-value pair.
     *
     * @param key The key of the pair.
     * @param value The value of the pair.
     */
    void put(K key, V value);

    /**
     * Gets the value of the key of the pair.
     *
     * @param key The key of the pair.
     * @return pairValue
     */
    V get(K key);

    void remove(K key);
}
