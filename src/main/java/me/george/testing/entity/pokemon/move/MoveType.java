package me.george.testing.entity.pokemon.move;

import lombok.Getter;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.move.base.Move;
import me.george.testing.entity.pokemon.move.moves.MoveTackle;
import me.george.testing.entity.pokemon.move.moves.SimpleDamagingMove;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A registry of all available moves that can be cast.
 * Created by George K. on 4/13/2024.
 */
@Getter
public enum MoveType {
    TACKLE("Tackle", Element.NORMAL, MoveCategory.PHYSICAL, 40, MoveTackle::new),
    KARATE_CHOP("Karate Chop", Element.FIGHTER, MoveCategory.PHYSICAL, 50),
    MUD_SLAP("Mud-Slap", Element.GROUND, MoveCategory.SPECIAL, 20),
    ROCK_THROW("Rock Throw", Element.ROCK, MoveCategory.PHYSICAL, 50),
    METAL_CLAW("Metal Claw", Element.STEEL, MoveCategory.PHYSICAL, 50),
    GUST("Gust", Element.FLYING, MoveCategory.SPECIAL, 40),
    TWISTER("Twister", Element.DRAGON, MoveCategory.SPECIAL, 40),
    EMBER("Ember", Element.FIRE, MoveCategory.SPECIAL, 40),
    THUNDER_SHOCK("Thunder Shock", Element.ELECTRIC, MoveCategory.SPECIAL, 40),
    WATER_GUN("Water Gun", Element.WATER, MoveCategory.SPECIAL, 40),
    POWDER_SNOW("Powder Snow", Element.ICE, MoveCategory.SPECIAL, 40),
    VINE_WHIP("Vine Whip", Element.GRASS, MoveCategory.PHYSICAL, 45),
    BITE("Bite", Element.DARK, MoveCategory.PHYSICAL, 60),
    CONFUSION("Confusion", Element.PSYCHIC, MoveCategory.SPECIAL, 50),
    LICK("Lick", Element.GHOST, MoveCategory.PHYSICAL, 30),
    BUG_BITE("Bug Bite", Element.BUG, MoveCategory.PHYSICAL, 60),
    POISON_STING("Poison Sting", Element.POISON, MoveCategory.PHYSICAL, 15),
    FAIRY_WIND("Fairy Wind", Element.FAIRY, MoveCategory.SPECIAL, 40);

    private final String name;
    private final Element element;
    private final MoveCategory category;
    private final int power; // The base of the damage it deals, 0 for moves that deal none.
    private final Function<Combatant, Move> maker;
    private final BiFunction<Combatant, Combatant[], Move> targetMoveMaker;

    MoveType(String name, Element element, MoveCategory category, int power, Function<Combatant, Move> maker) {
        this.name = name;
        this.element = element;
        this.category = category;
        this.power = power;
        this.maker = maker;
        this.targetMoveMaker = null;
    }

    MoveType(String name, Element element, MoveCategory category, int power, BiFunction<Combatant, Combatant[], Move> targetMoveMaker) {
        this.name = name;
        this.element = element;
        this.category = category;
        this.power = power;
        this.maker = null;
        this.targetMoveMaker = targetMoveMaker;
    }

    MoveType(String name, Element element, MoveCategory category, int power) {
        this.name = name;
        this.element = element;
        this.category = category;
        this.power = power;
        this.maker = null;
        this.targetMoveMaker = null;
    }

    /**
     * Creates this move, cast by a combatant on targets.
     * Moves without their own class are made as simple damaging moves.
     *
     * @param caster  The combatant using the move.
     * @param targets The combatants it's used on.
     * @return move
     */
    public Move create(Combatant caster, Combatant... targets) {
        if (this.targetMoveMaker != null)
            return this.targetMoveMaker.apply(caster, targets);

        if (this.maker != null)
            return this.maker.apply(caster);

        return new SimpleDamagingMove(this, caster, targets);
    }

    /**
     * Gets the moves a Pokémon of some elements starts with: Tackle, and the first move of each of its elements.
     *
     * @param elements The Pokémon's elements.
     * @return startingMoves
     */
    public static List<MoveType> getStartingMoves(Element... elements) {
        List<MoveType> moves = new ArrayList<>();
        moves.add(TACKLE);
        for (Element element : elements)
            for (MoveType move : values())
                if (move.getElement() == element) {
                    if (!moves.contains(move))
                        moves.add(move);
                    break;
                }

        return moves;
    }
}
