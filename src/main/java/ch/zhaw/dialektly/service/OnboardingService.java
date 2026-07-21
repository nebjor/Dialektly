package ch.zhaw.dialektly.service;

import ch.zhaw.dialektly.domain.AppLanguage;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.repository.UserRepository;

import java.util.Objects;

/**
 * Service responsible for user onboarding and account-level updates that need
 * to be persisted. Sits between presenters and {@link UserRepository} so
 * controllers do not call the repository directly.
 *
 * <p>Currently exposes only {@link #updateLanguage(User, AppLanguage)}. The
 * broader onboarding flow (creating new users, loading the first existing
 * user) is tracked separately and may extend this class in a future change.</p>
 */
public class OnboardingService {

    private final UserRepository userRepository;

    /**
     * Creates the service backed by the given user repository.
     *
     * @param userRepository the repository used to persist user changes; must not be {@code null}
     */
    public OnboardingService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }

    /**
     * Updates the given user's preferred application language and persists
     * the change.
     *
     * @param user        the user to update; must not be {@code null}
     * @param newLanguage the new language preference; must not be {@code null}
     */
    public void updateLanguage(User user, AppLanguage newLanguage) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(newLanguage, "newLanguage");
        user.setAppLanguage(newLanguage);
        userRepository.save();
    }
}
