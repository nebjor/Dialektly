package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.ExerciseType;
import ch.zhaw.dialektly.domain.VocabularyItem;
import ch.zhaw.dialektly.domain.exercises.AudioToTextExercise;
import ch.zhaw.dialektly.domain.exercises.TextInputExercise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseTest {

    @Mock
    private VocabularyItem item;

    private TextInputExercise textInputExercise;
    private AudioToTextExercise audioToTextExercise;

    @BeforeEach
    void setUp() {
        textInputExercise = new TextInputExercise(item);
        audioToTextExercise = new AudioToTextExercise(item);
    }

    // AeK-15 – correct answer TEXT_INPUT, first attempt correct
    @Test
    void textInput_correctAnswer_returnsTrue() {
        when(item.getStandardWord()).thenReturn("Hallo");

        assertTrue(textInputExercise.submitAnswer("Hallo"));
    }

    // AeK-16 – two correct answers → isMastered
    @Test
    void textInput_correctAnswerTwice_isMastered() {
        when(item.getStandardWord()).thenReturn("Hallo");

        textInputExercise.submitAnswer("Hallo");
        textInputExercise.submitAnswer("Hallo");
        assertTrue(textInputExercise.isMastered());
    }

    // AeK-18 – wrong answer → correctCount reset to 0
    @Test
    void textInput_wrongAnswer_returnsFalse() {
        assertFalse(textInputExercise.submitAnswer("Falsches Wort"));
    }

    @Test
    void textInput_wrongAnswerAfterOneCorrect_resetsCount() {
        textInputExercise.submitAnswer("Hallo");   // correctCount = 1
        textInputExercise.submitAnswer("Falsch");   // correctCount = 0 (reset)
        textInputExercise.submitAnswer("Hallo");   // correctCount = 1
        assertFalse(textInputExercise.isMastered());
    }

    // AeK-19 – Case-insensitive
    @Test
    void textInput_correctAnswerDifferentCase_returnsTrue() {
        when(item.getStandardWord()).thenReturn("Hallo");

        assertTrue(textInputExercise.submitAnswer("hallo"));
        assertTrue(textInputExercise.submitAnswer("HALLO"));
    }

    // AeK-20 – empty answer
    @Test
    void textInput_emptyAnswer_returnsFalse() {
        assertFalse(textInputExercise.submitAnswer(""));
    }

    // AeK-21 – null answer
    @Test
    void textInput_nullAnswer_returnsFalse() {
        assertFalse(textInputExercise.submitAnswer(null));
    }

    @Test
    void textInput_answerWithWhitespace_returnsTrue() {
        when(item.getStandardWord()).thenReturn("Hallo");

        assertTrue(textInputExercise.submitAnswer("  Hallo  "));
    }

    @Test
    void textInput_oneCorrectAnswer_isNotMastered() {
        textInputExercise.submitAnswer("Hallo");
        assertFalse(textInputExercise.isMastered());
    }

    @Test
    void textInput_twoCorrectAnswers_isMastered() {
        when(item.getStandardWord()).thenReturn("Hallo");

        textInputExercise.submitAnswer("Hallo");
        textInputExercise.submitAnswer("Hallo");
        assertTrue(textInputExercise.isMastered());
    }

    // AeK-17 – correct answer AUDIO_TO_TEXT (dialectWord)
    @Test
    void audioToText_correctDialectWord_returnsTrue() {
        when(item.getStandardWord()).thenReturn("Hallo");
        assertTrue(audioToTextExercise.submitAnswer("Hallo"));
    }

    @Test
    void audioToText_wrongAnswer_returnsFalse() {
        assertFalse(audioToTextExercise.submitAnswer("Hallo")); // standardWord is wrong here
    }

    @Test
    void audioToText_correctAnswerTwice_isMastered() {
        when(item.getStandardWord()).thenReturn("Hallo");
        audioToTextExercise.submitAnswer("Hallo");
        audioToTextExercise.submitAnswer("Hallo");
        assertTrue(audioToTextExercise.isMastered());
    }

    @Test
    void getItem_returnsInjectedItem() {
        assertSame(item, textInputExercise.getItem());
    }

    @Test
    void getType_textInput_returnsCorrectType() {
        assertEquals(ExerciseType.TEXT_INPUT, textInputExercise.getType());
    }

    @Test
    void getType_audioToText_returnsCorrectType() {
        assertEquals(ExerciseType.AUDIO_TO_TEXT, audioToTextExercise.getType());
    }
}