package me.george.testing.effect.effects;

import me.george.testing.effect.Effect;
import me.george.testing.effect.EffectType;
import me.george.testing.entity.combatant.Combatant;

import java.util.Collection;

/**
 * The paralysis movement impairing effect.
 * Created by George K. on 5/11/2024.
 */
public class Paralysis extends Effect {

    public Paralysis() {
        super(EffectType.PARALYSIS);
    }

    @Override
    protected boolean perform(Collection<Combatant> receivers) {
        for (Combatant receiver : receivers) {
            // TODO: Paralyze receivers.
        }

        return true;
    }
}
