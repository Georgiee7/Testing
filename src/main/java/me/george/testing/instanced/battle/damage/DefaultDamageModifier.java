package me.george.testing.instanced.battle.damage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.manager.CombatManager;
import me.george.testing.utils.Utils;

import java.util.function.Function;

/**
 * The modifiers every damage pipeline starts with, in the order they're applied.
 * Created by George K. on 11/27/2026.
 */
@Getter
@AllArgsConstructor
public enum DefaultDamageModifier implements DamageModifier {
    CRITICAL_HIT("Critical Hit", DefaultDamageModifier::rollCriticalHit),
    RANDOM("Random", calculation -> Utils.randFloat(CombatManager.MIN_RANDOM_DAMAGE_MULTIPLIER, 1F)),
    SAME_TYPE_ATTACK_BONUS("Same Type Attack Bonus", calculation -> calculation.getAttacker().hasElement(calculation.getMove().getElement())
            ? CombatManager.SAME_TYPE_ATTACK_BONUS_MULTIPLIER
            : CombatManager.NORMAL_DAMAGE_MULTIPLIER),
    TYPE_EFFECTIVENESS("Type Effectiveness", calculation -> CombatManager.calculateEffectivenessModifier(calculation.getMove().getElement(), calculation.getDefender().getElements()));

    private final String name;
    private final Function<DamageCalculation, Float> multiplierFunction;

    @Override
    public float getMultiplier(DamageCalculation calculation) {
        return this.multiplierFunction.apply(calculation);
    }

    // Rolls whether the hit is critical, marking the calculation so listeners can tell.
    private static float rollCriticalHit(DamageCalculation calculation) {
        calculation.setCritical(Utils.randChance(CombatManager.CRITICAL_HIT_CHANCE));
        return calculation.isCritical() ? CombatManager.CRITICAL_HIT_MULTIPLIER : CombatManager.NORMAL_DAMAGE_MULTIPLIER;
    }
}
