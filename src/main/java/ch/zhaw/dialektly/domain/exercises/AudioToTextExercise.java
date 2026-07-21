package ch.zhaw.dialektly.domain.exercises;

import ch.zhaw.dialektly.domain.AbstractExercise;
import ch.zhaw.dialektly.domain.ExerciseType;
import ch.zhaw.dialektly.domain.VocabularyItem;

/**
 * An exercise in which the user listens to an audio clip and types the
 * dialect word they hear. The answer is compared case-insensitively and
 * with leading/trailing whitespace ignored.
 */
public class AudioToTextExercise extends AbstractExercise {

    /**
     * Creates an audio-to-text exercise for the given vocabulary item.
     *
     * @param item the vocabulary item whose audio will be played
     */
    public AudioToTextExercise(VocabularyItem item) {
        super(item, ExerciseType.AUDIO_TO_TEXT);
    }

    /**
     * Accepts the answer if it matches the item's dialect word, ignoring
     * case and surrounding whitespace.
     *
     * @param answer the user's typed input; {@code null} is treated as wrong
     * @return {@code true} if the trimmed, case-insensitive answer equals the dialect word
     */
    @Override
    protected boolean isCorrect(String answer) {
        if (answer == null || answer.isEmpty()) return false;
        return normalize(answer).equals(normalize(item.getStandardWord()));
    }
}