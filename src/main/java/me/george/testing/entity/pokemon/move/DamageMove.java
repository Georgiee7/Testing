package me.george.testing.entity.pokemon.move;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.utils.Utils;

/**
 * Represents a Pokémon Move that will cause damage when performed.
 * Created by George K. on 4/14/2024.
 */
@Setter
@Getter
public abstract class DamageMove extends Move {

    private int damage;

    private Pokemon[] targets;

    protected DamageMove(MoveType type, Pokemon caster, Pokemon... targets) {
        super(type, caster);
        this.targets = targets;
        this.damage = getType().getBaseDamage();
    }

    /**
     * Gets a random target out of all targets.
     *
     * @return randomTarget
     */
    public Pokemon getRandomTarget() {
        return Utils.randElement(this.targets);
    }
}
