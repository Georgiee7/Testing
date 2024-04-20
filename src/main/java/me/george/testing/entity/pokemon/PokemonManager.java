package me.george.testing.entity.pokemon;

import me.george.testing.Testing;
import me.george.testing.data.storage.supported.InMemoryCache;
import me.george.testing.entity.player.Player;
import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.manager.Manager;
import me.george.testing.utils.GeneralException;
import me.george.testing.utils.Utils;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Mechanics related to the Pokémon system.
 * Created by George K. on 4/13/2024.
 */
public class PokemonManager extends Manager {

    private static final InMemoryCache<UUID, Pokemon> POKEMON_BY_UUID = new InMemoryCache<>();

    private static final Pattern VALID_NAME_SYNTAX_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,12}$"); // Underscores, 3 chars min, 12 chars max, alphanumeric english characters.

    public static final int MAX_LEVEL = 100;

    @Override
    public void onStop() {
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

    /**
     * Sets a player as a Pokémon's owner.
     *
     * @param pokemon The Pokémon to set the owner of.
     * @param player  The new Pokémon's owner.
     */
    public static void setPokemonOwner(Pokemon pokemon, Player player) {
        Utils.verifyNotNull(player, "Tried to make a null player the owner of a pokemon.");

        if (pokemon.hasOwner()) {
            if (pokemon.getOwner().getUuid().equals(player.getUuid()))
                throw new GeneralException("Tried to make player %s the owner of a pokemon they already own!", player.getName());

            removePokemonOwner(pokemon, pokemon.getOwner());
        }

        pokemon.setOwner(player);
        if (!player.hasPokemon(pokemon))
            player.addPokemon(pokemon);
        else
            Testing.warn("Tried to give ownership of Pokemon: '%s' to player '%s', but they already have ownership of it!", pokemon.getName(), player.getName());
    }

    /**
     * Removes a Pokémon from its owner.
     *
     * @param pokemon The pokemon to remove.
     * @param player  The player to remove the Pokémon from.
     */
    public static void removePokemonOwner(Pokemon pokemon, Player player) {
        Utils.verifyNotNull(player, "Tried to remove a pokemon from a null owner.");
        updatePokemon(pokemon, p -> {
            player.removePokemon(pokemon);
            p.setOwner(null);
        });
    }

    /**
     * Updates a Pokémon's name to a new name.
     *
     * @param pokemon The Pokémon whose name to update.
     * @param name The new name to set.
     */
    public static void setPokemonName(Pokemon pokemon, String name) {
        Utils.verify(isValidPokemonName(name), "Tried to set an invalid name: '%s' to %s.", name, pokemon.getName());
        updatePokemon(pokemon, p -> p.setName(name));
    }

    private static void updatePokemon(Pokemon pokemon, Consumer<Pokemon> callback) {
        Utils.verifyNotNull(pokemon, "Cannot update a null pokemon.");
        callback.accept(pokemon);
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
     * Removes a pokemon.
     *
     * @param pokemon The pokemon to remove.
     */
    public static void removePokemon(Pokemon pokemon) {
        removePokemon(pokemon.getUuid());
    }

    /**
     * Gets all active cached Pokémon.
     *
     * @return activeCachedPokémon
     */
    public static Collection<Pokemon> getPokemons() {
        return POKEMON_BY_UUID.getValues();
    }

    /**
     * Checks whether a string is a valid Pokémon name.
     *
     * @param string The string to check.
     * @return isValidPokemonName
     */
    public static boolean isValidPokemonName(String string) {
        return !Utils.isNullOrEmpty(string) && VALID_NAME_SYNTAX_PATTERN.matcher(string).matches();
    }
}
