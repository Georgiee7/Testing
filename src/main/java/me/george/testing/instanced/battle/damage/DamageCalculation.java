package me.george.testing.instanced.battle.damage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.move.base.Move;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The damage of one hit as the pipeline works it out: the base damage, each modifier's multiplier and the final damage.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class DamageCalculation {
    private final Pokemon attacker;
    private final Pokemon defender;
    private final Move move;
    private final int baseDamage;
    private final Map<DamageModifier, Float> multipliers = new LinkedHashMap<>(); // In the order they were applied.

    @Setter(AccessLevel.PACKAGE)
    private boolean critical;

    private int damage;

    DamageCalculation(Pokemon attacker, Pokemon defender, Move move, int baseDamage) {
        this.attacker = attacker;
        this.defender = defender;
        this.move = move;
        this.baseDamage = baseDamage;
        this.damage = baseDamage;
    }

    /**
     * Gets the multiplier a modifier applied.
     *
     * @param modifier The modifier in question.
     * @return multiplier, 1 if it wasn't applied.
     */
    public float getMultiplier(DamageModifier modifier) {
        return this.multipliers.getOrDefault(modifier, 1F);
    }

    /**
     * Gets every multiplier applied, by modifier, in order.
     *
     * @return multipliers
     */
    public Map<DamageModifier, Float> getMultipliers() {
        return Collections.unmodifiableMap(this.multipliers);
    }

    /**
     * Gets all the multipliers applied, multiplied together.
     *
     * @return totalMultiplier
     */
    public float getTotalMultiplier() {
        float total = 1F;
        for (float multiplier : this.multipliers.values())
            total *= multiplier;

        return total;
    }

    /**
     * Whether the defender is immune to the move, so it deals no damage.
     *
     * @return isImmune
     */
    public boolean isImmune() {
        return this.baseDamage > 0 && getTotalMultiplier() == 0;
    }

    void applyMultiplier(DamageModifier modifier, float multiplier) {
        this.multipliers.put(modifier, Math.max(multiplier, 0F));
    }

    // Works out the final damage, at least 1 unless the defender is immune or the move deals no damage.
    void finish() {
        float total = getTotalMultiplier();
        this.damage = this.baseDamage <= 0 || total <= 0 ? 0 : Math.max(1, (int) (this.baseDamage * total));
    }
}
