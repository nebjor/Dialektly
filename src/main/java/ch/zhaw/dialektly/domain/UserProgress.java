package ch.zhaw.dialektly.domain;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Tracks a user's learning status across all topics, their overall XP
 * total, and which badges they've earned. All state is mutated in-place and
 * serialised with the owning user by {@link ch.zhaw.dialektly.Storage}.
 */
public class UserProgress {

    private final Set<VocabularyItem> learnedItems;
    private final Map<TopicCategory, TopicProgress> topicProgress;
    private final Set<Badge> badges;
    private int xp;
    private Set<VocabularyItem> learnedTextItems;
    private Set<VocabularyItem> learnedAudioItems;

    /**
     * Creates a new, empty progress record with zero XP and no learned items.
     */
    public UserProgress() {
        this.learnedItems = new HashSet<>();
        this.topicProgress = new EnumMap<>(TopicCategory.class);
        this.badges = new HashSet<>();
        this.xp = 0;
    }

    /**
     * Marks the given vocabulary item as learned and increments the
     * corresponding topic's learned counter. If the item was already marked
     * learned, this call is a no-op.
     *
     * @param item the mastered item to record
     */
    public void markLearned(VocabularyItem item) {
        if (!learnedItems.contains(item)) {
            learnedItems.add(item);
            // verify TopicProgress exists
            topicProgress.computeIfAbsent(
                    item.getTopic(),
                    t -> new TopicProgress(t, Integer.MAX_VALUE)
            );
            topicProgress.get(item.getTopic()).incrementLearned();
        }
    }

    /**
     * Marks the given item as learned for a specific exercise type and
     * increments the corresponding per-type counter in {@link TopicProgress}.
     * Also delegates to {@link #markLearned(VocabularyItem)} to keep the
     * combined set and badge counters in sync.
     *
     * @param item the mastered item
     * @param type the exercise type through which it was mastered
     */
    public void markLearnedByType(VocabularyItem item, ExerciseType type) {
        markLearned(item);
        TopicProgress tp = topicProgress.get(item.getTopic());
        if (type == ExerciseType.TEXT_INPUT) {
            if (learnedTextItems == null) learnedTextItems = new HashSet<>();
            if (learnedTextItems.add(item) && tp != null) tp.incrementLearnedText();
        } else if (type == ExerciseType.AUDIO_TO_TEXT) {
            if (learnedAudioItems == null) learnedAudioItems = new HashSet<>();
            if (learnedAudioItems.add(item) && tp != null) tp.incrementLearnedAudio();
        }
    }

    /**
     * Returns whether the given vocabulary item has been learned.
     *
     * @param item the item to check; must not be {@code null}
     * @return {@code true} if the item is in the learned set
     * @throws IllegalArgumentException if {@code item} is {@code null}
     */
    public boolean isLearned(VocabularyItem item) {
        if (item == null) throw new IllegalArgumentException("VocabularyItem is null");
        return learnedItems.contains(item);
    }

    /**
     * Returns the average learning percentage across all topics that have
     * been started, or {@code 0.0} if no topic has been started yet.
     *
     * @return average topic completion percentage in {@code [0.0, 100.0]}
     */
    public double getOverallPercentage() {
        TopicCategory[] all = TopicCategory.values();
        if (all.length == 0) return 0.0;
        double sum = 0.0;
        for (TopicCategory topic : all) {
            TopicProgress tp = topicProgress.get(topic);
            if (tp != null) sum += tp.getCombinedPercentage();
        }
        return sum / all.length;
    }

    /**
     * Returns whether every tracked topic has been fully completed.
     *
     * @return {@code true} if all topic records report completion and at least
     * one topic has been started
     */
    public boolean isCompleted() {
        for (TopicCategory topic : TopicCategory.values()) {
            TopicProgress tp = topicProgress.get(topic);
            if (tp == null || !tp.isCompleted()) return false;
        }
        return true;
    }

    /**
     * Initialises a {@link TopicProgress} entry for the given topic if none
     * exists yet. Idempotent — safe to call multiple times with the same topic.
     *
     * @param topic      the topic to initialise
     * @param totalWords the total vocabulary count for that topic
     */
    public void ensureTopicInitialized(TopicCategory topic, int totalWords) {
        topicProgress.putIfAbsent(topic, new TopicProgress(topic, totalWords));
    }

    /**
     * Returns the live set of all vocabulary items the user has learned.
     *
     * @return the learned-items set; do not mutate directly
     */
    public Set<VocabularyItem> getLearnedItems() {
        return learnedItems;
    }

    /**
     * Returns the per-topic progress map keyed by {@link TopicCategory}.
     *
     * @return the topic-progress map; do not mutate directly
     */
    public Map<TopicCategory, TopicProgress> getTopicProgress() {
        return topicProgress;
    }

    /**
     * Returns the live set of badges the user has earned. Mutating the
     * returned set updates this user's earned badges.
     *
     * @return the user's earned badges
     */
    public Set<Badge> getBadges() {
        return badges;
    }

    /**
     * Adds XP to the user's total. Negative or zero values are ignored.
     *
     * @param amount the XP delta to add (must be positive to take effect)
     */
    public void addXp(int amount) {
        if (amount > 0) this.xp += amount;
    }

    /**
     * Returns the total accumulated XP for this user.
     *
     * @return total XP (always &ge; 0)
     */
    public int getXp() {
        return xp;
    }

    /**
     * Inspects this progress, awards any newly-earned badges by adding
     * them, and returns the badges that were just added.
     *
     * @return badges newly awarded by this call, empty if none
     */
    public List<Badge> checkAndAwardBadges() {
        List<Badge> newlyAwarded = new ArrayList<>();
        for (Badge badge : Badge.values()) {
            if (badge.isEarned(this) && badges.add(badge)) {
                newlyAwarded.add(badge);
            }
        }
        return newlyAwarded;
    }
}
