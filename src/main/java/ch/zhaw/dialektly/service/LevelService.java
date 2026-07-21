package ch.zhaw.dialektly.service;

import ch.zhaw.dialektly.config.AppConfig;
import ch.zhaw.dialektly.domain.UserProgress;

/**
 * Stateless service that derives the user's current level and progress-to-next-level
 * from their accumulated XP using the thresholds defined in {@link ch.zhaw.dialektly.config.AppConfig}.
 */
public class LevelService {

    /** Default no-arg constructor — the service holds no state. */
    public LevelService() {}

    /**
     * Calculates the current level for the given total XP.
     * Returns the highest level whose threshold does not exceed {@code totalPoints}.
     *
     * @param totalPoints the user's total accumulated XP
     * @return the current level (1-based); always at least 1
     */
    public int calculateLevel(int totalPoints) {
        int level = 1;
        for (int i = 0; i < AppConfig.LEVEL_THRESHOLDS.length; i++) {
            if (totalPoints >= AppConfig.LEVEL_THRESHOLDS[i]) {
                level = i + 1;
            } else {
                break;
            }
        }
        return level;
    }

    /**
     * Returns the fraction of XP earned toward the next level, suitable for
     * driving a progress bar.
     *
     * @param progress the user's progress containing the current XP value
     * @return a value in {@code [0.0, 1.0]}; {@code 1.0} if the user is at max level
     */
    public double getProgressToNextLevel(UserProgress progress) {
        int totalPoints = progress.getXp();
        int currentLevel = calculateLevel(totalPoints);

        if (currentLevel >= AppConfig.LEVEL_THRESHOLDS.length) {
            return 1.0; // Max level reached
        }

        int currentLevelBaseXp = AppConfig.LEVEL_THRESHOLDS[currentLevel - 1];
        int nextLevelThresholdXp = AppConfig.LEVEL_THRESHOLDS[currentLevel];

        int pointsIntoCurrentLevel = totalPoints - currentLevelBaseXp;
        int pointsNeededForNextLevel = nextLevelThresholdXp - currentLevelBaseXp;

        return (double) pointsIntoCurrentLevel / pointsNeededForNextLevel;
    }
}