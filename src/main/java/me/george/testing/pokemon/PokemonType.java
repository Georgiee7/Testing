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
    CHARMANDER("Charmander", 0.6F, 8.5F, Charmander::new, Element.FIRE),
    BULBASAUR("Bulbasaur", 0.7F, 6.9F, Element.GRASS, Element.POISON),
    SQUIRTLE("Squirtle", 0.5F, 9F, Element.WATER);

    private final String name;
    private final float height; // in meters
    private final float weight; // in kg
    private final Function<PermanentStats, Pokemon> maker;
    private final Element[] elements;

    PokemonType(String name, float height, float weight, Element... elements) {
        this(name, height, weight, null, elements);
    }

    PokemonType(String name, float height, float weight, Function<PermanentStats, Pokemon> maker, Element... elements) {
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.maker = maker;
        this.elements = elements;
    }

    PokemonType(String name, Element... elements) {
        this(name, 0, 0, elements);
    }
}
