package me.george.testing.instanced.battle.damage;

import java.util.function.Function;

/**
 * A step of the damage pipeline: a multiplier applied to a move's base damage, such as type effectiveness.
 * Created by George K. on 11/27/2026.
 */
public interface DamageModifier {

    /**
     * Gets the display name of this modifier.
     *
     * @return modifierName
     */
    String getName();

    /**
     * Gets the multiplier this modifier applies to a calculation. 1 changes nothing and 0 deals no damage.
     *
     * @param calculation The calculation so far, with the attacker, defender and move.
     * @return damageMultiplier
     */
    float getMultiplier(DamageCalculation calculation);

    /**
     * Makes a modifier from a name and a function, such as a battle's weather.
     *
     * @param name       The display name of the modifier.
     * @param multiplier The function giving the multiplier of a calculation.
     * @return damageModifier
     */
    static DamageModifier of(String name, Function<DamageCalculation, Float> multiplier) {
        return new DamageModifier() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public float getMultiplier(DamageCalculation calculation) {
                return multiplier.apply(calculation);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
