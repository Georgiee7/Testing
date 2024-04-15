package me.george.testing.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.parties.Party;
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

    @Setter(AccessLevel.PROTECTED)
    private String name;

    private final List<Pokemon> pokemons;

    private final Map<Integer, PokemonType> pokedex;

    @Setter(AccessLevel.PROTECTED)
    private Pokemon activePokemon;

    private Party party;

    protected Player(String name) {
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
     *
     * @param pokemon The pokemon to remove.
     */
    public void removePokemon(Pokemon pokemon) {
        despawnActivePokemon();
        this.pokemons.remove(pokemon);
    }

    /**
     * Whether a player has a specific Pokémon in their team.
     *
     * @param pokemon The Pokémon in question.
     * @return hasPokemonInTeam
     */
    public boolean hasPokemon(Pokemon pokemon) {
        return this.pokemons.contains(pokemon);
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

    /**
     * Checks whether a player has unlocked a type of Pokémon in their pokedex.
     *
     * @param type The type of Pokémon to check.
     * @return hasUnlockedPokemon
     */
    public boolean hasUnlocked(PokemonType type) {
        return this.pokedex.containsValue(type);
    }

    /**
     * Adds the player to a party.
     *
     * @param party The party to add the player to.
     */
    public void addToParty(Party party) {
        this.party = party;
    }

    /**
     * Removes the player from their party.
     */
    public void removeFromParty() {
        this.party = null;
    }
}
