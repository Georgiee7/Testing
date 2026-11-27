package me.george.testing.challenge.composite;

import lombok.Getter;
import me.george.testing.challenge.ActionType;
import me.george.testing.challenge.Challenge;
import me.george.testing.challenge.ChallengeAction;
import me.george.testing.utils.Utils;

import java.util.Iterator;
import java.util.List;

/**
 * A challenge whose parts must be completed one after another, in order. Only the current part is active, activated when the
 * sequence reaches it, and the progress and description are the current part's.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class SequenceChallenge extends Challenge {
    private final List<Challenge> parts;
    private int currentIndex;

    public SequenceChallenge(Challenge... parts) {
        this(List.of(parts));
    }

    public SequenceChallenge(List<Challenge> parts) {
        Utils.verify(parts != null && !parts.isEmpty(), "Cannot make a sequence challenge with no parts.");
        this.parts = List.copyOf(parts);
    }

    @Override
    protected boolean doOffer(ChallengeAction action) {
        if (isComplete())
            return false;

        Challenge current = this.parts.get(this.currentIndex);
        boolean progressed = current.offer(action);
        if (current.isComplete()) {
            this.currentIndex++;
            if (!isComplete())
                this.parts.get(this.currentIndex).activate(); // On to the next part.
        }

        return progressed;
    }

    @Override
    protected void doActivate() {
        if (!isComplete()) // The current part, the first one unless the challenge was restored further along.
            this.parts.get(this.currentIndex).activate();
    }

    @Override
    public boolean listensTo(ActionType type) {
        return !isComplete() && this.parts.get(this.currentIndex).listensTo(type); // Only the current part can use actions.
    }

    /**
     * Gets the part being worked on.
     *
     * @return currentPart, null once the sequence is complete.
     */
    public Challenge getCurrent() {
        return isComplete() ? null : this.parts.get(this.currentIndex);
    }

    @Override
    public boolean isComplete() {
        return this.currentIndex >= this.parts.size();
    }

    @Override
    public int getProgress() {
        return isComplete() ? getGoal() : getCurrent().getProgress();
    }

    @Override
    public int getGoal() {
        return isComplete() ? 1 : getCurrent().getGoal();
    }

    @Override
    public String getDescription() {
        return isComplete() ? "Complete." : getCurrent().getDescription();
    }

    @Override
    protected void doSaveState(List<Integer> state) {
        state.add(this.currentIndex);
        for (Challenge part : this.parts)
            part.saveState(state);
    }

    @Override
    protected void doRestoreState(Iterator<Integer> state) {
        this.currentIndex = Math.max(0, Math.min(this.parts.size(), state.next()));
        for (Challenge part : this.parts)
            part.restoreState(state);
    }

    @Override
    protected void doReset() {
        this.currentIndex = 0;
        this.parts.forEach(Challenge::reset);
    }
}
