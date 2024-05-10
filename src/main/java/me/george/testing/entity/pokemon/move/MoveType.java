package me.george.testing.entity.pokemon.move;

import lombok.Getter;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.entity.pokemon.move.base.Move;
import me.george.testing.entity.pokemon.move.moves.MoveTackle;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A registry of all available moves that can be cast.
 * Created by George K. on 4/13/2024.
 */
@Getter
public enum MoveType {
    TACKLE("Tackle", Element.NORMAL, MoveTackle::new, 5);

    private final String name;
    private final Element element;
    private final Function<Combatant, Move> maker;
    private final BiFunction<Combatant, Combatant[], Move> targetMoveMaker;
    private final int baseDamage;

    MoveType(String name, Element element, Function<Combatant, Move> maker, int baseDamage) {
        this.name = name;
        this.element = element;
        this.maker = maker;
        this.targetMoveMaker = null;
        this.baseDamage = baseDamage;
    }

    MoveType(String name, Element element, BiFunction<Combatant, Combatant[], Move> targetMoveMaker, int baseDamage) {
        this.name = name;
        this.element = element;
        this.maker = null;
        this.targetMoveMaker = targetMoveMaker;
        this.baseDamage = baseDamage;
    }

    MoveType(String name, Element element, int baseDamage) {
        this.name = name;
        this.element = element;
        this.maker = null;
        this.targetMoveMaker = null;
        this.baseDamage = baseDamage;
    }
}
