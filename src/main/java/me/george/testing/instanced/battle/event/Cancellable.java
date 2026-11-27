package me.george.testing.instanced.battle.event;

/**
 * An event that listeners can cancel, stopping what it's about to do.
 * Created by George K. on 11/27/2026.
 */
public interface Cancellable {

    /**
     * Whether the event has been cancelled.
     *
     * @return isCancelled
     */
    boolean isCancelled();

    /**
     * Cancels the event, or un-cancels it.
     *
     * @param cancelled Whether it should be cancelled.
     */
    void setCancelled(boolean cancelled);
}
