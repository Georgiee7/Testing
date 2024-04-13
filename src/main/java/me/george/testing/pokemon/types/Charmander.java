package me.george.testing.pokemon.types;

import me.george.testing.pokemon.Pokemon;
import me.george.testing.pokemon.PokemonType;
import me.george.testing.pokemon.stats.PermanentStats;

/**
 * The Charmander Pokémon.
 * Created by George K. on 4/13/2024.
 */
public class Charmander extends Pokemon {

    public Charmander(PermanentStats permanentStats) {
        super(PokemonType.CHARMANDER, permanentStats);
    }

    public Charmander() {
        super(PokemonType.CHARMANDER);
    }
}
