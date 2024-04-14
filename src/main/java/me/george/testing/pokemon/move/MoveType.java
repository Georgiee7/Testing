package me.george.testing.pokemon.move;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.pokemon.Element;
import me.george.testing.pokemon.Pokemon;
import me.george.testing.pokemon.PokemonType;

import java.util.function.BiFunction;

/**
 * A registry of all available moves that can be cast.
 * Created by George K. on 4/13/2024.
 */
@Getter
@AllArgsConstructor
public enum MoveType {
    FIRE_MOVE("Fire Move", Element.FIRE, null, 10),
    WATER_MOVE("Water Move", Element.WATER, null, 5);

    private final String name;
    private final Element element;
    private final BiFunction<PokemonType, Pokemon, Move> maker;
    private final int baseDamage;
}
