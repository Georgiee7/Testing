package me.george.testing.entity.pokemon.move.base;

import me.george.testing.entity.pokemon.move.MoveType;

/**
 * Functionality for moves that may deal damage.
 * Created by George K. on 5/9/2024.
 */
public interface DamagingMove extends AccuracyMove {

    /**
     * Gets the power of the move, the base of the damage it deals. Defaults to its type's power,
     * can be overridden for moves whose power changes (such as ones that get stronger at low health).
     *
     * @param moveType The move to get the power of.
     * @return movePower
     */
    default int getPower(MoveType moveType) {
        return Math.max(moveType.getPower(), 0);
    }
}
