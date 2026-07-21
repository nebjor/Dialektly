package ch.zhaw.dialektly;

import ch.zhaw.dialektly.service.LevelService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelServiceTest {

    private final LevelService levelService = new LevelService();

    @Test
    void testCalculateLevel_Level1() {
        // 99 XP -> level 1
        int level = levelService.calculateLevel(99);
        assertEquals(1, level, "99 XP should remain at Level 1");
    }

    @Test
    void testCalculateLevel_Level2() {
        // 100 XP -> level 2
        int level = levelService.calculateLevel(100);
        assertEquals(2, level, "100 XP should promote to Level 2");
    }
}