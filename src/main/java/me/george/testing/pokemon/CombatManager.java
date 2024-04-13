package me.george.testing.pokemon;

import me.george.testing.manager.Manager;
import me.george.testing.pokemon.move.DamageMove;
import me.george.testing.pokemon.move.Move;

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

    /**
     * Calculates the damage that a damaging move will deal to a Pokémon.
     * If the move is not a damaging move, it will return damage equal to 0.
     *
     * @param move    The damaging move.
     * @param pokemon The defending Pokémon.
     * @return calculatedDamage
     */
    public int calculateDamage(Move move, Pokemon pokemon) {
        return move instanceof DamageMove
                ? (int) (((DamageMove) move).getDamage() * calculateEffectivenessModifier(move.getElement(), pokemon.getElements()))
                : 0;
    }

    /**
     * Calculates the effectiveness modifier of an element against an array of elements.
     *
     * @param element  The element to compare.
     * @param elements The elements to compare to.
     * @return elementalEffectivenessModifier
     */
    public float calculateEffectivenessModifier(Element element, Element[] elements) {
        float modifier = NORMAL_DAMAGE_MULTIPLIER;
        for (Element e : elements)
            modifier *= element.getEffectivenessMultiplier(element);

        // TODO: Deal with doubly effective and doubly weak modifiers.
        // TODO: Deal with STAB modifier.

        return modifier;
    }
}
