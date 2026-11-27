package me.george.testing.analytics;

import java.io.IOException;
import java.util.List;

/**
 * Where analytics rows end up, such as a file or a database.
 * Created by George K. on 11/27/2026.
 */
@FunctionalInterface
public interface AnalyticsSink {

    /**
     * Sends a batch of rows, each one a JSON object.
     * Throwing an IOException means the sink couldn't be reached, so the batch is kept and tried again later, waiting longer each time.
     * Throwing anything else means the sink refused the batch, which is dropped after a few tries so it can't hold up the rows after it.
     *
     * @param batchId The batch's id, the same on every try, so a sink like a database can skip a batch an earlier try already saved.
     * @param rows    The rows.
     * @throws IOException if the sink couldn't be reached.
     */
    void send(String batchId, List<String> rows) throws IOException;
}
