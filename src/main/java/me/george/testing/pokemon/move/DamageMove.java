package me.george.testing.pokemon.move;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.pokemon.Pokemon;
import me.george.testing.utils.Utils;

/**
 * Represents a Pokémon Move that will cause damage when performed.
 * Created by George K. on 4/14/2024.
 */
@Getter
public abstract class DamageMove extends Move {

    private final int damage;

    @Setter
    private Pokemon[] targets;

    protected DamageMove(Pokemon caster, Pokemon... targets) {
        super(MoveType.FIRE_MOVE, caster);
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
