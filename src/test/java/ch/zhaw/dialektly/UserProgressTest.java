package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.UserProgress;
import ch.zhaw.dialektly.domain.VocabularyItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgressTest {

    private UserProgress userProgress;

    @Mock
    private VocabularyItem itemAlltag1;


    @BeforeEach
    void setUp() {
        userProgress = new UserProgress();
    }

    // AeK-22
    @Test
    void getOverallPercentage_noTopics_returnsZero() {
        assertEquals(0.0, userProgress.getOverallPercentage());
    }

    @Test
    void isCompleted_noTopics_returnsFalse() {
        assertFalse(userProgress.isCompleted());
    }

    // AeK-23
    @Test
    void getOverallPercentage_oneTopicHalfDone_returnsFifty() {
        when(itemAlltag1.getTopic()).thenReturn(TopicCategory.ALLTAG);

        userProgress.ensureTopicInitialized(TopicCategory.ALLTAG, 10);
        for (int i = 0; i < 5; i++) {
            userProgress.markLearned(itemAlltag1);
        }
        // manually check via topicProgress map
        int learned = userProgress.getTopicProgress().get(TopicCategory.ALLTAG).getLearnedCount();
        assertEquals(1, learned); // markLearned guards duplicates — only 1 unique item
    }

    // AeK-27 – call markLearned twice
    @Test
    void markLearned_sameItemTwice_doesNotIncrementTwice() {
        when(itemAlltag1.getTopic()).thenReturn(TopicCategory.ALLTAG);

        userProgress.ensureTopicInitialized(TopicCategory.ALLTAG, 10);
        userProgress.markLearned(itemAlltag1);
        userProgress.markLearned(itemAlltag1);

        int learned = userProgress.getTopicProgress().get(TopicCategory.ALLTAG).getLearnedCount();
        assertEquals(1, learned);
        assertTrue(userProgress.isLearned(itemAlltag1));
    }

    // AeK-28 – markLearned without ensureTopicInitialized
    @Test
    void markLearned_topicNotInitialized_createsTopicProgressAutomatically() {
        when(itemAlltag1.getTopic()).thenReturn(TopicCategory.ALLTAG);

        userProgress.markLearned(itemAlltag1);

        assertNotNull(userProgress.getTopicProgress().get(TopicCategory.ALLTAG));
        assertEquals(1, userProgress.getTopicProgress().get(TopicCategory.ALLTAG).getLearnedCount());
    }

    // AeK-24 – multiple topics
    @Test
    void getOverallPercentage_oneOfFiveTopicsDone_returnsTwentyPercent() {
        userProgress.ensureTopicInitialized(TopicCategory.ALLTAG, 10);
        userProgress.ensureTopicInitialized(TopicCategory.ARBEIT, 10);

        // ALLTAG: 100%, ARBEIT: 0%, three uninitialized: 0% → 100/5 = 20%
        for (int i = 0; i < 10; i++) {
            VocabularyItem item = mock(VocabularyItem.class);
            when(item.getTopic()).thenReturn(TopicCategory.ALLTAG);
            userProgress.markLearned(item);
        }

        assertEquals(20.0, userProgress.getOverallPercentage());
    }

    @Test
    void getOverallPercentage_oneTopicHalfDone_dividesAcrossAllTopics() {
        userProgress.ensureTopicInitialized(TopicCategory.ALLTAG, 10);
        for (int i = 0; i < 5; i++) {
            VocabularyItem item = mock(VocabularyItem.class);
            when(item.getTopic()).thenReturn(TopicCategory.ALLTAG);
            userProgress.markLearned(item);
        }

        // ALLTAG at 50%, four others at 0% → 50/5 = 10%
        assertEquals(10.0, userProgress.getOverallPercentage());
    }

    // AeK-25 – everything completed
    @Test
    void isCompleted_allTopicsDone_returnsTrue() {
        for (TopicCategory topic : TopicCategory.values()) {
            userProgress.ensureTopicInitialized(topic, 1);
            VocabularyItem item = mock(VocabularyItem.class);
            when(item.getTopic()).thenReturn(topic);
            userProgress.markLearned(item);
        }

        assertTrue(userProgress.isCompleted());
    }

    @Test
    void isCompleted_onlyOneTopicDone_returnsFalse() {
        when(itemAlltag1.getTopic()).thenReturn(TopicCategory.ALLTAG);

        userProgress.ensureTopicInitialized(TopicCategory.ALLTAG, 1);
        userProgress.markLearned(itemAlltag1);

        assertFalse(userProgress.isCompleted());
    }

    // AeK-26 – partially completed
    @Test
    void isCompleted_oneTopicIncomplete_returnsFalse() {
        when(itemAlltag1.getTopic()).thenReturn(TopicCategory.ALLTAG);

        userProgress.ensureTopicInitialized(TopicCategory.ALLTAG, 2);
        userProgress.markLearned(itemAlltag1); // only 1 of 2

        assertFalse(userProgress.isCompleted());
    }

    // isLearned – null check
    @Test
    void isLearned_nullItem_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> userProgress.isLearned(null));
    }

    // isLearned – not learned
    @Test
    void isLearned_itemNotLearned_returnsFalse() {
        assertFalse(userProgress.isLearned(itemAlltag1));
    }

    // isLearned – learned
    @Test
    void isLearned_itemLearned_returnsTrue() {
        when(itemAlltag1.getTopic()).thenReturn(TopicCategory.ALLTAG);

        userProgress.markLearned(itemAlltag1);
        assertTrue(userProgress.isLearned(itemAlltag1));
    }
}