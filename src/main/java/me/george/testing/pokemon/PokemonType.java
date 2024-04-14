package me.george.testing.pokemon;

import lombok.Getter;
import me.george.testing.pokemon.stats.PermanentStats;
import me.george.testing.pokemon.types.Charmander;

import java.util.function.Function;

/**
 * A registry of all available types of Pokémon.
 * Created by George K. on 4/12/2024.
 */
@Getter
public enum PokemonType {
    // Tier 2
    CHARIZARD("Charizard", 1.7F, 90F, null, evolutionsOf(), Element.FIRE, Element.DRAGON),

    // Tier 1
    CHARMANDER("Charmander", 0.6F, 8.5F, Charmander::new, evolutionsOf(CHARIZARD), Element.FIRE),
    BULBASAUR("Bulbasaur", 0.7F, 6.9F, null, evolutionsOf(), Element.GRASS, Element.POISON),
    SQUIRTLE("Squirtle", 0.5F, 9F, null, evolutionsOf(), Element.WATER);

    private final String name;
    private final float height; // in meters
    private final float weight; // in kg
    private final Function<PermanentStats, Pokemon> maker;
    private final PokemonType[] evolutions;
    private final Element[] elements;

    PokemonType(String name, float height, float weight, Function<PermanentStats, Pokemon> maker, PokemonType[] evolutions, Element... elements) {
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.maker = maker;
        this.evolutions = evolutions;
        this.elements = elements;
    }

    private static PokemonType[] evolutionsOf(PokemonType... types) {
        return types;
    }
}
