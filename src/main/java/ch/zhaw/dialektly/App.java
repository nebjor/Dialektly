package ch.zhaw.dialektly;

import ch.zhaw.dialektly.repository.UserRepository;
import ch.zhaw.dialektly.repository.VocabularyRepository;
import ch.zhaw.dialektly.service.I18nService;
import ch.zhaw.dialektly.service.OnboardingService;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * JavaFX entry point for the Dialektly application.
 * Bootstraps the {@link Storage}, repositories, and {@link Navigator},
 * then shows the login screen. On shutdown, persists the user list.
 */
public class App extends Application {

    private UserRepository userRepository;

    /**
     * Default constructor invoked by the JavaFX runtime.
     */
    public App() {
    }

    /**
     * Initialises the application infrastructure and opens the login screen.
     *
     * @param primaryStage the primary window provided by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
        Storage storage = new Storage(Path.of("data"));
        this.userRepository = new UserRepository(storage);
        VocabularyRepository vocabularyRepository = new VocabularyRepository(storage);
        I18nService i18nService = new I18nService();
        OnboardingService onboardingService = new OnboardingService(userRepository);
        Navigator navigator = new Navigator(primaryStage, userRepository, vocabularyRepository, i18nService, onboardingService);

        primaryStage.setTitle("Dialektly");
        navigator.showLogin();
        primaryStage.show();
    }

    /**
     * Called by the JavaFX runtime when the application is closing.
     * Persists the in-memory user list to disk before shutdown.
     */
    @Override
    public void stop() {
        userRepository.save();
    }

    /**
     * Application entry point — delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
