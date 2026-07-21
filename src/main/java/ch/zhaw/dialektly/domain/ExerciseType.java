package ch.zhaw.dialektly.domain;

import ch.zhaw.dialektly.domain.exercises.AudioToTextExercise;
import ch.zhaw.dialektly.domain.exercises.TextInputExercise;

/**
 * The format of a vocabulary exercise.
 * Each constant carries a {@link #supported()} flag that indicates whether
 * the exercise type is fully implemented. Unsupported types throw
 * {@link UnsupportedOperationException} when used in a {@link LearningSession}.
 */
public enum ExerciseType {

    /** User types the standard German translation of a displayed dialect word. */
    TEXT_INPUT(true) {
        @Override
        public Exercise createExercise(VocabularyItem item) {
            return new TextInputExercise(item);
        }
    },

    /** User listens to audio and types the dialect word they hear. */
    AUDIO_TO_TEXT(true) {
        @Override
        public Exercise createExercise(VocabularyItem item) {
            return new AudioToTextExercise(item);
        }
    },

    /** User reads a dialect word and records their voice (not yet implemented). */
    TEXT_TO_VOICE(true) {
        @Override
        public Exercise createExercise(VocabularyItem item) {
            throw new UnsupportedOperationException("TEXT_TO_VOICE not yet supported");
        }
    };

    private final boolean supported;

    ExerciseType(boolean supported) {
        this.supported = supported;
    }

    /**
     * Returns whether this exercise type is fully supported by the application.
     *
     * @return {@code true} if this type can be used to create a {@link LearningSession}
     */
    public boolean supported() {
        return supported;
    }

    /**
     * Creates a new Exercise instance of this type.
     */
    public abstract Exercise createExercise(VocabularyItem item);
}
