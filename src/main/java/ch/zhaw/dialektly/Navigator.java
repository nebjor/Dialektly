package ch.zhaw.dialektly;

import ch.zhaw.dialektly.domain.AppLanguage;
import ch.zhaw.dialektly.domain.Badge;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.domain.VocabularyItem;
import ch.zhaw.dialektly.presenter.HomePresenter;
import ch.zhaw.dialektly.presenter.LoginPresenter;
import ch.zhaw.dialektly.presenter.SessionPresenter;
import ch.zhaw.dialektly.presenter.SummaryPresenter;
import ch.zhaw.dialektly.presenter.TopicPresenter;
import ch.zhaw.dialektly.repository.UserRepository;
import ch.zhaw.dialektly.repository.VocabularyRepository;
import ch.zhaw.dialektly.service.I18nService;
import ch.zhaw.dialektly.service.OnboardingService;
import ch.zhaw.dialektly.service.SessionService;

import ch.zhaw.dialektly.service.XPService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;


/**
 * Central scene-switching hub for the Dialektly application.
 * Owns the main shell (header + center pane) loaded from {@code main.fxml}
 * and replaces the center pane's content to navigate between screens.
 * All view-loading methods throw {@link RuntimeException} on FXML failures.
 */
public class Navigator {

    private final Stage stage;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final OnboardingService onboardingService;
    private final I18nService i18nService;
    private final SessionService sessionService;

    private final StackPane centerPane;
    private final Label headerModeLabel;
    private final Label headerTopicLabel;

    private User currentUser;
    private Runnable customLogoAction = null;
    private Runnable currentReloadHandler;

    /**
     * Loads the main shell FXML, wires the header controls, and sets up
     * the logo-click handler that returns the user to the home screen.
     *
     * @param stage                the primary window
     * @param userRepository       repository for loading and persisting users
     * @param vocabularyRepository repository providing the vocabulary data set
     * @param i18nService          translation service used to resolve {@code %key} attributes in FXML
     * @param onboardingService    service that persists user-account changes
     *                             such as the selected language
     * @throws RuntimeException if {@code main.fxml} cannot be loaded
     */
    public Navigator(Stage stage,
                     UserRepository userRepository,
                     VocabularyRepository vocabularyRepository,
                     I18nService i18nService,
                     OnboardingService onboardingService) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.vocabularyRepository = Objects.requireNonNull(vocabularyRepository, "vocabularyRepository");
        this.i18nService = Objects.requireNonNull(i18nService, "i18nService");
        this.onboardingService = Objects.requireNonNull(onboardingService, "onboardingService");

        XPService xpService = new XPService();
        this.sessionService = new SessionService(userRepository, xpService);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main.fxml"));
            loader.setResources(i18nService.getBundle());
            Parent shellRoot = loader.load();

            this.centerPane = (StackPane) loader.getNamespace().get("centerPane");
            this.headerModeLabel = (Label) loader.getNamespace().get("headerModeLabel");
            this.headerTopicLabel = (Label) loader.getNamespace().get("headerTopicLabel");
            wireLogoBox((HBox) loader.getNamespace().get("logoBox"));
            wireLanguageToggle((Button) loader.getNamespace().get("languageToggle"));

