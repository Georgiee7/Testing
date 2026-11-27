package me.george.testing.analytics;

import me.george.testing.instanced.battle.event.BattleEventManager;
import me.george.testing.instanced.battle.event.RegisteredListener;
import me.george.testing.manager.Manager;
import me.george.testing.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles analytics: what happens in battles, written as rows of JSON to a sink in the background.
 * It's off until it's enabled with a sink. While it's off its listeners aren't registered and tracking does nothing, so it costs nothing.
 * Created by George K. on 11/27/2026.
 */
public class AnalyticsManager extends Manager {

    private static final List<RegisteredListener<?>> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile AnalyticsWriter writer;

    @Override
    public void onStop() {
        disable();
    }

    /**
     * Enables analytics, writing to a sink.
     *
     * @param sink Where the rows go.
     */
    public static synchronized void enable(AnalyticsSink sink) {
        Utils.verify(!isEnabled(), "Analytics is already enabled.");
        writer = new AnalyticsWriter(sink);
        LISTENERS.addAll(BattleEventManager.register(new BattleAnalytics()));
    }

    /**
     * Disables analytics, sending the rows still queued first. Does nothing if it's already off.
     */
    public static synchronized void disable() {
        if (writer == null)
            return;

        LISTENERS.forEach(BattleEventManager::unlisten);
        LISTENERS.clear();
        writer.stop();
        writer = null;
    }

    /**
     * Whether analytics is enabled.
     *
     * @return isEnabled
     */
    public static boolean isEnabled() {
        return writer != null;
    }

    /**
     * Starts a row of analytics about something that happened.
     *
     * @param event What happened, such as "damage".
     * @return row, which does nothing if analytics is off.
     */
    public static AnalyticsRow track(String event) {
        return isEnabled() ? new AnalyticsRow(event) : AnalyticsRow.NONE;
    }

    /**
     * Gets the writer sending the rows, to see how many were sent or dropped.
     *
     * @return writer, null if analytics is off.
     */
    public static AnalyticsWriter getWriter() {
        return writer;
    }

    // Queues a finished row, if analytics is still on.
    static void offer(String row) {
        AnalyticsWriter current = writer;
        if (current != null)
            current.offer(row);
    }
}
