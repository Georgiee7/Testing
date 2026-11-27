package me.george.testing.entity.pokemon.types;

import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonType;

/**
 * A Pokémon with no mechanics of its own, made for the types that don't have their own class (yet).
 * Created by George K. on 11/27/2026.
 */
public class SimplePokemon extends Pokemon {

    public SimplePokemon(PokemonType type, PermanentStats permanentStats) {
        super(type, permanentStats);
    }
}
