package ch.zhaw.dialektly.domain.exercises;

import ch.zhaw.dialektly.domain.AbstractExercise;
import ch.zhaw.dialektly.domain.ExerciseType;
import ch.zhaw.dialektly.domain.VocabularyItem;

/**
 * An exercise in which the user is shown a dialect word and must type its
 * standard German (Hochdeutsch) equivalent. The answer is compared
 * case-insensitively and with leading/trailing whitespace ignored.
 */
public class TextInputExercise extends AbstractExercise {

    /**
     * Creates a text-input exercise for the given vocabulary item.
     *
     * @param item the vocabulary item to be translated
     */
    public TextInputExercise(VocabularyItem item) {
        super(item, ExerciseType.TEXT_INPUT);
    }

    /**
     * Accepts the answer if it matches the item's standard German word,
     * ignoring case and surrounding whitespace.
     *
     * @param answer the user's typed input; {@code null} is treated as wrong
     * @return {@code true} if the trimmed, case-insensitive answer equals the standard word
     */
    @Override
    protected boolean isCorrect(String answer) {
        if (answer == null || answer.isEmpty()) return false;
        return normalize(answer).equals(normalize(item.getStandardWord()));
    }
}