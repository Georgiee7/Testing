package me.george.testing.pokemon.move;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.pokemon.Element;

/**
 * Represents a move that can be performed by Pokémon, that consists of a particular element.
 * Created by George K. on 4/13/2024.
 */
@AllArgsConstructor
@Getter
public class Move {

    private final MoveType type;

    /**
     * Gets this move's element.
     *
     * @return moveElement
     */
    public Element getElement() {
        return this.type.getElement();
    }
}
