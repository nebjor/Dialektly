package ch.zhaw.dialektly.presenter;

import ch.zhaw.dialektly.Navigator;
import ch.zhaw.dialektly.domain.Badge;
import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.TopicProgress;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.service.I18nService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Controller for {@code home.fxml}. Displays the welcome message, per-topic
 * progress bars, and any badges the user has earned. Provides actions to
 * start a new session, log out, or exit the application.
 *
 * <p>Constructor-injected. Wire the controller via
 * {@code FXMLLoader.setControllerFactory(c -> new HomePresenter(user, navigator))}
 * before calling {@code loader.load()}; then call {@link #start()} to populate the view.</p>
 */
public class HomePresenter {

    private static final int TOPIC_LABEL_MIN_WIDTH = 100;
    private static final int PERCENTAGE_LABEL_MIN_WIDTH = 90;
    private static final int PROGRESS_BAR_PREF_WIDTH = 200;
    private static final int PROGRESS_ROW_SPACING = 12;
    private static final int BADGE_ROW_SPACING = 8;
    private static final int BADGE_ICON_HEIGHT = 32;
    private static final double LOCKED_BADGE_OPACITY = 0.4;
    private static final double GRAYSCALE_SATURATION = -1.0;
    private static final String EARNED_LABEL_STYLE = "-fx-font-size: 13px; -fx-text-fill: #333;";
    private static final String LOCKED_LABEL_STYLE = "-fx-font-size: 13px; -fx-text-fill: #999;";

    private final User user;
    private final Navigator navigator;
    private final I18nService i18nService;

    /** Welcome greeting at the top of the home view. */
    @FXML
    public Label welcomeLabel;
    /** Button that navigates to the topic-picker screen. */
    @FXML
    public Button startSessionButton;
    /** Container that holds one row per topic with its progress bar. */
    @FXML
    public VBox progressContainer;
    /** Bar showing overall progress across all topics. */
    @FXML
    public ProgressBar overallProgressBar;
    /** Label showing the overall progress percentage value. */
    @FXML
    public Label overallPercentageLabel;
    /** Container that holds the rendered badge rows. */
    @FXML
    public VBox badgesContainer;

    /**
     * Creates a new home presenter for the given user.
     *
     * @param user         the currently logged-in user
     * @param navigator    used to switch between scenes
     * @param i18nService  service providing translated UI text
     */
    public HomePresenter(User user, Navigator navigator, I18nService i18nService) {
        this.user = Objects.requireNonNull(user, "user");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.i18nService = Objects.requireNonNull(i18nService, "i18nService");
    }

    /**
     * Populates the welcome label, awards any newly-earned badges, renders the
     * overall and per-topic progress bars, and displays the user's badge
     * collection. Must be called after the FXML has been loaded so the
     * {@code @FXML} fields are wired.
     */
    public void start() {
        welcomeLabel.setText(i18nService.getText("home.welcomeUser", user.getUsername()));

        user.getProgress().checkAndAwardBadges();

        populateOverallProgress();
        populateProgress();
        populateBadges();
    }

    private void populateOverallProgress() {
        double overall = user.getProgress().getOverallPercentage();
        overallProgressBar.setProgress(overall / 100.0);
        overallPercentageLabel.setText(String.format("%.2f", overall) + "%");
    }

    /**
     * Clears and rebuilds the progress container with one row per topic,
     * each showing the topic name, a progress bar, and the percentage value.
     */
    private void populateProgress() {
        progressContainer.getChildren().clear();

        Map<TopicCategory, TopicProgress> topicProgress = user.getProgress().getTopicProgress();

        for (Map.Entry<TopicCategory, TopicProgress> entry : topicProgress.entrySet()) {
            Label topicLabel = new Label(i18nService.getText(entry.getKey().getDisplayKey()));
            topicLabel.setStyle("-fx-font-weight: bold;");
            topicLabel.setMinWidth(TOPIC_LABEL_MIN_WIDTH);

            double percentage = entry.getValue().getCombinedPercentage();
            ProgressBar bar = new ProgressBar(percentage / 100.0);
            bar.getStyleClass().add("progress-bar");
            bar.setPrefWidth(PROGRESS_BAR_PREF_WIDTH);
            HBox.setHgrow(bar, Priority.ALWAYS);

            Label percentageLabel = new Label(String.format("%.2f", percentage) + "%");
            percentageLabel.setStyle("-fx-font-weight: bold;");
            percentageLabel.setMinWidth(PERCENTAGE_LABEL_MIN_WIDTH);

            HBox row = new HBox(PROGRESS_ROW_SPACING, topicLabel, bar, percentageLabel);
            row.setAlignment(Pos.CENTER_LEFT);
            progressContainer.getChildren().add(row);
        }
    }

    private void populateBadges() {
        badgesContainer.getChildren().clear();
        Set<Badge> earned = user.getProgress().getBadges();
        for (Badge badge : Badge.values()) {
            boolean isEarned = earned.contains(badge);

            HBox row = new HBox(BADGE_ROW_SPACING);
            row.setAlignment(Pos.CENTER_LEFT);

            java.io.InputStream badgeStream = getClass().getResourceAsStream("/ch/zhaw/dialektly/images/badges/" + badge.getId() + ".png");
            if (badgeStream == null) {
                throw new IllegalStateException("Badge image not found: /ch/zhaw/dialektly/images/badges/" + badge.getId() + ".png");
            }
            ImageView icon = new ImageView(new Image(badgeStream));
            icon.setFitHeight(BADGE_ICON_HEIGHT);
            icon.setPreserveRatio(true);

            Label label = new Label(i18nService.getText(badge.getDisplayKey()));

            if (isEarned) {
                label.setStyle(EARNED_LABEL_STYLE);
            } else {
                ColorAdjust grayscale = new ColorAdjust();
                grayscale.setSaturation(GRAYSCALE_SATURATION);
                icon.setEffect(grayscale);
                row.setOpacity(LOCKED_BADGE_OPACITY);
                label.setStyle(LOCKED_LABEL_STYLE);
            }

            row.getChildren().addAll(icon, label);
            badgesContainer.getChildren().add(row);
        }
    }

    /** Navigates to the topic-picker screen to start a new learning session. */
    @FXML
    public void onStartNewSession() {
        navigator.showTopicPicker(user);
    }

    /** Logs out and returns to the login screen. */
    @FXML
    public void onLogout() {
        navigator.showLogin();
    }

    /** Exits the JavaFX application. */
    @FXML
    public void onExit() {
        Platform.exit();
    }
}
