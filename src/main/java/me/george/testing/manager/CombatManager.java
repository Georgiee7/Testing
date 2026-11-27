package me.george.testing.manager;

import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.move.base.Move;
import me.george.testing.instanced.battle.damage.DamagePipeline;

/**
 * Handles combat related mechanics.
 * Created by George K. on 4/13/2024.
 */
public class CombatManager extends Manager {

    public static final float NORMAL_DAMAGE_MULTIPLIER = 1F;
    public static final float SAME_TYPE_ATTACK_BONUS_MULTIPLIER = 1.5F;
    public static final float EFFECTIVE_DAMAGE_MULTIPLIER = 2.0F;
    public static final float DOUBLY_EFFECTIVE_DAMAGE_MULTIPLIER = 4.0F;
    public static final float NOT_EFFECTIVE_DAMAGE_MULTIPLIER = .5F;
    public static final float DOUBLY_NOT_EFFECTIVE_DAMAGE_MULTIPLIER = .25F;
    public static final float IMMUNE_DAMAGE = 0F;

    public static final float CRITICAL_HIT_MULTIPLIER = 1.5F;
    public static final int CRITICAL_HIT_CHANCE = 24; // 1 in 24.
    public static final float MIN_RANDOM_DAMAGE_MULTIPLIER = .85F; // Damage varies between 85% and 100%.

    /**
     * Calculates the damage that a move will deal from one Pokémon to another, with the default damage pipeline.
     * Battles use their own pipeline, which may have more modifiers. Moves that don't deal damage deal 0.
     *
     * @param attacker The Pokémon using the move.
     * @param defender The Pokémon it's used on.
     * @param move     The move.
     * @return calculatedDamage
     */
    public static int calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        return DamagePipeline.createDefault().calculate(attacker, defender, move).getDamage();
    }

    /**
     * Calculates the effectiveness modifier of an element against an array of elements.
     * Automatically handles double (in)effectiveness.
     *
     * @param element  The element to compare.
     * @param elements The elements to compare to.
     * @return elementalEffectivenessModifier
     */
    public static float calculateEffectivenessModifier(Element element, Element[] elements) {
        float effectivenessModifier = NORMAL_DAMAGE_MULTIPLIER;
        for (Element targetElement : elements) {
            float multiplier = element.getEffectivenessMultiplier(targetElement);
            effectivenessModifier *= multiplier;
        }

        return effectivenessModifier;
    }
}
