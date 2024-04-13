package me.george.testing.pokemon.move;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.pokemon.Element;

/**
 * A registry of all available moves that can be cast.
 * Created by George K. on 4/13/2024.
 */
@AllArgsConstructor
@Getter
public enum MoveType {
    FIRE_MOVE("Fire Move", Element.FIRE, 10),
    WATER_MOVE("Water Move", Element.WATER, 5);

    private final String name;
    private final Element element;
    private final int baseDamage;
}
