package me.george.testing.pokemon.move;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.pokemon.Element;
import me.george.testing.pokemon.Pokemon;

/**
 * Represents a move that can be performed by Pokémon, that consists of a particular element.
 * Created by George K. on 4/13/2024.
 */
@Getter
public abstract class Move {

    private final MoveType type;

    private final String name;

    @Setter
    private boolean undodgeable = false;

    private final Pokemon caster;

    @Setter
    private float accuracy;

    protected Move(MoveType type, Pokemon caster) {
        this.type = type;
        this.name = type.getName();
        this.caster = caster;
    }

    /**
     * Executes the mechanics of the move.
     */
    protected abstract void execute();

    /**
     * Attempts to perform the move.
     *
     * @return moveSuccessfullyPerformed
     */
    public final boolean perform() {
        execute();
        // calculateAccuracy();
        // TODO: Cater to mechanics that may cause a move to fail (randomness involved in moves, lack of skill, etc.).
        return true;
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
