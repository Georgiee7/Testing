package me.george.testing.entity.pokemon.move.base;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.utils.Utils;

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

    private final Combatant caster;

    @Setter
    private float successRate = 100F;

    protected Move(MoveType type, Combatant caster) {
        this.type = type;
        this.name = type.getName();
        this.caster = caster;
    }

    /**
     * Executes the mechanics of the move.
     */
    protected abstract boolean execute();

    /**
     * Attempts to perform the move.
     *
     * @return moveSuccessfullyPerformed
     */
    public final boolean perform() {
        return rollSuccessRate() && execute(); // Roll first, so a failed move doesn't do anything.
        // calculateAccuracy();
        // TODO: Cater to mechanics that may cause a move to fail (randomness involved in moves, lack of skill, etc.).
    }

    private boolean rollSuccessRate() {
        return Utils.randChancePercent(this.successRate);
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
