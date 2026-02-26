package me.george.testing.entity.pokemon.move.base.effect;

import me.george.testing.effect.Effect;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.entity.pokemon.move.base.AccuracyMove;
import me.george.testing.entity.pokemon.move.base.TargetMove;

/**
 * A base move that causes an effect to a combatant.
 * Created by George K. on 5/11/2024.
 */
public abstract class EffectMove extends TargetMove implements AccuracyMove {
    private final Effect effect;

    protected EffectMove(MoveType type, Effect effect, Combatant caster, Combatant... targets) {
        super(type, caster, targets);
        this.effect = effect;
    }
}