            stage.setScene(new Scene(shellRoot));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load main.fxml", e);
        }
    }

    /**
     * Hooks the logo box to either run a session-specific custom action (set
     * via {@link #setCustomLogoAction(Runnable)}) or, by default, navigate
     * back to the home screen for the currently logged-in user.
     */
    private void wireLogoBox(HBox logoBox) {
        logoBox.setOnMouseClicked(event -> {
            if (customLogoAction != null) {
                customLogoAction.run();
            } else if (currentUser != null) {
                showHome(currentUser);
            }
        });
    }

    /**
     * Hooks the language-toggle button: shows the current language code,
     * flips it on click (also persisting the change via
     * {@link OnboardingService} when a user is logged in), and stays in sync
     * with later language changes via an {@link I18nService} listener.
     */
    private void wireLanguageToggle(Button languageToggle) {
        Runnable syncText = () -> languageToggle.setText(i18nService.getLanguage().name());
        syncText.run();
        languageToggle.setOnAction(event -> {
            AppLanguage current = i18nService.getLanguage();
            AppLanguage target = current == AppLanguage.DE ? AppLanguage.EN : AppLanguage.DE;
            if (currentUser != null) {
                onboardingService.updateLanguage(currentUser, target);
            }
            i18nService.setLanguage(target);
        });
        i18nService.addLanguageChangeListener(syncText);
    }

    /**
     * Updates {@link I18nService}'s active language to match the given user's
     * preference. No-op if the user or their language preference is {@code null}.
     */
    private void applyUserLanguage(User user) {
        if (user != null && user.getAppLanguage() != null) {
            i18nService.setLanguage(user.getAppLanguage());
        }
    }

    /**
     * Replaces the registered I18n change-listener with a new "reload current
     * view" callback. Called by every {@code show*} method so a language flip
     * triggered elsewhere (e.g. a switcher button) re-runs the active view
     * with the new bundle. Pass {@code null} to clear.
     */
    private void setReloadHandler(Runnable handler) {
        if (currentReloadHandler != null) {
            i18nService.removeLanguageChangeListener(currentReloadHandler);
        }
        currentReloadHandler = handler;
        if (handler != null) {
            i18nService.addLanguageChangeListener(handler);
        }
    }

    /**
     * Updates the session-context labels in the header.
     * Passing {@code null} for either argument hides both labels.
     *
     * @param mode  the learning mode label text, or {@code null} to hide
     * @param topic the topic label text, or {@code null} to hide
     */
    public void setHeaderContent(String mode, String topic) {
        if (mode == null || topic == null) {
            headerModeLabel.setVisible(false);
            headerModeLabel.setManaged(false);
            headerTopicLabel.setVisible(false);
            headerTopicLabel.setManaged(false);
        } else {
            headerModeLabel.setText(mode);
            headerModeLabel.setVisible(true);
            headerModeLabel.setManaged(true);
            headerTopicLabel.setText(topic);
            headerTopicLabel.setVisible(true);
            headerTopicLabel.setManaged(true);
        }
    }

    /**
     * Overrides the default logo-click behavior with a custom action.
     * Pass {@code null} to restore the default (navigate to home).
     *
     * @param action the action to run on logo click, or {@code null} to reset
     */
    public void setCustomLogoAction(Runnable action) {
        this.customLogoAction = action;
    }

    private void resetViewContext(User user) {
        this.currentUser = user;
        setHeaderContent(null, null); // Hide session labels
        setCustomLogoAction(null);    // Reset logo click behavior
    }

    private <T> void loadAuthenticatedView(String fxmlFile, User user, T presenter, java.util.function.Consumer<T> postInit, Runnable reloader) {
        Objects.requireNonNull(user, "Authenticated views require a non-null user");
        loadViewInternal(fxmlFile, user, presenter, postInit, reloader, false);
    }

    private <T> void loadGuestView(String fxmlFile, T presenter, Runnable reloader, boolean resizeForLogin) {
        loadViewInternal(fxmlFile, null, presenter, null, reloader, resizeForLogin);
    }

    private <T> void loadViewInternal(String fxmlFile, User user, T presenter, java.util.function.Consumer<T> postInit, Runnable reloader, boolean resizeForLogin) {
        try {
            setReloadHandler(null);
            resetViewContext(user);
            applyUserLanguage(user);

            String path = fxmlFile.startsWith("/") ? fxmlFile : "fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setResources(i18nService.getBundle());
            if (presenter != null) {
                loader.setControllerFactory(c -> presenter);
            }
            Parent root = loader.load();

            if (postInit != null) {
                postInit.accept(presenter);
            }

            centerPane.getChildren().setAll(root);

            if (resizeForLogin) {
                stage.setMinWidth(750);
                stage.setMinHeight(500);
                stage.setWidth(1000);
                stage.setHeight(750);
            }
            stage.show();
            setReloadHandler(reloader);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + fxmlFile, e);
        }
    }

    /**
     * Loads and displays the login screen.
     *
     * @throws RuntimeException if {@code login.fxml} cannot be loaded
     */
    public void showLogin() {
        LoginPresenter presenter = new LoginPresenter(userRepository, this, i18nService);
        loadGuestView("login.fxml", presenter, this::showLogin, true);
    }

    /**
     * Loads and displays the home screen for the given user.
     *
     * @param user the currently logged-in user
     * @throws RuntimeException if {@code home.fxml} cannot be loaded
     */
    public void showHome(User user) {
        HomePresenter presenter = new HomePresenter(user, this, i18nService);
        loadAuthenticatedView("home.fxml", user, presenter, HomePresenter::start, () -> showHome(user));
    }

    /**
     * Loads and displays the topic-picker screen for the given user.
     *
     * @param user the currently logged-in user
     * @throws RuntimeException if {@code topic.fxml} cannot be loaded
     */
    public void showTopicPicker(User user) {
        TopicPresenter presenter = new TopicPresenter(user, vocabularyRepository, this, i18nService);
        loadAuthenticatedView("topic.fxml", user, presenter, TopicPresenter::start, () -> showTopicPicker(user));
    }

    /**
     * Loads and displays the active learning session screen.
     *
     * @param user    the currently logged-in user
     * @param session the session to run
     * @throws RuntimeException if {@code session.fxml} cannot be loaded
     */
    public void showSession(User user, LearningSession session) {
        SessionPresenter presenter = new SessionPresenter(this, sessionService, user, session, i18nService);
        loadAuthenticatedView("session.fxml", user, presenter, SessionPresenter::start, () -> showSession(user, session));
    }

    /**
     * Loads {@code summary.fxml}, wires a {@link SummaryPresenter} via
     * controller factory, and shows the round summary scene with the badges
     * earned by the just-finished session.
     *
     * @param user         the currently logged-in user
     * @param session      the completed session whose results are shown
     * @param newlyAwarded badges awarded by this round (empty if none)
     * @throws RuntimeException if the FXML resource cannot be loaded
     */
    public void showSummary(User user, LearningSession session, List<Badge> newlyAwarded) {
        SummaryPresenter presenter = new SummaryPresenter(user, session, newlyAwarded, this, i18nService);
        loadAuthenticatedView("summary.fxml", user, presenter, SummaryPresenter::start, () -> showSummary(user, session, newlyAwarded));
    }

    /**
     * Convenience alias for {@link #showHome(User)}.
     *
     * @param user the currently logged-in user
     */
    public void backToHome(User user) {
        showHome(user);
    }
}
