package ch.zhaw.dialektly.config;

/**
 * Central configuration constants for the Dialektly application.
 * All values are {@code static final} and should be referenced by name
 * rather than inlined so that future tuning only requires changes here.
 */
public class AppConfig {

    private AppConfig() {
        // utility class, not instantiable
    }

    /** XP awarded for each correctly mastered vocabulary item in a round. */
    public static final int XP_PER_CORRECT = 10;

    /** XP deducted for each exercise that required at least one wrong attempt. */
    public static final int XP_PENALTY_PER_MISTAKE = 3;

    /**
     * Minimum cumulative XP required to reach each level.
     * Index {@code i} is the threshold for level {@code i + 1}, so
     * level 1 starts at 0, level 2 at 100, level 3 at 250, etc.
     */
    public static final int[] LEVEL_THRESHOLDS = {0, 100, 250, 450, 700};
}