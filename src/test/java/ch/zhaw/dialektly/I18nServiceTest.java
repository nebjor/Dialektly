package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.AppLanguage;
import ch.zhaw.dialektly.service.I18nService;
import org.junit.jupiter.api.Test;

import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class I18nServiceTest {

    @Test
    void getText_de_returnsGermanString() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        assertEquals("Weiter", i18n.getText("session.btnNext"));
    }

    @Test
    void getText_en_returnsEnglishString() {
        I18nService i18n = new I18nService(AppLanguage.EN);
        assertEquals("Next", i18n.getText("session.btnNext"));
    }

    @Test
    void defaultConstructor_initialisesToGerman() {
        I18nService i18n = new I18nService();
        assertEquals(AppLanguage.DE, i18n.getLanguage());
        assertEquals("Weiter", i18n.getText("session.btnNext"));
    }

    @Test
    void setLanguage_switchesActiveBundle() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        i18n.setLanguage(AppLanguage.EN);
        assertEquals("Next", i18n.getText("session.btnNext"));
        assertEquals(AppLanguage.EN, i18n.getLanguage());
    }

    @Test
    void setLanguage_notifiesRegisteredListener() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        AtomicInteger callCount = new AtomicInteger();
        i18n.addLanguageChangeListener(callCount::incrementAndGet);

        i18n.setLanguage(AppLanguage.EN);

        assertEquals(1, callCount.get());
    }

    @Test
    void setLanguage_sameLanguage_doesNotNotifyListeners() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        AtomicInteger callCount = new AtomicInteger();
        i18n.addLanguageChangeListener(callCount::incrementAndGet);

        i18n.setLanguage(AppLanguage.DE);

        assertEquals(0, callCount.get());
    }

    @Test
    void removeLanguageChangeListener_stopsNotifications() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        AtomicInteger callCount = new AtomicInteger();
        Runnable listener = callCount::incrementAndGet;
        i18n.addLanguageChangeListener(listener);
        i18n.removeLanguageChangeListener(listener);

        i18n.setLanguage(AppLanguage.EN);

        assertEquals(0, callCount.get());
    }

    @Test
    void getText_withArgs_substitutesPlaceholders() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        assertEquals("Willkommen, Raphi!", i18n.getText("home.welcomeUser", "Raphi"));
    }

    @Test
    void getText_missingKey_throws() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        assertThrows(MissingResourceException.class,
                () -> i18n.getText("nope.does.not.exist"));
    }

    @Test
    void getBundle_returnsActiveBundle() {
        I18nService i18n = new I18nService(AppLanguage.EN);
        assertEquals("Next", i18n.getBundle().getString("session.btnNext"));
    }

    @Test
    void constructor_nullLanguage_throws() {
        assertThrows(NullPointerException.class, () -> new I18nService(null));
    }

    @Test
    void setLanguage_listenerThatReregistersItself_doesNotThrow() {
        I18nService i18n = new I18nService(AppLanguage.DE);
        AtomicInteger callCount = new AtomicInteger();
        Runnable[] holder = new Runnable[1];
        holder[0] = () -> {
            callCount.incrementAndGet();
            i18n.removeLanguageChangeListener(holder[0]);
            i18n.addLanguageChangeListener(holder[0]);
        };
        i18n.addLanguageChangeListener(holder[0]);

        assertDoesNotThrow(() -> i18n.setLanguage(AppLanguage.EN));
        assertEquals(1, callCount.get());
    }
}
