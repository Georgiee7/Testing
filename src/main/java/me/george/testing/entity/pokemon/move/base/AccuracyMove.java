package me.george.testing.entity.pokemon.move.base;

import me.george.testing.entity.combatant.Combatant;

import java.util.Collection;

/**
 * Functionality revolving around the accuracy of a move that a caster performed against combatants.
 * Created by George K. on 5/11/2024.
 */
public interface AccuracyMove {

    /**
     * Calculates accuracy based on a caster-receivers relationship, taking the following into account:
     * level difference, resistances and other effects & element compatibility.
     *
     * @param caster    The caster to calculate the accuracy for.
     * @param receivers The receivers to compare to the caster's state.
     * @return calculatedAccuracy
     */
    default float calculateAccuracy(Combatant caster, Collection<Combatant> receivers) {
        // TODO: Calculate accuracy based on caster-receivers level difference, resistances/effects, and other conditions.
        return 100F;
    }
}
