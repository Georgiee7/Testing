package me.george.testing.manager;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests managers stopping in the right order, even when one of them fails to.
 * Created by George K. on 11/27/2026.
 */
public class ManagerLifecycleTest {

    @Test
    public void testManagersStopInReverseOrder() {
        List<String> stopped = new ArrayList<>();
        Manager.addManager(new FirstManager(stopped));
        Manager.addManager(new FailingManager(stopped));
        Manager.addManager(new LastManager(stopped));

        Manager.disableManagers();
        assertEquals(List.of("Last", "Failing", "First"), stopped); // The failing one didn't stop the first from stopping.
    }

    private static class FirstManager extends Manager {
        private final List<String> stopped;

        private FirstManager(List<String> stopped) {
            this.stopped = stopped;
        }

        @Override
        public void onStop() {
            this.stopped.add("First");
        }
    }

    private static class FailingManager extends Manager {
        private final List<String> stopped;

        private FailingManager(List<String> stopped) {
            this.stopped = stopped;
        }

        @Override
        public void onStop() {
            this.stopped.add("Failing");
            throw new IllegalStateException("Failed to stop.");
        }
    }

    private static class LastManager extends Manager {
        private final List<String> stopped;

        private LastManager(List<String> stopped) {
            this.stopped = stopped;
        }

        @Override
        public void onStop() {
            this.stopped.add("Last");
        }
    }
}
