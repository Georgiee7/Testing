package me.george.testing.challenge;

import java.util.Iterator;
import java.util.List;

/**
 * A challenge a player works towards by doing matching actions in battles, like winning battles or knocking out Fire types.
 * Leaf challenges ({@link CountChallenge} and its subclasses) count actions, and the composites in the composite package combine
 * them: all of them, any of them, or one after another. Challenges hold their own progress, so every player gets their own tree.
 * <p>
 * Two optional callbacks drive what happens around it: {@link #onActivate(Runnable)} runs once, when the challenge becomes the
 * active one (it's given out, or a sequence reaches it), and {@link #onComplete(Runnable)} runs once, the moment it completes, to give a reward.
 * Created by George K. on 11/27/2026.
 */
public abstract class Challenge {
    private Runnable activationCallback;
    private Runnable completionCallback;
    private boolean activated;
    private boolean completionFired;

    /**
     * Offers an action to this challenge, progressing it (or a part of it) if the action matches, and running the completion
     * callback if this completes it. Subclasses only write how they take the action, in doOffer.
     *
     * @param action The action that was done.
     * @return whether any progress was made.
     */
    public final boolean offer(ChallengeAction action) {
        boolean progressed = doOffer(action);
        if (progressed && !this.completionFired && isComplete()) {
            this.completionFired = true;
            if (this.completionCallback != null)
                this.completionCallback.run();
        }

        return progressed;
    }

    /**
     * Takes an action, progressing this challenge (or a part of it) if the action matches.
     *
     * @param action The action that was done.
     * @return whether any progress was made.
     */
    protected abstract boolean doOffer(ChallengeAction action);

    /**
     * Activates this challenge, running its activation callback the first time. Composites pass it on to the parts that become active.
     */
    public final void activate() {
        if (this.activated)
            return;

        this.activated = true;
        if (this.activationCallback != null)
            this.activationCallback.run();

        doActivate();
    }

    /**
     * Passes activation on to the parts of this challenge. Leaves have none.
     */
    protected void doActivate() {

    }

    /**
     * Whether this challenge (or any part of it) uses actions of a type at all, so actions it doesn't care about are skipped.
     *
     * @param type The type of action.
     * @return listensTo
     */
    public abstract boolean listensTo(ActionType type);

    /**
     * Whether this challenge has been completed.
     *
     * @return isComplete
     */
    public abstract boolean isComplete();

    /**
     * Whether this challenge's completion callback has run, because it completed or was restored as complete.
     *
     * @return completionFired
     */
    public final boolean isCompletionFired() {
        return this.completionFired;
    }

    /**
     * Gets this challenge's progress towards its goal.
     *
     * @return progress
     */
    public abstract int getProgress();

    /**
     * Gets the progress needed to complete this challenge.
     *
     * @return goal
     */
    public abstract int getGoal();

    /**
     * Gets the progress left before this challenge is complete.
     *
     * @return progressLeft
     */
    public int getProgressLeft() {
        return Math.max(0, getGoal() - getProgress());
    }

    /**
     * Describes what this challenge needs right now, for the player.
     *
     * @return description
     */
    public abstract String getDescription();

    /**
     * Writes this challenge's progress (and its parts') as numbers, so it can be saved and continued later, like after a restart.
     * Only a challenge built the same way can read it back.
     *
     * @param state The list to add the numbers to.
     */
    public final void saveState(List<Integer> state) {
        doSaveState(state);
    }

    /**
     * Restores progress written by saveState into a newly built challenge. The completion callbacks of the parts already done
     * don't run again. Activation runs again on activate, so the current step can set itself back up.
     *
     * @param state The saved numbers, in order.
     */
    public final void restoreState(Iterator<Integer> state) {
        doRestoreState(state);
        this.completionFired = isComplete();
    }

    /**
     * Writes this challenge's own progress, then its parts'.
     *
     * @param state The list to add the numbers to.
     */
    protected abstract void doSaveState(List<Integer> state);

    /**
     * Reads back what doSaveState wrote, in the same order.
     *
     * @param state The saved numbers.
     */
    protected abstract void doRestoreState(Iterator<Integer> state);

    /**
     * Resets this challenge's progress back to the start, so its callbacks can run again.
     */
    public final void reset() {
        this.activated = false;
        this.completionFired = false;
        doReset();
    }

    /**
     * Resets this challenge's own progress back to the start.
     */
    protected abstract void doReset();

    /**
     * Sets a callback to run once, the moment this challenge becomes active.
     *
     * @param callback The callback to run.
     * @return thisChallenge
     */
    public Challenge onActivate(Runnable callback) {
        this.activationCallback = callback;
        return this;
    }

    /**
     * Sets a callback to run once, the moment this challenge completes.
     *
     * @param callback The callback to run.
     * @return thisChallenge
     */
    public Challenge onComplete(Runnable callback) {
        this.completionCallback = callback;
        return this;
    }

    /**
     * Strips the period at the end of a description, to fit it into a longer one, like a composite joining its parts'.
     *
     * @param description The description to strip.
     * @return strippedDescription
     */
    public static String stripTrailingPeriod(String description) {
        return description.endsWith(".") ? description.substring(0, description.length() - 1) : description;
    }
}
