package me.george.testing.challenge.challenges;

import lombok.Getter;
import me.george.testing.challenge.ActionType;
import me.george.testing.challenge.ChallengeAction;
import me.george.testing.challenge.CountChallenge;
import me.george.testing.entity.pokemon.spec.PokemonSpec;

/**
 * A challenge to knock out a number of Pokémon, optionally only the ones matching a spec, like "*[element=FIRE]" for Fire types.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class KnockOutChallenge extends CountChallenge<KnockOutChallenge> {
    private final PokemonSpec spec; // Null for any Pokémon.

    public KnockOutChallenge(int goal, PokemonSpec spec) {
        super(ActionType.KNOCK_OUT, goal);
        this.spec = spec;
    }

    public KnockOutChallenge(int goal) {
        this(goal, null);
    }

    @Override
    protected boolean matches(ChallengeAction action) {
        return this.spec == null || this.spec.matches(action.target());
    }

    @Override
    protected String makeDescription() {
        return "Knock out " + getGoal() + " " + (this.spec != null ? this.spec.describe() : "Pokémon") + ".";
    }
}
