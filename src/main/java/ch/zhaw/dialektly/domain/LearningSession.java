package ch.zhaw.dialektly.domain;

import java.util.*;

/**
 * A single learning round for one topic.
 * Manages an exercise queue and tracks which vocabulary items the user has
 * mastered. An item is kept in the queue until {@link AbstractExercise#isMastered()}
 * returns {@code true}; only then is it counted as mastered and removed.
 */
public class LearningSession {

    private final UUID sessionId;
    private final TopicCategory topic;
    private final ExerciseType exerciseType;
    private final int totalItems;
    private final Deque<Exercise> queue;
    private final Set<VocabularyItem> masteredItems;
    private final Set<VocabularyItem> attemptedItems;
    private final Set<VocabularyItem> correctOnceItems;
    private final Set<VocabularyItem> firstTryCorrectItems;

    /**
     * Creates a new session for the given topic, vocabulary list, and exercise format.
     *
     * @param topic the topic category for this session
     * @param items the vocabulary items to include; must not be {@code null}
     * @param type  the exercise format; must be {@link ExerciseType#supported()} and not {@code null}
     * @throws NullPointerException          if any argument is {@code null}
     * @throws UnsupportedOperationException if {@code type} is not supported
     */
    public LearningSession(TopicCategory topic, List<VocabularyItem> items, ExerciseType type) {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(type, "type");
        if (!type.supported()) throw new UnsupportedOperationException();

        this.sessionId = UUID.randomUUID();
        this.topic = topic;
        this.exerciseType = type;
        this.totalItems = items.size();
        this.masteredItems = new HashSet<>();
        this.attemptedItems = new HashSet<>();
        this.correctOnceItems = new HashSet<>();
        this.firstTryCorrectItems = new HashSet<>();
        this.queue = new ArrayDeque<>();

        for (VocabularyItem item : items) {
            queue.add(type.createExercise(item));
        }
    }

    /**
     * Returns the next exercise in the queue without removing it.
     *
     * @return the current exercise, or an empty {@link Optional} when the queue is empty
     */
    public Optional<Exercise> getCurrentExercise() {
        return Optional.ofNullable(queue.peek());
    }

    /**
     * Submits an answer for the current exercise, updates mastery state, and
     * re-queues the exercise if not yet mastered.
     *
     * @param answer the user's raw input
     * @return {@code true} if the answer was correct, {@code false} otherwise
     */
    public boolean submitAnswer(String answer) {
        if (Objects.equals(answer, "")) return false;
        Exercise current = queue.poll();
        if (current == null) return false;

        boolean isFirstAttempt = attemptedItems.add(current.getItem());
        boolean correct = current.submitAnswer(answer);

        if (correct) {
            correctOnceItems.add(current.getItem());
        } else {
            firstTryCorrectItems.remove(current.getItem());
        }
        if (isFirstAttempt && correct) {
            firstTryCorrectItems.add(current.getItem());
        }

        if (current.isMastered()) {
            masteredItems.add(current.getItem());
        } else {
            queue.addLast(current);
        }

        return correct;
    }

    /**
     * Returns whether all items in this session have been mastered.
     *
     * @return {@code true} when the exercise queue is empty
     */
    public boolean isCompleted() {
        return queue.isEmpty();
    }

    /**
     * Returns the proportion of mastered items as a percentage.
     *
     * @return a value in {@code [0.0, 100.0]}, or {@code 0.0} if the session has no items
     */
    public double completedRatio() {
        if (totalItems == 0) return 0.0;
        return (double) masteredItems.size() / totalItems * 100.0;
    }

    /**
     * Returns the unique identifier for this session.
     *
     * @return the session UUID
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * Returns the topic category this session covers.
     *
     * @return the topic
     */
    public TopicCategory getTopic() {
        return topic;
    }

    /**
     * Returns the exercise type used in this session (TEXT_INPUT or AUDIO_TO_TEXT).
     */
    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    /**
     * Returns the total number of vocabulary items in this session.
     *
     * @return total item count (fixed at construction time)
     */
    public int getTotalItems() {
        return totalItems;
    }

    /**
     * Returns how many items have been answered correctly at least once
     * (including fully mastered items).
     */
    public int getCorrectOnceCount() {
        return correctOnceItems.size();
    }

    /**
     * Returns how many items have been mastered so far.
     *
     * @return mastered item count
     */
    public int getMasteredCount() {
        return masteredItems.size();
    }

    /**
     * Returns the set of vocabulary items the user has fully mastered.
     *
     * @return live mastered-items set; do not mutate
     */
    public Set<VocabularyItem> getMasteredItems() {
        return masteredItems;
    }

    /**
     * Returns the set of items the user answered correctly on their very first attempt.
     * Only these items are eligible to be marked as "learned" in the user's progress.
     */
    public Set<VocabularyItem> getFirstTryCorrectItems() {
        return firstTryCorrectItems;
    }

    public int getFirstTryCorrectCount() {
        return firstTryCorrectItems.size();
    }

    /**
     * First-try accuracy as a percentage of total items. Used by the round summary
     * to express how well the user knew the vocabulary going in (independent of
     * how often a wrong item had to be repeated to reach mastery).
     *
     * @return percentage in {@code [0.0, 100.0]}, or {@code 0.0} if the session is empty
     */
    public double firstTryRatio() {
        if (totalItems == 0) return 0.0;
        return (double) firstTryCorrectItems.size() / totalItems * 100.0;
    }
}