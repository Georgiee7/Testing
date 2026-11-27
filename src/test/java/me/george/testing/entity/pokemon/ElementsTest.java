package me.george.testing.entity.pokemon;

import me.george.testing.manager.CombatManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the functionality of Elements and their relationships - affinities/weaknesses.
 * Created by George K. on 4/21/2024.
 */
public class ElementsTest {

    @Test
    public void testEffectiveness() {
        assertEquals(CombatManager.EFFECTIVE_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.FIRE, Element.GRASS));
        assertEquals(CombatManager.NOT_EFFECTIVE_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.FIRE, Element.WATER));
        assertEquals(CombatManager.NORMAL_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.NORMAL, Element.NORMAL));
    }

    @Test
    public void testResistancesAreNotWeaknesses() {
        // These used to be listed the wrong way around (the defending element's weaknesses).
        assertEquals(CombatManager.NORMAL_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.ICE, Element.ROCK));
        assertEquals(CombatManager.NORMAL_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.ROCK, Element.WATER));
        assertEquals(CombatManager.NORMAL_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.WATER, Element.ELECTRIC));
        assertEquals(CombatManager.NOT_EFFECTIVE_DAMAGE_MULTIPLIER, Element.getEffectivenessMultiplier(Element.STEEL, Element.WATER));
    }

    @Test
    public void testNoEffect() {
        assertEquals(CombatManager.IMMUNE_DAMAGE, Element.getEffectivenessMultiplier(Element.NORMAL, Element.GHOST));
        assertEquals(CombatManager.IMMUNE_DAMAGE, Element.getEffectivenessMultiplier(Element.ELECTRIC, Element.GROUND));
        assertEquals(CombatManager.IMMUNE_DAMAGE, Element.getEffectivenessMultiplier(Element.DRAGON, Element.FAIRY));

        assertTrue(Element.GROUND.hasNoEffectOn(Element.FLYING));
        assertFalse(Element.FLYING.hasNoEffectOn(Element.GROUND));
    }

    @Test
    public void testNormalAgainst() {
        assertTrue(Element.FIRE.isNormalAgainst(Element.NORMAL));
        assertFalse(Element.FIRE.isNormalAgainst(Element.FIRE)); // Resisted by itself.
        assertFalse(Element.NORMAL.isNormalAgainst(Element.GHOST));
    }

    @Test
    public void testDualElements() {
        assertEquals(CombatManager.DOUBLY_EFFECTIVE_DAMAGE_MULTIPLIER, CombatManager.calculateEffectivenessModifier(Element.ICE, new Element[]{Element.GROUND, Element.FLYING}));
        assertEquals(CombatManager.DOUBLY_NOT_EFFECTIVE_DAMAGE_MULTIPLIER, CombatManager.calculateEffectivenessModifier(Element.FIRE, new Element[]{Element.WATER, Element.ROCK}));
        assertEquals(CombatManager.IMMUNE_DAMAGE, CombatManager.calculateEffectivenessModifier(Element.GROUND, new Element[]{Element.FIRE, Element.FLYING}));
    }
}
