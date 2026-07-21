package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.domain.VocabularyItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Low-level persistence: reads and writes the user list and vocabulary list
 * to the data directory. Holds no in-memory state — every call reads from
 * or writes to the file fresh.
 */
public class Storage {

    private static final String USERS_FILE = "users.json";
    private final Path dataDir;
    private final Gson gson;

    /**
     * Creates the storage helper for the given data directory, ensuring the
     * directory exists.
     *
     * @param dataDir the directory under which {@code users.json} is read and written
     */
    public Storage(Path dataDir) {
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(UUID.class, new UUIDAdapter())
                .setPrettyPrinting()
                .create();

        ensureDataDirExists();
    }

    /**
     * Loads users from {@code users.json} in the data directory.
     * Returns an empty list if the file does not exist yet (first launch).
     *
     * @return the loaded users; empty if the file is missing
     */
    public List<User> loadUsers() {
        Path usersFile = dataDir.resolve(USERS_FILE);
        if (!Files.exists(usersFile)) {
            return List.of();
        }
        try (Reader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) {
            User[] users = gson.fromJson(reader, User[].class);
            return users != null ? Arrays.asList(users) : List.of();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read users.json", e);
        }
    }

    /**
     * Serializes the given user list to {@code users.json} in the data directory.
     *
     * @param users the users to persist
     */
    public void saveUsers(List<User> users) {
        Path usersFile = dataDir.resolve(USERS_FILE);
        try {
            Files.writeString(usersFile, gson.toJson(users), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write users.json", e);
        }
    }

    /**
     * Loads vocabulary from {@code vocabulary.json} in the data directory.
     *
     * @return the loaded vocabulary items
     */
    public List<VocabularyItem> loadVocabulary() {
        Path vocabularyFile = Path.of("data", "vocabulary.json");
        try (Reader reader = Files.newBufferedReader(vocabularyFile, StandardCharsets.UTF_8)) {
            VocabularyItem[] items = gson.fromJson(reader, VocabularyItem[].class);
            return items != null ? Arrays.asList(items) : List.of();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read vocabulary.json", e);
        }
    }

    /**
     * Returns the data directory configured for this storage instance.
     *
     * @return the data directory path
     */
    public Path getDataDir() {
        return dataDir;
    }

    /**
     * Custom Gson TypeAdapter for {@link UUID} since Gson cannot
     * handle UUID serialization out of the box.
     */
    static class UUIDAdapter extends TypeAdapter<UUID> {
        @Override
        public void write(JsonWriter out, UUID value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public UUID read(JsonReader in) throws IOException {
            String value = in.nextString();
            return value == null ? null : UUID.fromString(value);
        }
    }

    /**
     * Creates the data directory if it does not already exist.
     */
    private void ensureDataDirExists() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create data directory: " + dataDir, e);
        }
    }
}
