package me.george.testing.pokemon.stats;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A wrapper around permanent stats a Pokémon may have.
 * Created by George K. on 4/13/2024.
 */
@Getter
public class PermanentStats {

    public static final int MAX_EV_STATS = 255;

    private final Map<PermanentStat, Integer> stats;

    public PermanentStats() {
        this.stats = new HashMap<>();
        randomizeStats();
    }

    protected PermanentStats(Map<PermanentStat, Integer> stats) {
        this.stats = stats;
    }

    public void setStat(PermanentStat stat, int value) {
        this.stats.put(stat, value);
    }

    /**
     * Gets a specific stat's value
     *
     * @param stat The stat to get the value of.
     * @return statValue
     */
    public int getStat(PermanentStat stat) {
        return this.stats.getOrDefault(stat, 0);
    }

    /**
     * Increases a specific stat by a value, up to its maximum.
     *
     * @param stat  The stat to increase.
     * @param value The value to increase it by.
     */
    public void increaseStat(PermanentStat stat, int value) {
        this.stats.put(stat, getStat(stat) + value);
    }

    /**
     * Decreases a specific stat by a value, up to 0.
     *
     * @param stat  The stat to decrease.
     * @param value The value to decrease it by.
     */
    public void decreaseStat(PermanentStat stat, int value) {
        this.stats.put(stat, getStat(stat) - value);
    }

    /**
     * Randomizes all stats.
     */
    public void randomizeStats() {
        this.stats.replaceAll((stat, value) -> ThreadLocalRandom.current().nextInt(MAX_EV_STATS + 1));
    }
}
