package me.george.testing.instanced.battle.event;

import lombok.Getter;
import me.george.testing.entity.pokemon.Pokemon;
import me.george.testing.instanced.battle.Battle;

/**
 * Fired when a Pokémon faints in a battle.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class FaintEvent extends BattleEvent {
    private final Pokemon pokemon;
    private final Pokemon cause; // The Pokémon that made it faint, may be null.

    /**
     * Makes the event.
     *
     * @param battle  The battle.
     * @param pokemon The Pokémon that fainted.
     * @param cause   The Pokémon that made it faint, may be null.
     */
    public FaintEvent(Battle battle, Pokemon pokemon, Pokemon cause) {
        super(battle);
        this.pokemon = pokemon;
        this.cause = cause;
    }
}
