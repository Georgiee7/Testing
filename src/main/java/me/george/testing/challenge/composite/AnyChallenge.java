package me.george.testing.challenge.composite;

import lombok.Getter;
import me.george.testing.challenge.ActionType;
import me.george.testing.challenge.Challenge;
import me.george.testing.challenge.ChallengeAction;
import me.george.testing.utils.Utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A challenge completed as soon as any one of its parts is. Every part is active from the start, and the progress shown is
 * the progress of the part closest to completing.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class AnyChallenge extends Challenge {
    private final List<Challenge> parts;

    public AnyChallenge(Challenge... parts) {
        this(List.of(parts));
    }

    public AnyChallenge(List<Challenge> parts) {
        Utils.verify(parts != null && !parts.isEmpty(), "Cannot make an any-of challenge with no parts.");
        this.parts = List.copyOf(parts);
    }

    @Override
    protected boolean doOffer(ChallengeAction action) {
        if (isComplete())
            return false;

        boolean progressed = false;
        for (Challenge part : this.parts) {
            if (part.offer(action))
                progressed = true; // An action can progress several parts: a Fire type knocked out is a Pokémon knocked out too.

            if (part.isComplete())
                break; // Done, so the parts after it must not complete (and run their callbacks) as well.
        }

        return progressed;
    }

    @Override
    protected void doActivate() {
        for (Challenge part : this.parts)
            if (!part.isComplete())
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
            if (part.isComplete())
                return true;

        return false;
    }

    @Override
    public int getProgress() {
        return isComplete() ? getGoal() : getLeadingPart().getProgress();
    }

    @Override
    public int getGoal() {
        return getLeadingPart().getGoal();
    }

    // The part closest to completing, which the progress and goal both come from.
    private Challenge getLeadingPart() {
        return this.parts.stream()
                .min(Comparator.comparingInt(Challenge::getProgressLeft))
                .orElse(this.parts.get(0));
    }

    @Override
    public String getDescription() {
        return this.parts.stream()
                .map(Challenge::getDescription)
                .map(Challenge::stripTrailingPeriod)
                .collect(Collectors.joining(" OR ")) + ".";
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
