package me.george.testing.entity.pokemon.types.t1.fire;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.george.testing.entity.combatant.stats.PermanentStats;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.PokemonType;

/**
 * The Charmander Pokémon.
 * Created by George K. on 4/13/2024.
 */
public class Charmander extends Pokemon {

    @JsonCreator
    public Charmander(@JsonProperty("permanentStats") PermanentStats permanentStats) {
        super(PokemonType.CHARMANDER, permanentStats);
    }
}
