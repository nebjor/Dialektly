package ch.zhaw.dialektly.domain;

/**
 * Achievement badges awarded to users for reaching learning milestones.
 * The {@link #getId()} value (kebab-case, e.g. {@code "first-round"}) is the
 * stable external identifier used in the spec; the {@link #getDisplayKey()}
 * value is the i18n bundle key for the user-facing label. Use the enum value
 * itself for in-process logic.
 */
public enum Badge {

    /** Awarded after the user completes their first learning round. */
    FIRST_ROUND("first-round", "badge.firstRound") {
        @Override
        public boolean isEarned(UserProgress progress) {
            return !progress.getLearnedItems().isEmpty();
        }
    },
    /** Awarded when total earned XP reaches 50. */
    XP_50("xp-50", "badge.xp50") {
        @Override
        public boolean isEarned(UserProgress progress) {
            return progress.getXp() >= 50;
        }
    },
    /** Awarded when total earned XP reaches 100. */
    XP_100("xp-100", "badge.xp100") {
        @Override
        public boolean isEarned(UserProgress progress) {
            return progress.getXp() >= 100;
        }
    },
    /** Awarded when total earned XP reaches 250. */
    XP_250("xp-250", "badge.xp250") {
        @Override
        public boolean isEarned(UserProgress progress) {
            return progress.getXp() >= 250;
        }
    },
    /** Awarded when the user fully completes any one topic. */
    TOPIC_COMPLETE("topic-complete", "badge.topicComplete") {
        @Override
        public boolean isEarned(UserProgress progress) {
            return progress.getTopicProgress().values().stream().anyMatch(TopicProgress::isCompleted);
        }
    },
    /** Awarded when every topic has been fully completed. */
    ALL_TOPICS("all-topics", "badge.allTopics") {
        @Override
        public boolean isEarned(UserProgress progress) {
            return progress.isCompleted();
        }
    };

    private final String id;
    private final String displayKey;

    /**
     * Binds the spec-defined kebab-case id and i18n display key to the enum constant.
     *
     * @param id         the kebab-case identifier from the badge spec
     * @param displayKey the resource-bundle key for the user-facing label
     */
    Badge(String id, String displayKey) {
        this.id = id;
        this.displayKey = displayKey;
    }

    /**
     * Returns the kebab-case identifier defined in the spec.
     *
     * @return the spec id, e.g. {@code "first-round"}
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the i18n bundle key for this badge's user-facing label.
     *
     * @return the resource-bundle key, e.g. {@code "badge.firstRound"}
     */
    public String getDisplayKey() {
        return displayKey;
    }

    /**
     * Evaluates whether this badge has been earned based on the given progress.
     *
     * @param progress the user's progress
     * @return true if the milestone is reached, false otherwise
     */
    public abstract boolean isEarned(UserProgress progress);
}
