package me.george.testing.data.storage.supported;

import me.george.testing.entity.pokemon.Pokemon;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Simple file-backed repository for {@link Pokemon} objects using JSON.
 * <p>
 * The storage directory defaults to {@code data/json/pokemons} but can be overridden
 * via the {@code data.dir} system property — set this in tests to a temp directory
 * so test data never touches the real application data.
 * </p>
 * Created by George K. on 2/26/2026.
 */
public final class PokemonFileRepository extends AbstractFileRepository<Pokemon> {
    public static final String DATA_DIR_PROPERTY = "data.dir";
    private static final String DEFAULT_DATA_DIR = "data";

    private static final PokemonFileRepository INSTANCE = new PokemonFileRepository();

    private PokemonFileRepository() {
        super(resolveDir(), Pokemon.class);
    }

    private static Path resolveDir() {
        return Path.of(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR), "json", "pokemons");
    }

    public static PokemonFileRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the repository directory path used to store pokemon JSON files.
     */
    public static Path getDir() {
        return getInstance().dir;
    }

    @Override
    protected boolean isValid(Pokemon pokemon) {
        return pokemon != null && pokemon.getUuid() != null
                && pokemon.getName() != null && !pokemon.getName().isEmpty()
                && pokemon.getType() != null;
    }

    /**
     * Persist the given pokemon to disk.
     *
     * @param pokemon pokemon to persist
     */
    public static void save(Pokemon pokemon) {
        getInstance().save(pokemon, pokemon.getUuid().toString() + ".json");
    }

    /**
     * Loads and returns all valid pokemon instances from disk.
     *
     * @return collection of pokemons
     */
    public static Collection<Pokemon> loadAll() {
        return getInstance().loadAllInternal();
    }

    /**
     * Removes a persisted pokemon file from disk.
     *
     * @param pokemon pokemon to remove
     */
    public static void remove(Pokemon pokemon) {
        getInstance().remove(pokemon.getUuid().toString() + ".json");
    }
}
