package ch.zhaw.dialektly.domain;

/**
 * Thematic category that groups vocabulary items.
 * Each category corresponds to a set of entries in the vocabulary data file
 * and is shown as a selectable tile on the topic-picker screen. The
 * {@link #getDisplayKey()} value is the i18n bundle key for the user-facing label.
 */
public enum TopicCategory {
    /** Everyday life / general vocabulary. */
    ALLTAG("topic.alltag"),
    /** Shopping vocabulary. */
    EINKAUFEN("topic.einkaufen"),
    /** Work and profession vocabulary. */
    ARBEIT("topic.arbeit"),
    /** Health and medicine vocabulary. */
    GESUNDHEIT("topic.gesundheit"),
    /** Housing and home vocabulary. */
    WOHNEN("topic.wohnen");

    private final String displayKey;

    TopicCategory(String displayKey) {
        this.displayKey = displayKey;
    }

    /**
     * Returns the i18n bundle key for this topic's user-facing label.
     *
     * @return the resource-bundle key, e.g. {@code "topic.alltag"}
     */
    public String getDisplayKey() {
        return displayKey;
    }
}
