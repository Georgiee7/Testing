package me.george.testing.instanced.battle.event;

import lombok.Getter;
import lombok.Setter;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.instanced.battle.Battle;

/**
 * Fired when a Pokémon's turn starts, before it picks a move. Cancelling it skips the turn (such as when it's paralyzed).
 * Created by George K. on 11/27/2026.
 */
@Getter
public class TurnStartEvent extends BattleEvent implements Cancellable {
    private final Pokemon pokemon;
    @Setter
    private boolean cancelled;

    /**
     * Makes the event.
     *
     * @param battle  The battle.
     * @param pokemon The Pokémon whose turn it is.
     */
    public TurnStartEvent(Battle battle, Pokemon pokemon) {
        super(battle);
        this.pokemon = pokemon;
    }
}
