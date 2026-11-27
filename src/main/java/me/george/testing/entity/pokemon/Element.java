package me.george.testing.entity.pokemon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.manager.CombatManager;
import me.george.testing.utils.Utils;

import java.util.HashSet;
import java.util.Set;

/**
 * The available elemental types Pokémon and moves may have.
 * Created by George K. on 4/12/2024.
 */
@AllArgsConstructor
@Getter
public enum Element {
    NORMAL,
    FIGHTER,
    GROUND,
    ROCK,
    STEEL,
    FLYING,
    DRAGON,
    FIRE,
    ELECTRIC,
    WATER,
    ICE,
    GRASS,
    DARK,
    PSYCHIC,
    GHOST,
    BUG,
    POISON,
    FAIRY;

    private final Set<Element> effectiveAgainst = new HashSet<>();
    private final Set<Element> weakAgainst = new HashSet<>();
    private final Set<Element> noEffectOn = new HashSet<>();

    static {
        initializeElementRelationships();
    }

    private static void initializeElementRelationships() {
        // From the attacking element's side: effective against (2x), weak against (0.5x) and no effect on (0x).
        Element.NORMAL.weakAgainst.addAll(Set.of(Element.ROCK, Element.STEEL));
        Element.NORMAL.noEffectOn.add(Element.GHOST);

        Element.FIGHTER.effectiveAgainst.addAll(Set.of(Element.NORMAL, Element.ROCK, Element.STEEL, Element.ICE, Element.DARK));
        Element.FIGHTER.weakAgainst.addAll(Set.of(Element.FLYING, Element.PSYCHIC, Element.FAIRY, Element.POISON, Element.BUG));
        Element.FIGHTER.noEffectOn.add(Element.GHOST);

        Element.GROUND.effectiveAgainst.addAll(Set.of(Element.FIRE, Element.ELECTRIC, Element.POISON, Element.ROCK, Element.STEEL));
        Element.GROUND.weakAgainst.addAll(Set.of(Element.GRASS, Element.BUG));
        Element.GROUND.noEffectOn.add(Element.FLYING);

        Element.ROCK.effectiveAgainst.addAll(Set.of(Element.FIRE, Element.ICE, Element.FLYING, Element.BUG));
        Element.ROCK.weakAgainst.addAll(Set.of(Element.FIGHTER, Element.GROUND, Element.STEEL));

        Element.STEEL.effectiveAgainst.addAll(Set.of(Element.ICE, Element.ROCK, Element.FAIRY));
        Element.STEEL.weakAgainst.addAll(Set.of(Element.FIRE, Element.WATER, Element.ELECTRIC, Element.STEEL));

        Element.FLYING.effectiveAgainst.addAll(Set.of(Element.GRASS, Element.FIGHTER, Element.BUG));
        Element.FLYING.weakAgainst.addAll(Set.of(Element.ELECTRIC, Element.ROCK, Element.STEEL));

        Element.DRAGON.effectiveAgainst.add(Element.DRAGON);
        Element.DRAGON.weakAgainst.add(Element.STEEL);
        Element.DRAGON.noEffectOn.add(Element.FAIRY);

        Element.FIRE.effectiveAgainst.addAll(Set.of(Element.GRASS, Element.ICE, Element.BUG, Element.STEEL));
        Element.FIRE.weakAgainst.addAll(Set.of(Element.WATER, Element.ROCK, Element.FIRE, Element.DRAGON));

        Element.ELECTRIC.effectiveAgainst.addAll(Set.of(Element.WATER, Element.FLYING));
        Element.ELECTRIC.weakAgainst.addAll(Set.of(Element.ELECTRIC, Element.GRASS, Element.DRAGON));
        Element.ELECTRIC.noEffectOn.add(Element.GROUND);

        Element.WATER.effectiveAgainst.addAll(Set.of(Element.FIRE, Element.GROUND, Element.ROCK));
        Element.WATER.weakAgainst.addAll(Set.of(Element.WATER, Element.GRASS, Element.DRAGON));

        Element.ICE.effectiveAgainst.addAll(Set.of(Element.GRASS, Element.GROUND, Element.FLYING, Element.DRAGON));
        Element.ICE.weakAgainst.addAll(Set.of(Element.FIRE, Element.WATER, Element.ICE, Element.STEEL));

        Element.GRASS.effectiveAgainst.addAll(Set.of(Element.WATER, Element.GROUND, Element.ROCK));
        Element.GRASS.weakAgainst.addAll(Set.of(Element.FIRE, Element.GRASS, Element.POISON, Element.FLYING, Element.BUG, Element.DRAGON, Element.STEEL));

        Element.DARK.effectiveAgainst.addAll(Set.of(Element.PSYCHIC, Element.GHOST));
        Element.DARK.weakAgainst.addAll(Set.of(Element.FIGHTER, Element.DARK, Element.FAIRY));

        Element.PSYCHIC.effectiveAgainst.addAll(Set.of(Element.FIGHTER, Element.POISON));
        Element.PSYCHIC.weakAgainst.addAll(Set.of(Element.PSYCHIC, Element.STEEL));
        Element.PSYCHIC.noEffectOn.add(Element.DARK);

        Element.GHOST.effectiveAgainst.addAll(Set.of(Element.GHOST, Element.PSYCHIC));
        Element.GHOST.weakAgainst.add(Element.DARK);
        Element.GHOST.noEffectOn.add(Element.NORMAL);

        Element.BUG.effectiveAgainst.addAll(Set.of(Element.GRASS, Element.PSYCHIC, Element.DARK));
        Element.BUG.weakAgainst.addAll(Set.of(Element.FIRE, Element.FIGHTER, Element.FLYING, Element.STEEL, Element.POISON, Element.GHOST, Element.FAIRY));

        Element.POISON.effectiveAgainst.addAll(Set.of(Element.GRASS, Element.FAIRY));
        Element.POISON.weakAgainst.addAll(Set.of(Element.POISON, Element.GROUND, Element.ROCK, Element.GHOST));
        Element.POISON.noEffectOn.add(Element.STEEL);

        Element.FAIRY.effectiveAgainst.addAll(Set.of(Element.FIGHTER, Element.DRAGON, Element.DARK));
        Element.FAIRY.weakAgainst.addAll(Set.of(Element.FIRE, Element.POISON, Element.STEEL));
    }

