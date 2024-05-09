package me.george.testing.entity.pokemon.move;

/**
 * Functionality for moves that may deal damage.
 * Created by George K. on 5/9/2024.
 */
public interface DamagingMove {

    /**
     * Calculates the amount of damage the move should do. Defaults to its base damage.
     *
     * @param moveType The move to calculate the damage of.
     * @return calculatedMoveDamage
     */
    default int calculateDamage(MoveType moveType) {
        return Math.max(moveType.getBaseDamage(), 0);
    }
}
