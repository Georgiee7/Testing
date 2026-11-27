package me.george.testing.analytics;

import me.george.testing.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * An analytics sink that adds every row to a file as a line of JSON, so it can be read back line by line.
 * Created by George K. on 11/27/2026.
 */
public class JsonLinesFileSink implements AnalyticsSink {
    private final Path file;

    public JsonLinesFileSink(Path file) {
        Utils.verifyNotNull(file, "Tried to make a file sink with no file.");
        this.file = file;
    }

    @Override
    public void send(String batchId, List<String> rows) throws IOException {
        Path folder = this.file.toAbsolutePath().getParent();
        if (folder != null)
            Files.createDirectories(folder);

        Files.write(this.file, rows, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
