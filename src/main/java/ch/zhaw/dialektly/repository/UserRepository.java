package ch.zhaw.dialektly.repository;

import ch.zhaw.dialektly.Storage;
import ch.zhaw.dialektly.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds the user list in memory and delegates persistence to {@link Storage}.
 * Created once on application start and injected into every component that
 * works with users.
 */
public class UserRepository {

    private final Storage storage;
    private final List<User> users;

    /**
     * Loads the user list from the given storage and keeps it in memory.
     *
     * @param storage the persistence backend; must not be {@code null}
     */
    public UserRepository(Storage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.users = new ArrayList<>(storage.loadUsers());
    }

    /**
     * Returns a list of all registered usernames in the order they were added.
     *
     * @return a new list of usernames; never {@code null}
     */
    public List<String> getAllUsernames() {
        List<String> names = new ArrayList<>(users.size());
        for (User u : users) {
            names.add(u.getUsername());
        }
        return names;
    }

    /**
     * Looks up a user by exact username (case-sensitive).
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user, or empty if not found
     */
    public Optional<User> findByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    /**
     * Adds a new user to the in-memory list.
     * The change is not persisted until {@link #save()} is called.
     *
     * @param user the user to add; must not be {@code null}
     */
    public void add(User user) {
        users.add(user);
    }

    /** Persists the current in-memory state. */
    public void save() {
        storage.saveUsers(users);
    }
}
