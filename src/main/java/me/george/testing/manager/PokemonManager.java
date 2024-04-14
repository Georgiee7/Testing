package me.george.testing.manager;

import me.george.testing.Player;
import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
import me.george.testing.pokemon.Pokemon;
import me.george.testing.pokemon.PokemonType;
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

    @Override
    public void onStop() {
        super.onStop();
        Testing.logInfo("Unloading %d Pokemon", POKEMON_BY_UUID.size());
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

    public static void setPokemonOwner(Pokemon pokemon, Player player) {
        Utils.verifyNotNull(pokemon, "Cannot set the owner of a null pokemon.");
        Utils.verifyNotNull(player, "Tried to make a null player the owner of a pokemon.");

        pokemon.setOwner(player);
        player.addPokemon(pokemon);
    }

    public static void removePokemonOwner(Pokemon pokemon, Player player) {
        Utils.verifyNotNull(pokemon, "Cannot remove a null pokemon from a player.");
        Utils.verifyNotNull(player, "Tried to remove a pokemon from a null owner.");

        player.removePokemon(pokemon);
        pokemon.setOwner(null);
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
