package ch.zhaw.dialektly.service;

import ch.zhaw.dialektly.domain.Badge;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.domain.VocabularyItem;
import ch.zhaw.dialektly.repository.UserRepository;

import java.util.List;
import java.util.Objects;

/**
 * Service orchestrating operations related to an active or completing
 * {@link LearningSession}.
 */
public class SessionService {

    private final UserRepository userRepository;
    private final XPService xpService;

    public SessionService(UserRepository userRepository, XPService xpService) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.xpService = Objects.requireNonNull(xpService);
    }

    /**
     * Completes the given session by mapping its current achievements into the
     * user's overall progress profile. Persists the changes to the user repository.
     *
     * @param user    the user whose progress is to be updated
     * @param session the session whose results are being saved
     * @return a list of badges newly awarded as a direct consequence of this save operation
     */
    public List<Badge> persistProgress(User user, LearningSession session) {
        int total = session.getTotalItems();
        user.getProgress().ensureTopicInitialized(session.getTopic(), total);

        for (VocabularyItem mastered : session.getMasteredItems()) {
            user.getProgress().markLearned(mastered);
        }

        int earnedXp = xpService.calculateRoundXP(session);
        xpService.applyXP(user.getProgress(), earnedXp);

        List<Badge> newlyAwarded = user.getProgress().checkAndAwardBadges();
        userRepository.save();

        return newlyAwarded;
    }
}
