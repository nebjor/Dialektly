package ch.zhaw.dialektly.service;

import ch.zhaw.dialektly.config.AppConfig;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.UserProgress;

/**
 * Stateless service that calculates and applies XP earned during a learning round.
 * XP is awarded per mastered item and reduced by a penalty for each item that
 * required at least one incorrect attempt. The final value is clamped to zero
 * (it can never go negative).
 */
public class XPService {

    /** Default no-arg constructor — the service holds no state. */
    public XPService() {}

    /**
     * Calculates the XP earned for a completed {@link LearningSession}.
     *
     * @param session the completed session; must not be {@code null}
     * @return the non-negative XP earned in this round
     */
    public int calculateRoundXP(LearningSession session) {
        int correct = session.getMasteredCount();
        // Exercises where attempts > 1 is the total items minus those gotten right on the first try
        int exercisesWithMistakes = session.getTotalItems() - session.getFirstTryCorrectCount();

        int earnedXp = (correct * AppConfig.XP_PER_CORRECT) - (exercisesWithMistakes * AppConfig.XP_PENALTY_PER_MISTAKE);

        return Math.max(earnedXp, 0);
    }

    /**
     * Calculates XP from raw counts, provided for unit testing without a full
     * {@link LearningSession} instance.
     *
     * @param correctCount          the number of mastered items
     * @param exercisesWithMistakes the number of items that needed at least one retry
     * @return the non-negative XP earned
     */
    public int calculateRoundXP(int correctCount, int exercisesWithMistakes) {
        int earnedXp = (correctCount * AppConfig.XP_PER_CORRECT) - (exercisesWithMistakes * AppConfig.XP_PENALTY_PER_MISTAKE);
        return Math.max(earnedXp, 0);
    }

    /**
     * Adds xp to progress.totalPoints (mapped to getXp()/addXp() in UserProgress).
     * Does not touch level — level is always computed by LevelService.
     *
     * @param progress the user's progress to mutate
     * @param xp       the amount of XP to add; non-positive values are ignored
     */
    public void applyXP(UserProgress progress, int xp) {
        if (xp > 0) {
            progress.addXp(xp);
        }
    }
}