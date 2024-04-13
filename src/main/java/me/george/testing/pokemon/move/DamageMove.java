package me.george.testing.pokemon.move;

import lombok.Getter;

/**
 * Represents a Pokémon Move that will cause damage when performed.
 * Created by George K. on 4/14/2024.
 */
@Getter
public abstract class DamageMove extends Move {

    private final int damage;

    public DamageMove() {
        super(MoveType.FIRE_MOVE);
        this.damage = getType().getBaseDamage();
    }
}
