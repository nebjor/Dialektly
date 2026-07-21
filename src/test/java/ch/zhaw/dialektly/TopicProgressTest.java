package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.TopicProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicProgressTest {

    private TopicProgress progress;

    @BeforeEach
    void setUp() {
        progress = new TopicProgress(TopicCategory.ALLTAG, 10);
    }

    // AeK-29 / AeK-30 – getPercentage()
    @Test
    void getPercentage_noProgress_returnsZero() {
        assertEquals(0.0, progress.getPercentage());
    }

    @Test
    void getPercentage_halfLearned_returnsFifty() {
        for (int i = 0; i < 5; i++) progress.incrementLearned();
        assertEquals(50.0, progress.getPercentage());
    }

    @Test
    void getPercentage_allLearned_returnsHundred() {
        for (int i = 0; i < 10; i++) progress.incrementLearned();
        assertEquals(100.0, progress.getPercentage());
    }

    // AeK-34 – Division by zero
    @Test
    void getPercentage_totalWordsZero_returnsZero() {
        TopicProgress empty = new TopicProgress(TopicCategory.ALLTAG, 0);
        assertEquals(0.0, empty.getPercentage());
    }

    // AeK-31 / AeK-32 – isCompleted()
    @Test
    void isCompleted_allLearned_returnsTrue() {
        for (int i = 0; i < 10; i++) progress.incrementLearned();
        assertTrue(progress.isCompleted());
    }

    @Test
    void isCompleted_notAllLearned_returnsFalse() {
        for (int i = 0; i < 9; i++) progress.incrementLearned();
        assertFalse(progress.isCompleted());
    }

    // AeK-35 – empty topic is not completed
    @Test
    void isCompleted_totalWordsZero_returnsFalse() {
        TopicProgress empty = new TopicProgress(TopicCategory.ALLTAG, 0);
        assertFalse(empty.isCompleted());
    }

    // AeK-33: no overflow in totalWords
    @Test
    void incrementLearned_beyondTotal_doesNotOverflow() {
        for (int i = 0; i < 15; i++) progress.incrementLearned();
        assertEquals(10, progress.getLearnedCount());
    }

    @Test
    void isCompleted_oneBeforeTotal_returnsFalse() {
        for (int i = 0; i < 9; i++) progress.incrementLearned();
        assertFalse(progress.isCompleted());
    }

    @Test
    void isCompleted_exactlyAtTotal_returnsTrue() {
        for (int i = 0; i < 10; i++) progress.incrementLearned();
        assertTrue(progress.isCompleted());
    }
}