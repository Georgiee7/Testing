package me.george.testing.instanced;

/**
 * An instanced scenario.
 * Created by George K. on 4/14/2024.
 */
public interface InstancedScenario {

    /**
     * Called when the scenario instance starts.
     */
    void start();

    /**
     * Called when the scenario instance is paused.
     */
    void pause();

    /**
     * Called when the scenario instance is continued.
     */
    void resume();

    /**
     * Called when the scenario instance is finished.
     */
    void finish();

    /**
     * Called when the scenario instance is stopped.
     * Attempts to finish it by default.
     */
    default void stop() {
        finish();
    }
}
