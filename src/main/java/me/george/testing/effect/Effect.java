package me.george.testing.effect;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.effect.effects.Paralysis;
import me.george.testing.entity.combatant.Combatant;
import me.george.testing.utils.Utils;

import java.util.Collection;

/**
 * A combat effect that can be applied to combatants.
 * Created by George K. on 5/11/2024.
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DotEffect.class, name = "dot_effect"),
        @JsonSubTypes.Type(value = Paralysis.class, name = "paralysis")
})
public abstract class Effect {

    private final EffectType effectType;

    @Setter
    private float procChance = 100F;

    public Effect(EffectType effectType) {
        this.effectType = effectType;
    }

    /**
     * Sets up the effect's mechanics and attempts to perform them when the effect is executed.
     *
     * @param receivers The effect receivers.
     * @return effectSuccessfullyPerformed
     */
    protected abstract boolean perform(Collection<Combatant> receivers);

    /**
     * Executes and attempts to perform the effect's mechanics.
     *
     * @param receivers The effect receivers.
     * @return effectSuccessfullyExecuted
     */
    public final boolean execute(Collection<Combatant> receivers) {
        return (Utils.randChance(this.procChance) && perform(receivers));
    }
}
