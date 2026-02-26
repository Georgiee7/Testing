package me.george.testing.data.storage.supported;

import me.george.testing.Testing;
import me.george.testing.data.serialization.JsonUtil;
import me.george.testing.utils.GeneralException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generic file-backed JSON repository. Subclasses provide the target directory,
 * the target class for deserialization, and a validation predicate.
 * Created by George K. on 2/26/2026.
 */
public abstract class AbstractFileRepository<T> {
    protected final Path dir;
    private final Class<T> clazz;

    /**
     * Create a repository tied to a filesystem directory and a concrete class type.
     *
     * @param dir   The directory in which JSON files are stored.
     * @param clazz The class to deserialize JSON files into.
     */
    protected AbstractFileRepository(Path dir, Class<T> clazz) {
        this.dir = dir;
        this.clazz = clazz;
    }

    /**
     * Validates a deserialized object. If this returns false the backing file will be moved to a
     * "corrupt" subdirectory and not returned from the repository load methods.
     *
     * @param obj The deserialized object to validate.
     * @return true when the object is valid and may be returned; false otherwise.
     */
    protected abstract boolean isValid(T obj);

    /**
     * Called when a file is determined to be corrupt. Default behaviour is to move it to the given corruptDir.
     * Subclasses can override to perform additional logging or metrics.
     *
     * @param file      the path to the corrupt file
     * @param corruptDir the directory where corrupt files are moved to
     */
    protected void onCorrupt(Path file, Path corruptDir) {
        try {
            if (!Files.exists(corruptDir)) Files.createDirectories(corruptDir);
            Files.move(file, corruptDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            Testing.logInfo("Moved corrupt file to %s", corruptDir.resolve(file.getFileName()));
        } catch (IOException e) {
            throw new GeneralException(e, "Failed to move corrupt file: %s", file);
        }
    }

    /**
     * Atomically persists an object as JSON to the repository directory under the given fileName.
     * On platforms that do not support atomic moves, a non-atomic replace will be used as a fallback.
     *
     * @param obj      The object to serialize.
     * @param fileName The filename to write to (inside the repository directory).
     */
    public void save(T obj, String fileName) {
        try {
            if (!Files.exists(this.dir))
                Files.createDirectories(this.dir);

            Path file = this.dir.resolve(fileName);
            String json = JsonUtil.toPrettyJson(obj);
            Path tmp = Files.createTempFile(this.dir, fileName, ".tmp");
            Files.writeString(tmp, json, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
                // fallback.
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new GeneralException(e, "Failed to persist file: %s", fileName);
        }
    }

    /**
     * Loads and returns all valid JSON-serialized objects from the repository directory. Files which
     * cannot be parsed or which fail validation are moved to a 'corrupt' subdirectory.
     *
     * @return collection of valid deserialized objects
     */
    protected Collection<T> loadAllInternal() {
        try {
            if (!Files.exists(this.dir))
                return List.of();

            List<T> out = new ArrayList<>();
            Path corruptDir = this.dir.resolve("corrupt");
            try (var stream = Files.list(this.dir)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        T obj = JsonUtil.fromJson(content, clazz);
                        if (!isValid(obj)) {
                            onCorrupt(path, corruptDir);
                            return;
                        }

                        out.add(obj);
                    } catch (Exception ex) {
                        onCorrupt(path, corruptDir);
                    }
                });
            }
            return out;
        } catch (IOException e) {
            throw new GeneralException(e, "Failed to load files from disk: %s", this.dir);
        }
    }

    /**
     * Deletes the named file from the repository directory if it exists.
     *
     * @param fileName the name of the file to delete (relative to this repository dir)
     */
    public void remove(String fileName) {
        try {
            Path file = this.dir.resolve(fileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new GeneralException(e, "Failed to delete file: %s", fileName);
        }
    }
}
