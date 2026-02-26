package me.george.testing.data.storage.supported;

import me.george.testing.entity.pokemon.Pokemon;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Simple file-backed repository for Pokemon objects using JSON.
 * Created by George K. on 2/26/2026.
 */
public final class PokemonFileRepository extends AbstractFileRepository<Pokemon> {
    private static final Path POKEMON_DIR = Path.of("data", "json", "pokemons");
    private static final PokemonFileRepository INSTANCE = new PokemonFileRepository();

    private PokemonFileRepository() {
        super(POKEMON_DIR, Pokemon.class);
    }

    public static PokemonFileRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the repository directory path used to store pokemon JSON files.
     */
    public static Path getDir() {
        return POKEMON_DIR;
    }

    @Override
    protected boolean isValid(Pokemon pokemon) {
        return pokemon != null && pokemon.getUuid() != null && pokemon.getName() != null && !pokemon.getName().isEmpty() && pokemon.getType() != null;
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
