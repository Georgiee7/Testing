package me.george.testing.challenge.composite;

import lombok.Getter;
import me.george.testing.challenge.ActionType;
import me.george.testing.challenge.Challenge;
import me.george.testing.challenge.ChallengeAction;
import me.george.testing.utils.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A challenge whose parts must all be completed, in any order. Every part is active from the start, and one action
 * progresses at most one part, so winning one battle doesn't count for two "win a battle" parts.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class AllChallenge extends Challenge {
    private final List<Challenge> parts;

    public AllChallenge(Challenge... parts) {
        this(List.of(parts));
    }

    public AllChallenge(List<Challenge> parts) {
        Utils.verify(parts != null && !parts.isEmpty(), "Cannot make an all-of challenge with no parts.");
        this.parts = List.copyOf(parts);
    }

    @Override
    protected boolean doOffer(ChallengeAction action) {
        for (Challenge part : this.parts)
            if (!part.isComplete() && part.offer(action))
                return true; // One action progresses at most one part.

        return false;
    }

    @Override
    protected void doActivate() {
        for (Challenge part : this.parts)
            if (!part.isComplete()) // A restored challenge's finished parts don't set themselves up again.
                part.activate();
    }

    @Override
    public boolean listensTo(ActionType type) {
        for (Challenge part : this.parts)
            if (part.listensTo(type))
                return true;

        return false;
    }

    @Override
    public boolean isComplete() {
        for (Challenge part : this.parts)
            if (!part.isComplete())
                return false;

        return true;
    }

    @Override
    public int getProgress() {
        return (int) this.parts.stream().filter(Challenge::isComplete).count();
    }

    @Override
    public int getGoal() {
        return this.parts.size();
    }

    @Override
    public String getDescription() {
        String remaining = this.parts.stream()
                .filter(part -> !part.isComplete())
                .map(Challenge::getDescription)
                .map(Challenge::stripTrailingPeriod)
                .collect(Collectors.joining(", "));

        return remaining.isEmpty() ? "Complete." : remaining + ".";
    }

    @Override
    protected void doSaveState(List<Integer> state) {
        for (Challenge part : this.parts)
            part.saveState(state);
    }

    @Override
    protected void doRestoreState(Iterator<Integer> state) {
        for (Challenge part : this.parts)
            part.restoreState(state);
    }

    @Override
    protected void doReset() {
        this.parts.forEach(Challenge::reset);
    }
}
