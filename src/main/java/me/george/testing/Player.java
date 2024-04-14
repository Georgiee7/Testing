package me.george.testing;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.pokemon.Pokemon;
import me.george.testing.pokemon.PokemonType;

import java.util.*;

/**
 * Represents a player that can own Pokémon slaves.
 * Created by George K. on 4/14/2024.
 */
@Getter
public class Player {

    private final UUID uuid;

    @Setter
    private String name;

    private final List<Pokemon> pokemons;

    private final Map<Integer, PokemonType> pokedex;

    @Setter
    private Pokemon activePokemon;

    public Player(String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.pokemons = new ArrayList<>();
        this.pokedex = new HashMap<>();
    }

    /**
     * Adds a Pokémon to the player's party.
     *
     * @param pokemon The Pokémon to give to the player.
     */
    public void addPokemon(Pokemon pokemon) {
        this.pokemons.add(pokemon);
    }

    /**
     * Removes a Pokémon from the player's party.
     * @param pokemon The pokemon to remove.
     */
    public void removePokemon(Pokemon pokemon) {
        despawnActivePokemon();
        this.pokemons.remove(pokemon);
    }

    /**
     * Despawns the player's active Pokémon. Should handle proper execution and storage.
     */
    public void despawnActivePokemon() {
        setActivePokemon(null);
    }

    /**
     * Unlocks a specific Pokémon entry for the player's pokedex.
     *
     * @param type The Pokémon type to unlock
     */
    public void unlock(PokemonType type) {
        this.pokedex.putIfAbsent(type.getPokedexId(), type);
    }

    /**
     * Unlearns a pokedex entry from the player's pokedex.
     *
     * @param type The Pokémon type to unlearn.
     */
    public void unlearn(PokemonType type) {
        this.pokedex.remove(type.getPokedexId(), type);
    }
}
