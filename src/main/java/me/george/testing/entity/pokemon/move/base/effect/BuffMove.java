package me.george.testing.entity.pokemon.move.base.effect;

import me.george.testing.effect.Effect;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.move.MoveType;

/**
 * An effect move that grants a buff to a combatant.
 * Created by George K. on 5/11/2024.
 */
public class BuffMove extends EffectMove {

    protected BuffMove(MoveType type, Effect effect, Combatant caster, Combatant... targets) {
        super(type, effect, caster, targets);
    }
}
