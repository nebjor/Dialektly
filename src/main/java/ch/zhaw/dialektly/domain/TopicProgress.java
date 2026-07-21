package ch.zhaw.dialektly.domain;

import java.util.Objects;

/**
 * Tracks a user's learning progress for a single {@link TopicCategory}.
 * Counts how many of the topic's vocabulary items the user has mastered
 * and exposes derived metrics (percentage, completion status).
 */
public class TopicProgress {

    private final TopicCategory topic;
    private final int totalWords;
    private int learnedCount;
    private int learnedTextCount;
    private int learnedAudioCount;

    /**
     * Creates a new progress record for the given topic.
     *
     * @param topic      the topic this record tracks
     * @param totalWords the total number of vocabulary items in scope for this topic
     */
    public TopicProgress(TopicCategory topic, int totalWords) {
        this.topic = Objects.requireNonNull(topic, "topic");
        this.totalWords = totalWords;
        this.learnedCount = 0;
    }

    /**
     * Returns the proportion of learned words as a percentage.
     *
     * @return a value in {@code [0.0, 100.0]}, or {@code 0.0} if {@code totalWords} is zero
     */
    public double getPercentage() {
        if (totalWords == 0) return 0.0;
        return (double) learnedCount / totalWords * 100.0;
    }

    /**
     * Returns whether every word in this topic has been learned.
     *
     * @return {@code true} if {@code learnedCount >= totalWords} and the topic is non-empty
     */
    public boolean isCompleted() {
        return totalWords > 0 && learnedCount >= totalWords;
    }

    /**
     * Increments the learned-word counter by one, up to {@code totalWords}.
     * Extra calls beyond the total are silently ignored.
     */
    public void incrementLearned() {
        if (learnedCount < totalWords) {
            learnedCount++;
        }
    }

    /** Increments the text-exercise learned counter (capped at totalWords). */
    public void incrementLearnedText() {
        if (learnedTextCount < totalWords) learnedTextCount++;
    }

    /** Increments the audio-exercise learned counter (capped at totalWords). */
    public void incrementLearnedAudio() {
        if (learnedAudioCount < totalWords) learnedAudioCount++;
    }

    /**
     * Returns the combined progress across both exercise types as a percentage.
     * 100 % is only reached when all items have been learned via text AND audio.
     */
    public double getCombinedPercentage() {
        if (totalWords == 0) return 0.0;
        double textPct  = (double) learnedTextCount  / totalWords * 100.0;
        double audioPct = (double) learnedAudioCount / totalWords * 100.0;
        return (textPct + audioPct) / 2.0;
    }

    /**
     * Returns {@code true} only when every item has been learned via both
     * text and audio exercises.
     */
    public boolean isBothCompleted() {
        return totalWords > 0
                && learnedTextCount  >= totalWords
                && learnedAudioCount >= totalWords;
    }

    /**
     * Returns the topic this progress record belongs to.
     *
     * @return the topic category
     */
    public TopicCategory getTopic() {
        return topic;
    }

    /**
     * Returns the number of words the user has learned in this topic.
     *
     * @return learned word count
     */
    public int getLearnedCount() {
        return learnedCount;
    }
}
