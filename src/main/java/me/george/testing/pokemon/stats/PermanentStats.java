package me.george.testing.pokemon.stats;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A wrapper around permanent stats a Pokémon may have.
 * Created by George K. on 4/13/2024.
 */
@Getter
public class PermanentStats {

    public static final int MIN_EV_STAT = 1;
    public static final int MAX_EV_STAT = 255;

    private final Map<PermanentStat, Integer> stats;
    private boolean initialized;

    public PermanentStats() {
        this(new HashMap<>());
        randomizeStats();
    }

    protected PermanentStats(Map<PermanentStat, Integer> stats) {
        this.stats = stats;
        this.initialized = !stats.isEmpty();
        if (!initialized)
            initializeStats();
    }

    /**
     * Gets a specific stat's value
     *
     * @param stat The stat to get the value of.
     * @return statValue
     */
    public int getStat(PermanentStat stat) {
        return this.stats.getOrDefault(stat, MIN_EV_STAT);
    }

    /**
     * Sets a stat to a specific value.
     *
     * @param stat The stat to update.
     * @param value The value to update it with.
     */
    public void setStat(PermanentStat stat, int value) {
        int newValue = Math.min(value, MAX_EV_STAT);
        this.stats.put(stat, Math.max(newValue, MIN_EV_STAT));
    }

    /**
     * Increases a specific stat by a value, up to its maximum.
     *
     * @param stat  The stat to increase.
     * @param value The value to increase it by.
     */
    public void increaseStat(PermanentStat stat, int value) {
        int newValue = Math.min(getStat(stat) + value, MAX_EV_STAT);
        this.stats.put(stat, Math.max(newValue, MIN_EV_STAT));
    }

    /**
     * Decreases a specific stat by a value, up to 0.
     *
     * @param stat  The stat to decrease.
     * @param value The value to decrease it by.
     */
    public void decreaseStat(PermanentStat stat, int value) {
        int newValue = Math.max(getStat(stat) - value, MIN_EV_STAT);
        this.stats.put(stat, Math.min(newValue, MAX_EV_STAT));
    }

    private void initializeStats() {
        Arrays.stream(PermanentStat.values()).forEach(stat -> this.stats.put(stat, MIN_EV_STAT));
        this.initialized = true;
    }

    private void randomizeStats() {
        this.stats.replaceAll((stat, value) -> ThreadLocalRandom.current().nextInt(MAX_EV_STAT + 1));
    }
}
