package me.george.testing.instanced.battle.event;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.entity.pokemon.move.base.Move;
import me.george.testing.instanced.battle.Battle;

/**
 * Fired when a Pokémon is about to use a move. Cancelling it stops the move, using up the turn.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class MoveUseEvent extends BattleEvent implements Cancellable {
    private final Pokemon attacker;
    private final Pokemon target;
    private final Move move;
    @Setter
    private boolean cancelled;

    /**
     * Makes the event.
     *
     * @param battle   The battle.
     * @param attacker The Pokémon using the move.
     * @param target   The Pokémon it's used on.
     * @param move     The move.
     */
    public MoveUseEvent(Battle battle, Pokemon attacker, Pokemon target, Move move) {
        super(battle);
        this.attacker = attacker;
        this.target = target;
        this.move = move;
    }
}
