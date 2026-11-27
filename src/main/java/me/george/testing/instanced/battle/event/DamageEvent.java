package me.george.testing.instanced.battle.event;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.instanced.battle.Battle;
import me.george.testing.instanced.battle.damage.DamageCalculation;

/**
 * Fired when a move hits, after its damage is calculated and before it's dealt.
 * Listeners can change the damage, or cancel the event so none is dealt. The calculation shows how the damage was reached.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class DamageEvent extends BattleEvent implements Cancellable {
    private final DamageCalculation calculation;
    private int damage;
    @Setter
    private boolean cancelled;

    /**
     * Makes the event.
     *
     * @param battle      The battle.
     * @param calculation The calculated damage, with the attacker, defender and move.
     */
    public DamageEvent(Battle battle, DamageCalculation calculation) {
        super(battle);
        this.calculation = calculation;
        this.damage = calculation.getDamage();
    }

    /**
     * Gets the Pokémon dealing the damage.
     *
     * @return attacker
     */
    public Pokemon getAttacker() {
        return this.calculation.getAttacker();
    }

    /**
     * Gets the Pokémon taking the damage.
     *
     * @return defender
     */
    public Pokemon getDefender() {
        return this.calculation.getDefender();
    }

    /**
     * Sets the damage that will be dealt.
     *
     * @param damage The new damage, 0 or more.
     */
    public void setDamage(int damage) {
        this.damage = Math.max(damage, 0);
    }
}
