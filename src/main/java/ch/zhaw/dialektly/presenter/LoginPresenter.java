package ch.zhaw.dialektly.presenter;

import ch.zhaw.dialektly.Navigator;
import ch.zhaw.dialektly.domain.AppLanguage;
import ch.zhaw.dialektly.domain.DialectRegion;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.repository.UserRepository;
import ch.zhaw.dialektly.service.I18nService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Objects;

/**
 * Controller for {@code login.fxml}. Collects the username and chosen language,
 * looks up the user in {@link UserRepository} (creating one if absent), and
 * delegates the home navigation to {@link Navigator#showHome(User)}.
 *
 * <p>Constructor-injected. Wire the controller via
 * {@code FXMLLoader.setControllerFactory(c -> new LoginPresenter(userRepo, navigator))}
 * before calling {@code loader.load()}.</p>
 */
public class LoginPresenter {

    @FXML
    private TextField usernameField;
    @FXML
    private Label registerErrorMessageField;
    @FXML
    private ComboBox<AppLanguage> languageSelector;
    @FXML
    private ComboBox<DialectRegion> regionSelector;
    @FXML
    private ComboBox<String> existingUserSelector;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    private final UserRepository userRepository;
    private final Navigator navigator;
    private final I18nService i18nService;

    /**
     * Creates a login presenter wired to the user repository and navigator.
     *
     * @param userRepository repository providing existing users and persisting new ones
     * @param navigator      used to switch to the home screen after login
     * @param i18nService    service providing translated UI text
     */
    public LoginPresenter(UserRepository userRepository, Navigator navigator, I18nService i18nService) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.i18nService = Objects.requireNonNull(i18nService, "i18nService");
    }

    /**
     * Initialises the controller after all {@code @FXML} fields have been
     * injected by the FXML loader. Populates the language selector with the
     * values of {@link AppLanguage}, preselects {@link AppLanguage#DE}, and
     * binds the login button's disabled state to whether the username field
     * is empty.
     */
    @FXML
    public void initialize() {
        existingUserSelector.setItems(
                FXCollections.observableArrayList(userRepository.getAllUsernames()));
        loginButton.disableProperty().bind(
                existingUserSelector.valueProperty().isNull());

        languageSelector.setItems(FXCollections.observableArrayList(AppLanguage.values()));
        languageSelector.getSelectionModel().select(AppLanguage.DE);

        regionSelector.setItems(FXCollections.observableArrayList(DialectRegion.values()));
        regionSelector.getSelectionModel().select(DialectRegion.ZURICH);

        registerButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                        .or(regionSelector.valueProperty().isNull())
                        .or(languageSelector.valueProperty().isNull())
        );
    }

    /**
     * Logs in an existing user selected from the dropdown.
     */
    @FXML
    private void onLoginClicked() {
        String username = existingUserSelector.getValue();
        if (username == null) return;

        userRepository.findByUsername(username).ifPresent(navigator::showHome);
    }

    /**
     * Registers a new user with the entered username and selected language.
     * If the username already exists, the existing user is loaded instead.
     */
    @FXML
    private void onRegisterClicked() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) return;

        if (isUsernameTaken(username)) {
            registerErrorMessageField.setText(i18nService.getText("login.errorUsernameTaken"));
            return;
        }

        AppLanguage language = languageSelector.getValue() != null
                ? languageSelector.getValue()
                : AppLanguage.DE;
        DialectRegion region = regionSelector.getValue() != null
                ? regionSelector.getValue()
                : DialectRegion.ZURICH;

        User user = getOrCreateUser(username, language, region);
        navigator.showHome(user);
    }

    /**
     * Returns whether a user with the given name already exists (case-insensitive).
     */
    private boolean isUsernameTaken(String username) {
        return existingUserSelector.getItems().stream()
                .anyMatch(name -> name.equalsIgnoreCase(username));
    }

    /**
     * Looks up the user by name; if absent, creates and persists a new one.
     */
    private User getOrCreateUser(String username, AppLanguage language, DialectRegion region) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User(username, language, region);
            userRepository.add(newUser);
            userRepository.save();
            return newUser;
        });
    }
}
