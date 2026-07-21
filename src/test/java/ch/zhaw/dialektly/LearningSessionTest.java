package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.ExerciseType;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.VocabularyItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningSessionTest {

    @Mock
    private VocabularyItem item1;
    @Mock
    private VocabularyItem item2;
    @Mock
    private VocabularyItem item3;

    private LearningSession session;

    @BeforeEach
    void setUp() {
        session = new LearningSession(TopicCategory.ALLTAG,
                List.of(item1, item2, item3), ExerciseType.TEXT_INPUT);
    }

    // AeK-10 – valid session created
    @Test
    void constructor_validArgs_queueFilledCorrectly() {
        assertEquals(3, session.getTotalItems());
        assertFalse(session.isCompleted());
        assertTrue(session.getMasteredItems().isEmpty());
    }

    // AeK-11 – empty vocabulary list
    @Test
    void constructor_emptyItems_isCompletedImmediately() {
        LearningSession empty = new LearningSession(
                TopicCategory.ALLTAG, List.of(), ExerciseType.TEXT_INPUT);
        assertTrue(empty.isCompleted());
        assertEquals(0, empty.getTotalItems());
    }

    // AeK-12 – topic null
    @Test
    void constructor_nullTopic_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new LearningSession(null, List.of(item1), ExerciseType.TEXT_INPUT));
    }

    // AeK-13 – items null
    @Test
    void constructor_nullItems_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new LearningSession(TopicCategory.ALLTAG, null, ExerciseType.TEXT_INPUT));
    }

    // AeK-14 – TEXT_TO_VOICE not supported
    @Test
    void constructor_textToVoiceType_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () ->
                new LearningSession(TopicCategory.ALLTAG, List.of(item1), ExerciseType.TEXT_TO_VOICE));
    }

    // AeK-01 – correct answer, first attempt correct
    @Test
    void submitAnswer_correctOnce_returnsTrueNotMastered() {
        when(item1.getStandardWord()).thenReturn("Hallo");

        assertTrue(session.submitAnswer("Hallo"));
        assertFalse(session.isCompleted());
        assertEquals(0, session.getMasteredCount());
    }

    // AeK-02 – two correct answers → exercise mastered
    @Test
    void submitAnswer_correctTwice_itemMastered() {
        when(item1.getStandardWord()).thenReturn("Hallo");
        LearningSession singleSession = new LearningSession(
                TopicCategory.ALLTAG, List.of(item1), ExerciseType.TEXT_INPUT);

        singleSession.submitAnswer("Hallo");
        singleSession.submitAnswer("Hallo");

        assertEquals(1, singleSession.getMasteredCount());
        assertTrue(singleSession.getMasteredItems().contains(item1));
    }

    // AeK-04 – wrong answer → exercise moved to end of queue
    @Test
    void submitAnswer_wrongAnswer_returnsFalseExerciseRequeued() {
        assertFalse(session.submitAnswer("Falsch"));
        assertFalse(session.isCompleted());
        assertEquals(0, session.getMasteredCount());
        assertSame(item2, session.getCurrentExercise().orElseThrow().getItem());
    }

    // AeK-05 – empty answer
    @Test
    void submitAnswer_emptyAnswer_returnsFalse() {
        assertFalse(session.submitAnswer(""));
        assertEquals(0, session.getMasteredCount());
    }

    // AeK-06 – null answer
    @Test
    void submitAnswer_nullAnswer_returnsFalse() {
        assertFalse(session.submitAnswer(null));
        assertEquals(0, session.getMasteredCount());
    }

    // AeK-03 – Case-insensitive
    @Test
    void submitAnswer_correctAnswerDifferentCase_returnsTrue() {
        when(item1.getStandardWord()).thenReturn("Hallo");
        LearningSession singleSession = new LearningSession(
                TopicCategory.ALLTAG, List.of(item1), ExerciseType.TEXT_INPUT);

        assertTrue(singleSession.submitAnswer("hallo"));
        assertTrue(singleSession.submitAnswer("HALLO"));
    }

    // AeK-07 – session completed
    @Test
    void isCompleted_allItemsMastered_returnsTrue() {
        when(item1.getStandardWord()).thenReturn("Hallo");
        when(item2.getStandardWord()).thenReturn("Tschüss");
        when(item3.getStandardWord()).thenReturn("Danke");

        session.submitAnswer("Hallo");
        session.submitAnswer("Tschüss");
        session.submitAnswer("Danke");
        session.submitAnswer("Hallo");
        session.submitAnswer("Tschüss");
        session.submitAnswer("Danke");

        assertTrue(session.isCompleted());
        assertEquals(3, session.getMasteredCount());
    }

    // AeK-08 – session not yet completed
    @Test
    void isCompleted_itemsRemaining_returnsFalse() {
        when(item1.getStandardWord()).thenReturn("Hallo");

        session.submitAnswer("Hallo");
        assertFalse(session.isCompleted());
    }

    // completedRatio()
    @Test
    void completedRatio_noItemsMastered_returnsZero() {
        assertEquals(0.0, session.completedRatio());
    }

    @Test
    void completedRatio_allMastered_returnsHundred() {
        when(item1.getStandardWord()).thenReturn("Hallo");
        when(item2.getStandardWord()).thenReturn("Tschüss");
        when(item3.getStandardWord()).thenReturn("Danke");

        session.submitAnswer("Hallo");
        session.submitAnswer("Tschüss");
        session.submitAnswer("Danke");
        session.submitAnswer("Hallo");
        session.submitAnswer("Tschüss");
        session.submitAnswer("Danke");

        assertEquals(100.0, session.completedRatio());
    }

    @Test
    void completedRatio_oneOfThreeMastered_returnsCorrectRatio() {
        when(item1.getStandardWord()).thenReturn("Hallo");
        when(item2.getStandardWord()).thenReturn("Tschüss");
        when(item3.getStandardWord()).thenReturn("Danke");

        session.submitAnswer("Hallo");
        session.submitAnswer("a");
        session.submitAnswer("b");
        session.submitAnswer("Hallo");

        assertEquals(100.0 / 3.0, session.completedRatio(), 0.01);
    }

    @Test
    void constructor_singleItem_masteredAfterTwoCorrect() {
        when(item1.getStandardWord()).thenReturn("Hallo");

        LearningSession single = new LearningSession(
                TopicCategory.ALLTAG, List.of(item1), ExerciseType.TEXT_INPUT);
        single.submitAnswer("Hallo");
        single.submitAnswer("Hallo");
        assertTrue(single.isCompleted());
    }

    @Test
    void constructor_sessionIdNotNull() {
        assertNotNull(session.getSessionId());
    }

    @Test
    void constructor_topicStoredCorrectly() {
        assertEquals(TopicCategory.ALLTAG, session.getTopic());
    }

    @Test
    void submitAnswer_wrongAnswer_exerciseMovedToEndOfQueue() {
        when(item1.getStandardWord()).thenReturn("Hallo");

        session.submitAnswer("Falsch");
        assertSame(item2, session.getCurrentExercise().orElseThrow().getItem());
    }
}
