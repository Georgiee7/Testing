package me.george.testing.challenge;

import me.george.testing.entity.player.Player;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.move.MoveType;
import me.george.testing.instanced.battle.Battle;

/**
 * Something a player did that challenges can count, made from the battle event it happened in.
 * Created by George K. on 11/27/2026.
 *
 * @param type    What kind of action it was.
 * @param player  The player who did it, through their Pokémon.
 * @param battle  The battle it happened in.
 * @param pokemon The player's Pokémon that did it, null for actions like winning a battle.
 * @param target  The Pokémon it was done to, such as the one knocked out. May be null.
 * @param move    The move it was done with, may be null.
 * @param amount  How much of it was done, such as the damage dealt. 1 for actions that aren't amounts.
 */
public record ChallengeAction(ActionType type, Player player, Battle battle, Pokemon pokemon, Pokemon target, MoveType move, int amount) {}
