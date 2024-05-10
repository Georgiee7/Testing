package me.george.testing.effect;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.combatant.Combatant;

import java.util.Collection;

/**
 * An effect that causes damage over time to receivers.
 * Created by George K. on 5/11/2024.
 */
@Getter
@Setter
public abstract class DotEffect extends Effect {
    private int damage;

    public DotEffect(EffectType effectType) {
        super(effectType);
    }

    @Override
    protected boolean perform(Collection<Combatant> receivers) {
        for (Combatant receiver : receivers) {
            // TODO: Damage receiver
        }
        return true;
    }
}
