package ch.zhaw.dialektly.domain;

/**
 * Contract for a single vocabulary exercise within a {@link LearningSession}.
 * An exercise wraps one {@link VocabularyItem} and tracks whether the user
 * has mastered it (answered correctly enough consecutive times).
 */
public interface Exercise {

    /**
     * Submits the user's answer and updates the internal mastery state.
     *
     * @param answer the raw user input
     * @return {@code true} if the answer is correct
     */
    boolean submitAnswer(String answer);

    /**
     * Indicates whether the item has been mastered (consecutive correct threshold met).
     *
     * @return {@code true} if the item is mastered and can be removed from the queue
     */
    boolean isMastered();

    /**
     * Returns the vocabulary item this exercise is based on.
     *
     * @return the associated {@link VocabularyItem}
     */
    VocabularyItem getItem();

    /**
     * Returns the exercise format used for this exercise.
     *
     * @return the {@link ExerciseType}
     */
    ExerciseType getType();
}
