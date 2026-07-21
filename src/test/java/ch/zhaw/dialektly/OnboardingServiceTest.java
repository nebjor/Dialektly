package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.AppLanguage;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.repository.UserRepository;
import ch.zhaw.dialektly.service.OnboardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    private OnboardingService service;

    @BeforeEach
    void setUp() {
        service = new OnboardingService(userRepository);
    }

    @Test
    void updateLanguage_setsAppLanguageOnUser() {
        service.updateLanguage(user, AppLanguage.EN);
        verify(user).setAppLanguage(AppLanguage.EN);
    }

    @Test
    void updateLanguage_persistsViaRepository() {
        service.updateLanguage(user, AppLanguage.EN);
        verify(userRepository).save();
    }

    @Test
    void updateLanguage_nullUser_throws() {
        assertThrows(NullPointerException.class,
                () -> service.updateLanguage(null, AppLanguage.EN));
    }

    @Test
    void updateLanguage_nullLanguage_throws() {
        assertThrows(NullPointerException.class,
                () -> service.updateLanguage(user, null));
    }

    @Test
    void constructor_nullRepository_throws() {
        assertThrows(NullPointerException.class,
                () -> new OnboardingService(null));
    }
}
