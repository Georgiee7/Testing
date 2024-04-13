package me.george.testing.pokemon.move;

import lombok.Getter;
import me.george.testing.pokemon.Element;

/**
 * Represents a move that can be performed by Pokémon, that consists of a particular element.
 * Created by George K. on 4/13/2024.
 */
@Getter
public abstract class Move {

    private final MoveType type;

    private final String name;

    public Move(MoveType type) {
        this.type = type;
        this.name = type.getName();
    }

    /**
     * Gets this move's element.
     *
     * @return moveElement
     */
    public Element getElement() {
        return this.type.getElement();
    }
}
