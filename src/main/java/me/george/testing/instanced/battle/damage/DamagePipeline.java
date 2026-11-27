package me.george.testing.instanced.battle.damage;

import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.move.MoveCategory;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.entity.pokemon.move.base.DamagingMove;
import me.george.testing.entity.pokemon.move.base.Move;
import me.george.testing.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Calculates the damage of moves: the base damage from the attacker's and defender's stats, then each modifier's multiplier in order.
 * Every battle has its own pipeline, so it can add modifiers of its own (such as weather) without changing other battles.
 * Created by George K. on 11/27/2026.
 */
public class DamagePipeline {

    private final List<DamageModifier> modifiers = new CopyOnWriteArrayList<>();

    /**
     * Creates a pipeline with the default modifiers.
     *
     * @return defaultPipeline
     */
    public static DamagePipeline createDefault() {
        DamagePipeline pipeline = new DamagePipeline();
        for (DefaultDamageModifier modifier : DefaultDamageModifier.values())
            pipeline.addModifier(modifier);

        return pipeline;
    }

    /**
     * Adds a modifier to the end of the pipeline.
     *
     * @param modifier The modifier to add.
     * @return this, to add more.
     */
    public DamagePipeline addModifier(DamageModifier modifier) {
        Utils.verifyNotNull(modifier, "Tried to add a null damage modifier.");
        this.modifiers.add(modifier);
        return this;
    }

    /**
     * Removes a modifier from the pipeline.
     *
     * @param modifier The modifier to remove.
     * @return this, to remove more.
     */
    public DamagePipeline removeModifier(DamageModifier modifier) {
        this.modifiers.remove(modifier);
        return this;
    }

    /**
     * Gets the modifiers of this pipeline, in the order they're applied.
     *
     * @return modifiers
     */
    public List<DamageModifier> getModifiers() {
        return Collections.unmodifiableList(this.modifiers);
    }

    /**
     * Calculates the damage of a move from one Pokémon to another.
     *
     * @param attacker The Pokémon using the move.
     * @param defender The Pokémon it's used on.
     * @param move     The move.
     * @return damageCalculation
     */
    public DamageCalculation calculate(Pokemon attacker, Pokemon defender, Move move) {
        Utils.verifyNotNull(attacker, "Tried to calculate the damage of a null attacker.");
        Utils.verifyNotNull(defender, "Tried to calculate the damage to a null defender.");
        Utils.verifyNotNull(move, "Tried to calculate the damage of a null move.");

        MoveType type = move.getType();
        int power = move instanceof DamagingMove damagingMove ? damagingMove.getPower(type) : 0;
        if (power <= 0 || type.getCategory() == MoveCategory.STATUS) {
            DamageCalculation calculation = new DamageCalculation(attacker, defender, move, 0);
            calculation.finish();
            return calculation; // Deals no damage, so there's nothing to modify.
        }

        boolean physical = type.getCategory() == MoveCategory.PHYSICAL;
        int attack = physical ? attacker.getAttack() : attacker.getSpAttack();
        int defense = physical ? defender.getDefense() : defender.getSpDefense();

        DamageCalculation calculation = new DamageCalculation(attacker, defender, move, calculateBaseDamage(attacker.getLevel(), power, attack, defense));
        for (DamageModifier modifier : this.modifiers)
            calculation.applyMultiplier(modifier, modifier.getMultiplier(calculation));

        calculation.finish();
        return calculation;
    }

    /**
     * Calculates the base damage of a hit with the games' formula, before any modifiers.
     *
     * @param level   The attacker's level.
     * @param power   The move's power.
     * @param attack  The attacker's attack (or special attack).
     * @param defense The defender's defense (or special defense).
     * @return baseDamage
     */
    public static int calculateBaseDamage(int level, int power, int attack, int defense) {
        return ((2 * level / 5 + 2) * power * attack / Math.max(defense, 1)) / 50 + 2;
    }
}
