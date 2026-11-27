package me.george.testing.challenge;

import lombok.Getter;
import me.george.testing.utils.Utils;

import java.util.Iterator;
import java.util.List;

/**
 * A challenge completed by counting matching actions of one type up to a goal, the base of every leaf challenge.
 * Subclasses decide which actions match. A custom description can use the {progress}, {goal} and {left} placeholders.
 * Created by George K. on 11/27/2026.
 *
 * @param <S> The challenge's own type, so the chained setters keep returning it.
 */
@Getter
public abstract class CountChallenge<S extends CountChallenge<S>> extends Challenge {
    private final ActionType actionType;
    private final int goal;
    private int progress;
    private String customDescription;

    protected CountChallenge(ActionType actionType, int goal) {
        Utils.verifyNotNull(actionType, "Cannot make a challenge without an action type.");
        Utils.verify(goal > 0, "Cannot make a challenge with a goal of %d, it must be at least 1.", goal);
        this.actionType = actionType;
        this.goal = goal;
    }

    /**
     * Whether an action of this challenge's type counts towards it.
     *
     * @param action The action that was done.
     * @return matches
     */
    protected abstract boolean matches(ChallengeAction action);

    /**
     * Makes the description shown when there's no custom one. Should end with a period.
     *
     * @return description
     */
    protected abstract String makeDescription();

    /**
     * How much a matching action adds to the progress.
     *
     * @param action The matching action.
     * @return progressGained
     */
    protected int getProgressGained(ChallengeAction action) {
        return 1;
    }

    @Override
    protected boolean doOffer(ChallengeAction action) {
        if (isComplete() || action.type() != this.actionType || !matches(action))
            return false;

        this.progress = Math.min(this.goal, this.progress + Math.max(1, getProgressGained(action)));
        return true;
    }

    @Override
    public boolean listensTo(ActionType type) {
        return this.actionType == type;
    }

    @Override
    public boolean isComplete() {
        return this.progress >= this.goal;
    }

    @Override
    public String getDescription() {
        String description = this.customDescription != null ? this.customDescription : makeDescription();
        if (description.indexOf('{') < 0)
            return description;

        return description.replace("{progress}", String.valueOf(getProgress()))
                .replace("{goal}", String.valueOf(getGoal()))
                .replace("{left}", String.valueOf(getProgressLeft()));
    }

    @Override
    protected void doSaveState(List<Integer> state) {
        state.add(this.progress);
    }

    @Override
    protected void doRestoreState(Iterator<Integer> state) {
        this.progress = Math.max(0, Math.min(this.goal, state.next()));
    }

    @Override
    protected void doReset() {
        this.progress = 0;
    }

    /**
     * Replaces the made description. It can use the {progress}, {goal} and {left} placeholders.
     *
     * @param description The description to use, or null for the made one.
     * @return thisChallenge
     */
    public S describedAs(String description) {
        this.customDescription = description;
        return self();
    }

    @Override
    public S onActivate(Runnable callback) {
        super.onActivate(callback);
        return self();
    }

    @Override
    public S onComplete(Runnable callback) {
        super.onComplete(callback);
        return self();
    }

    /**
     * Gets this challenge as its own type, so the chained setters here return the subclass.
     *
     * @return thisChallenge
     */
    @SuppressWarnings("unchecked")
    protected final S self() {
        return (S) this;
    }
}
