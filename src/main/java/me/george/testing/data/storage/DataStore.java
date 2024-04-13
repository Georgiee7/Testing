package me.george.testing.data.storage;

/**
 * A datastore contains a set of instructions all storage types should follow.
 * Created by George K. on 4/13/2024.
 */
public interface DataStore<K, V> {

    V put(K key, V value);

    V get(K key);

    void remove(K key);
}
