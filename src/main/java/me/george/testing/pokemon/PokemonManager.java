package me.george.testing.pokemon;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
import me.george.testing.manager.Manager;
import me.george.testing.pokemon.stats.PermanentStats;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.UUID;

/**
 * Mechanics related to the Pokémon system.
 * Created by George K. on 4/13/2024.
 */
public class PokemonManager extends Manager {

    private static final InMemoryCache<UUID, Pokemon> POKEMON_BY_UUID = new InMemoryCache<>();

    public static final int MAX_LEVEL = 100;

    public static final float SAME_TYPE_ATTACK_BONUS_MULTIPLIER = 1.5F;
    public static final float EFFECTIVE_DAMAGE_MULTIPLIER = 2.0F;
    public static final float DOUBLY_EFFECTIVE_DAMAGE_MULTIPLIER = 4.0F;
    public static final float NOT_EFFECTIVE_DAMAGE_MULTIPLIER = .5F;
    public static final float DOUBLY_NOT_EFFECTIVE_DAMAGE_MULTIPLIER = .25F;
    public static final float IMMUNE_DAMAGE = 0F;

    @Override
    public void onStop() {
        Testing.logInfo("Unloading " + POKEMON_BY_UUID.size() + " Pokemon");
        POKEMON_BY_UUID.clear();
    }

    /**
     * Creates and returns a new Pokémon.
     *
     * @param type  The type of Pokémon to create.
     * @param stats The stats it should have, if any.
     * @return newPokemon
     */
    public static Pokemon create(PokemonType type, PermanentStats stats) {
        Utils.verifyNotNull(type, "Tried to create a pokemon with an invalid type!");
        Utils.verifyNotNull(type.getMaker(), "Tried to create pokemon: '" + type.getName() + "' which has an invalid/null maker!");

        Pokemon pokemon = type.getMaker().apply(stats);
        POKEMON_BY_UUID.put(pokemon.getUuid(), pokemon);
        return pokemon;
    }

    /**
     * Gets a Pokémon by its id, or null if one does not exist.
     *
     * @param id The id to search for.
     * @return pokemonById
     */
    public static Pokemon getPokemon(UUID id) {
        return POKEMON_BY_UUID.get(id);
    }

    /**
     * Removes a pokemon by its id.
     *
     * @param id The id to search for.
     */
    public static void removePokemon(UUID id) {
        POKEMON_BY_UUID.remove(id);
    }

    /**
     * Gets all active cached Pokémon.
     *
     * @return activeCachedPokémon
     */
    public static Collection<Pokemon> getPokemons() {
        return POKEMON_BY_UUID.getValues();
    }
}
