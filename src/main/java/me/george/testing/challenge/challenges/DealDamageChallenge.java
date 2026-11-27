package me.george.testing.challenge.challenges;

import lombok.Getter;
import me.george.testing.challenge.ActionType;
import me.george.testing.challenge.ChallengeAction;
import me.george.testing.challenge.CountChallenge;
import me.george.testing.entity.pokemon.Element;
import me.george.testing.utils.Utils;

/**
 * A challenge to deal an amount of damage, optionally only with moves of an element. Every hit adds the damage it dealt.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class DealDamageChallenge extends CountChallenge<DealDamageChallenge> {
    private final Element element; // Null for moves of any element.

    public DealDamageChallenge(int goal, Element element) {
        super(ActionType.DEAL_DAMAGE, goal);
        this.element = element;
    }

    public DealDamageChallenge(int goal) {
        this(goal, null);
    }

    @Override
    protected boolean matches(ChallengeAction action) {
        return this.element == null || (action.move() != null && action.move().getElement() == this.element);
    }

    @Override
    protected int getProgressGained(ChallengeAction action) {
        return action.amount();
    }

    @Override
    protected String makeDescription() {
        return "Deal " + getGoal() + " damage" + (this.element != null ? " with " + Utils.capitalize(this.element.name()) + " moves" : "") + ".";
    }
}
