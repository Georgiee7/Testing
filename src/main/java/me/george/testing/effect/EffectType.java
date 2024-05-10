package me.george.testing.effect;

/**
 * A registry of all battle effects.
 * Created by George K. on 5/11/2024.
 */
public enum EffectType {
    PARALYSIS, // Movement impairment
    POISON, // DoT
    BAD_POISON, // Scaling DoT
    BURN, // DoT
    FREEZE, // Movement impairment
    FLINCH, // Lose turn
    CONFUSION, // May last 1-4 turns, 50% chance to attack itself every turn it attacks.
    LEECH_SEED; // Every turn leech 1/8HP from the target (rounded down but min of 1) and restore it to caster.
}
