package ch.zhaw.dialektly;

import ch.zhaw.dialektly.service.XPService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class XPServiceTest {

    private final XPService xpService = new XPService();

    @Test
    void testCalculateRoundXP_PerfectRound() {
        // 5 correct, 0 mistakes -> 50 XP
        int xp = xpService.calculateRoundXP(5, 0);
        assertEquals(50, xp, "5 correct and 0 mistakes should yield 50 XP");
    }

    @Test
    void testCalculateRoundXP_WithMistakes() {
        // 5 correct, 2 mistakes -> 44 XP (50 - 6)
        int xp = xpService.calculateRoundXP(5, 2);
        assertEquals(44, xp, "5 correct and 2 mistakes should yield 44 XP");
    }

    @Test
    void testCalculateRoundXP_MinimumZero() {
        // 0 correct, 5 mistakes -> 0 XP (not negative)
        int xp = xpService.calculateRoundXP(0, 5);
        assertEquals(0, xp, "XP should never drop below 0");
    }
}