package me.george.testing.entity.pokemon.move;

/**
 * The categories of moves, deciding which stats their damage uses.
 * Created by George K. on 11/27/2026.
 */
public enum MoveCategory {
    PHYSICAL, // The attacker's attack against the defender's defense.
    SPECIAL, // The attacker's special attack against the defender's special defense.
    STATUS // Deals no damage.
}
