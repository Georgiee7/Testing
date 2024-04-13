package me.george.testing.data.storage.supported;

import lombok.Getter;
import me.george.testing.data.storage.DataStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an in-memory storage (cache) that is key-value separated.
 * Created by George K. on 4/13/2024.
 */
@Getter
public class InMemoryCache<K, V> implements DataStore<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    @Override
    public void put(K key, V value) {
        this.cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return this.cache.get(key);
    }

    @Override
    public void remove(K key) {
        this.cache.remove(key);
    }

    /**
     * Returns the size of the cache (map).
     *
     * @return cacheSize
     */
    public int size() {
        return this.cache.size();
    }

    /**
     * Clears the cache (map).
     */
    public void clear() {
        this.cache.clear();
    }
}
