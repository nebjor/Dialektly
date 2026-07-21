package ch.zhaw.dialektly.presenter;

import ch.zhaw.dialektly.Navigator;
import ch.zhaw.dialektly.domain.Badge;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.service.I18nService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

/**
 * Presenter for the round summary screen. Displays the results of a just-completed
 * {@link LearningSession} (mastered count, total items, success ratio) and lets the
 * user return to the home screen.
 */
public class SummaryPresenter {

    private static final int BADGE_ROW_SPACING = 8;
    private static final int BADGE_ICON_HEIGHT = 40;
    private static final String BADGE_LABEL_STYLE = "-fx-font-size: 13px; -fx-text-fill: #333;";

    private final User user;
    private final LearningSession completedSession;
    private final List<Badge> newlyAwardedBadges;
    private final Navigator navigator;
    private final I18nService i18nService;

    /** Label showing the mastered / total item count. */
    @FXML
    public Label masteredLabel;
    /** Label showing the first-try-correct / total count. */
    @FXML
    public Label firstTryLabel;
    /** Label displaying the first-try success ratio as a percentage. */
    @FXML
    public Label ratioLabel;
    /** Button that returns the user to the home screen. */
    @FXML
    public Button continueButton;
    /** Section that contains the newly-awarded-badges header and container; hidden when no new badges. */
    @FXML
    public VBox badgesSection;
    /** Container that holds the rendered newly-awarded badge rows. */
    @FXML
    public VBox badgesContainer;


    /**
     * Creates a new presenter bound to a completed session.
     *
     * @param user the currently logged-in user
     * @param session the session whose results are shown; must be in completed state
     * @param newlyAwardedBadges badges awarded by the just-finished round (empty if none)
     * @param navigator used to switch back to the home scene on continue
     * @param i18nService service providing translated UI text
     */
    public SummaryPresenter(User user,
                            LearningSession session,
                            List<Badge> newlyAwardedBadges,
                            Navigator navigator,
                            I18nService i18nService) {
        this.user = Objects.requireNonNull(user, "user");
        this.completedSession = Objects.requireNonNull(session, "session");
        this.newlyAwardedBadges = Objects.requireNonNull(newlyAwardedBadges, "newlyAwardedBadges");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.i18nService = Objects.requireNonNull(i18nService, "i18nService");
    }

    /**
     * Populates the summary labels from the completed session. Must be called after
     * the FXML has been loaded so the {@code @FXML} fields are wired.
     */
    public void start() {
        int total = completedSession.getTotalItems();
        masteredLabel.setText(i18nService.getText("summary.mastered") + " "
                + completedSession.getMasteredCount() + " / " + total);
        firstTryLabel.setText(i18nService.getText("summary.firstTry") + " "
                + completedSession.getFirstTryCorrectCount() + " / " + total);
        ratioLabel.setText(String.format("%.0f%%", completedSession.firstTryRatio()));
        renderBadges();
    }

    /**
     * Reveals the badges section and appends an icon-and-label row for each
     * newly-awarded badge. No-op when the list is empty.
     */
    private void renderBadges() {
        if (newlyAwardedBadges.isEmpty()) {
            return;
        }
        badgesSection.setVisible(true);
        badgesSection.setManaged(true);
        for (Badge badge : newlyAwardedBadges) {
            HBox row = new HBox(BADGE_ROW_SPACING);
            row.setAlignment(Pos.CENTER);

            java.io.InputStream badgeStream = getClass().getResourceAsStream("/ch/zhaw/dialektly/images/badges/" + badge.getId() + ".png");
            if (badgeStream == null) {
                throw new IllegalStateException("Badge image not found: /ch/zhaw/dialektly/images/badges/" + badge.getId() + ".png");
            }
            ImageView icon = new ImageView(new Image(badgeStream));
            icon.setFitHeight(BADGE_ICON_HEIGHT);
            icon.setPreserveRatio(true);

            Label label = new Label(i18nService.getText(badge.getDisplayKey()));
            label.setStyle(BADGE_LABEL_STYLE);

            row.getChildren().addAll(icon, label);
            badgesContainer.getChildren().add(row);
        }
    }

    /**
     * Handler for the continue button — navigates back to the home screen.
     */
    @FXML
    public void onContinue() {
        navigator.backToHome(user);
    }

    /** Returns to the login screen. */
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