    /**
     * Gets the display friendly name of the element.
     *
     * @return displayFriendlyName
     */
    public String getDisplayName() {
        return Utils.capitalize(name());
    }

    /**
     * Whether this element is effective against another type of element.
     *
     * @param other The other element in question.
     * @return isEffective
     */
    public boolean isEffectiveAgainst(Element other) {
        return this.effectiveAgainst.contains(other);
    }

    /**
     * Whether this element is weak against another type of element.
     *
     * @param other The other element in question.
     * @return isWeak
     */
    public boolean isWeakAgainst(Element other) {
        return this.weakAgainst.contains(other);
    }

    /**
     * Whether this element has no effect on another type of element, meaning the other one is immune to it.
     *
     * @param other The other element in question.
     * @return hasNoEffect
     */
    public boolean hasNoEffectOn(Element other) {
        return this.noEffectOn.contains(other);
    }

    /**
     * Whether this element is normal against another type of element.
     *
     * @param other The other element in question.
     * @return isNormal
     */
    public boolean isNormalAgainst(Element other) {
        return !isEffectiveAgainst(other) && !isWeakAgainst(other) && !hasNoEffectOn(other);
    }

    /**
     * Gets the effectiveness multiplier of this element against another.
     *
     * @param other The element to compare to.
     * @return effectivenessMultiplier
     */
    public float getEffectivenessMultiplier(Element other) {
        if (isEffectiveAgainst(other))
            return CombatManager.EFFECTIVE_DAMAGE_MULTIPLIER;
        else if (isWeakAgainst(other))
            return CombatManager.NOT_EFFECTIVE_DAMAGE_MULTIPLIER;
        else if (hasNoEffectOn(other))
            return CombatManager.IMMUNE_DAMAGE;

        return CombatManager.NORMAL_DAMAGE_MULTIPLIER;
    }

    /**
     * Gets the effectiveness multiplier of one element against another.
     *
     * @param from The element to compare.
     * @param to The element to compare to.
     * @return effectivenessMultiplier
     */
    public static float getEffectivenessMultiplier(Element from, Element to) {
        return from.getEffectivenessMultiplier(to);
    }
}
