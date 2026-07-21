package ch.zhaw.dialektly.domain;

import java.util.Objects;

/**
 * Base class for all exercise types.
 * Tracks consecutive correct answers and declares an item mastered once
 * {@link #MASTERED_THRESHOLD} consecutive correct answers have been submitted.
 * A wrong answer resets the streak to zero.
 *
 * <p>Subclasses must implement {@link #isCorrect(String)} to define what
 * constitutes a correct answer for their exercise format.</p>
 */
public abstract class AbstractExercise implements Exercise {

    private static final int MASTERED_THRESHOLD = 2;

    /** The vocabulary item this exercise is based on. */
    protected final VocabularyItem item;
    /** The exercise format (e.g. {@link ExerciseType#TEXT_INPUT}). */
    protected final ExerciseType type;
    private int correctCount;

    /**
     * Constructs the exercise for the given vocabulary item and exercise type.
     *
     * @param item the vocabulary item this exercise is based on
     * @param type the exercise type (e.g. TEXT_INPUT)
     */
    protected AbstractExercise(VocabularyItem item, ExerciseType type) {
        this.item = Objects.requireNonNull(item, "item");
        this.type = Objects.requireNonNull(type, "type");
        this.correctCount = 0;
    }

    /**
     * Evaluates the submitted answer. Increments the consecutive-correct
     * counter on success; resets it to zero on failure.
     *
     * @param answer the user's raw input
     * @return {@code true} if the answer is correct, {@code false} otherwise
     */
    @Override
    public boolean submitAnswer(String answer) {
        if (isCorrect(answer)) {
            correctCount++;
            return true;
        } else {
            correctCount = 0;
            return false;
        }
    }

    /**
     * Determines whether the given answer is correct for this exercise.
     * Comparison semantics (case-insensitive, trimmed, etc.) are defined
     * by the concrete subclass.
     *
     * @param answer the user's raw input; may be {@code null}
     * @return {@code true} if the answer is accepted as correct
     */
    protected abstract boolean isCorrect(String answer);

    /**
     * Normalizes a string for comparison by converting to lowercase and stripping
     * all non-letter characters (like punctuation and spaces).
     *
     * @param text the string to normalize
     * @return the normalized string
     */
    protected String normalize(String text) {
        if (text == null) return "";
        return text.replaceAll("[^a-zA-ZäöüÄÖÜß]", "").toLowerCase();
    }

    /**
     * {@inheritDoc}
     * Returns {@code true} once the user has answered correctly
     * {@value #MASTERED_THRESHOLD} consecutive times.
     */
    @Override
    public boolean isMastered() {
        return correctCount >= MASTERED_THRESHOLD;
    }

    /** {@inheritDoc} */
    @Override
    public VocabularyItem getItem() { return item; }

    /** {@inheritDoc} */
    @Override
    public ExerciseType getType() { return type; }

    /**
     * Returns the current consecutive-correct-answer streak.
     *
     * @return number of consecutive correct answers since the last mistake
     */
    public int getCorrectCount() { return correctCount; }
}