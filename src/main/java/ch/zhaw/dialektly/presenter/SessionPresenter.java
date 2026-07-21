package ch.zhaw.dialektly.presenter;

import ch.zhaw.dialektly.Navigator;
import ch.zhaw.dialektly.domain.Badge;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.domain.VocabularyItem;
import ch.zhaw.dialektly.service.SessionService;
import ch.zhaw.dialektly.service.I18nService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * Controller for {@code session.fxml}. Drives an active {@link LearningSession}:
 * displays the current exercise, evaluates answers, shows feedback, and
 * transitions to the summary screen when all items are mastered. Progress is
 * persisted (XP, learned items, badges) at the end of the session or on logout/exit.
 *
 * <p>Constructor-injected. Wire the controller via
 * {@code FXMLLoader.setControllerFactory(c -> new SessionPresenter(navigator, userRepo, user, session))}
 * before calling {@code loader.load()}; then call {@link #start()} to initialise the view.</p>
 */
public class SessionPresenter {

    private static final String STYLE_FEEDBACK_CORRECT = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;";
    private static final String STYLE_FEEDBACK_WRONG = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;";
    private static final String BTN_DEFAULT = "-fx-background-color: #888888; -fx-text-fill: white; -fx-background-radius: 8;";
    private static final String BTN_HOVER   = "-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-background-radius: 8;";
    private final Navigator navigator;
    private final User user;
    private final LearningSession session;
    private final I18nService i18nService;
    private final SessionService sessionService;

    private MediaPlayer mediaPlayer;
    private VocabularyItem currentItemToPlay;

    /**
     * Label displaying the current dialect word to translate.
     */
    @FXML
    public Label questionLabel;

    /**
     * Button that plays or replays the audio clip for audio-to-text exercises.
     */
    @FXML
    public Button audioPlayButton;
    /**
     * Label showing mastered / total exercise progress.
     */
    @FXML
    public Label progressLabel;
    /**
     * Inline feedback label (✔/✘) after the user submits an answer.
     */
    @FXML
    public Label resultLabel;
    /**
     * Input field for the user's typed answer.
     */
    @FXML
    public TextField answerField;
    /**
     * Button that submits the typed answer for evaluation.
     */
    @FXML
    public Button submitButton;
    /**
     * Button that advances to the next exercise after feedback is shown.
     */
    @FXML
    public Button nextButton;

    /**
     * Creates a session presenter for the given user and session.
     *
     * @param navigator      used to switch between scenes
     * @param sessionService used to persist progress at end of session
     * @param user           the currently logged-in user
     * @param session        the active learning session to drive
     * @param i18nService    service providing translated UI text
     */
    public SessionPresenter(Navigator navigator,
                            SessionService sessionService,
                            User user,
                            LearningSession session,
                            I18nService i18nService) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.sessionService = Objects.requireNonNull(sessionService, "sessionService");
        this.user = Objects.requireNonNull(user, "user");
        this.session = Objects.requireNonNull(session, "session");
        this.i18nService = Objects.requireNonNull(i18nService, "i18nService");
    }

    /**
     * Initialises button states, applies language-specific labels, sets the
     * header content, and registers a back-navigation guard on the logo.
     * Must be called after the FXML has been loaded so the {@code @FXML} fields are wired.
     */
    public void start() {
        submitButton.setDefaultButton(true);
        updateView();
        applyLanguageLabels();
        setupExitConfirmation();
    }

    /**
     * Sets button captions and the header mode/topic text based on the user's
     * selected language and the session's topic.
     */
    private void applyLanguageLabels() {
        submitButton.setText(i18nService.getText("session.btnCheck"));
        nextButton.setText(i18nService.getText("session.btnNext"));

        String modeText = i18nService.getText("session.mode");
        String topicText = i18nService.getText(session.getTopic().getDisplayKey());
        navigator.setHeaderContent(modeText, topicText);
    }

    /**
     * Registers a logo-click action that asks the user whether to abandon the
     * active session, and navigates back to the home screen on confirmation.
     */
    private void setupExitConfirmation() {
        navigator.setCustomLogoAction(() -> {
            Alert alert = buildCancelSessionAlert();
            ButtonType yesBtn = alert.getButtonTypes().getFirst();
            alert.showAndWait().ifPresent(type -> {
                if (type == yesBtn) {
                    navigator.showHome(user);
                }
            });
        });
    }

    /**
     * Builds the localised confirmation alert shown when the user clicks the
     * Dialektly logo during an active session. The first {@link ButtonType} in
     * the returned alert's button list is the "yes/confirm" button.
     */
    private Alert buildCancelSessionAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(i18nService.getText("session.cancel.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18nService.getText("session.cancel.content"));

        ButtonType yesBtn = new ButtonType(i18nService.getText("common.btnYes"), ButtonBar.ButtonData.YES);
        ButtonType noBtn = new ButtonType(i18nService.getText("common.btnNo"), ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        alert.getDialogPane().setStyle(
                "-fx-background-color: rgba(245, 245, 245, 0.85); -fx-background-radius: 12;");

        javafx.scene.control.ButtonBase yesButton =
                (javafx.scene.control.ButtonBase) alert.getDialogPane().lookupButton(yesBtn);
        javafx.scene.control.ButtonBase noButton =
                (javafx.scene.control.ButtonBase) alert.getDialogPane().lookupButton(noBtn);

        yesButton.setStyle(BTN_DEFAULT);
        noButton.setStyle(BTN_DEFAULT);

        yesButton.hoverProperty().addListener((obs, wasHovered, isHovered) ->
                yesButton.setStyle(isHovered ? BTN_HOVER : BTN_DEFAULT));
        noButton.hoverProperty().addListener((obs, wasHovered, isHovered) ->
                noButton.setStyle(isHovered ? BTN_HOVER : BTN_DEFAULT));

        return alert;
    }
    /**
     * Handles the play-audio button click by delegating to {@link #playCurrentAudio()}.
     */
    @FXML
    public void onPlayAudioClicked() {
        playCurrentAudio();
    }

    private void playCurrentAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (currentItemToPlay == null) {
            return;
        }
        ch.zhaw.dialektly.domain.ExerciseType type = session.getCurrentExercise().orElseThrow().getType();

        String path = currentItemToPlay.getAudioFilePath().orElse("");
        if (!path.isEmpty()) {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                Media media = new Media(resource.toString());
                mediaPlayer = new MediaPlayer(media);

                if (type == ch.zhaw.dialektly.domain.ExerciseType.AUDIO_TO_TEXT) {
                    audioPlayButton.setText(i18nService.getText("session.audio.playing"));
                    audioPlayButton.setDisable(true);
                }

                mediaPlayer.setOnEndOfMedia(() -> {
                    if (type == ch.zhaw.dialektly.domain.ExerciseType.AUDIO_TO_TEXT) {
                        audioPlayButton.setText(i18nService.getText("session.audio.replay"));
                        audioPlayButton.setDisable(false);
                    }
                });

                mediaPlayer.play();
                return;
            } else {
                System.err.println("Audio resource not found: " + path);
            }
        }

        if (type == ch.zhaw.dialektly.domain.ExerciseType.AUDIO_TO_TEXT) {
            audioPlayButton.setText(i18nService.getText("session.audio.missing"));
            audioPlayButton.setDisable(true);
        }
    }

    /** Evaluates the user's answer and shows inline feedback. */
    @FXML
    public void onSubmitClicked() {
        VocabularyItem current = session.getCurrentExercise().orElseThrow().getItem();
        boolean correct = session.submitAnswer(answerField.getText());
        showFeedback(correct, current);
    }

    /**
     * Advances to the next exercise, or finalises the session when all items are mastered.
     */
    @FXML
    public void onNextClicked() {
        if (session.isCompleted()) {
            List<Badge> newlyAwarded = persistProgress();
            navigator.showSummary(user, session, newlyAwarded);
            return;
        }
        resetUiForNextQuestion();
        updateView();
    }
    /**
     * Shows a confirmation dialog before navigating back to the topic picker.
     * Progress is not persisted if the user confirms; the session is abandoned.
     */
    @FXML
    public void onBackClicked() {
        Alert alert = buildCancelSessionAlert();
        ButtonType yesBtn = alert.getButtonTypes().getFirst();
        alert.showAndWait().ifPresent(type -> {
            if (type == yesBtn) {
                navigator.showTopicPicker(user);
            }
        });
    }

    /** Persists progress and returns to the login screen. */
    @FXML
    public void onLogout() {
        persistProgress();
        navigator.showLogin();
    }

    /**
     * Persists progress and exits the application.
     */
    @FXML
    public void onExit() {
        persistProgress();
        Platform.exit();
    }

    /**
     * Marks all mastered items as learned, applies the XP earned this round,
     * evaluates badge rules, and persists the updated user data.
     *
     * @return the badges newly awarded by this call (empty if none)
     */
    private List<Badge> persistProgress() {
        return sessionService.persistProgress(user, session);
    }

    /**
     * Updates the result label and toggles the submit/next button visibility
     * after an answer has been evaluated.
     *
     * @param correct {@code true} if the submitted answer was correct
     * @param item    the vocabulary item that was just evaluated (used to show the correct answer)
     */
    private void showFeedback(boolean correct, VocabularyItem item) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (correct) {
            resultLabel.setText(i18nService.getText("session.feedbackCorrect"));
            resultLabel.setStyle(STYLE_FEEDBACK_CORRECT);
        } else {
            resultLabel.setText(i18nService.getText("session.feedbackWrong", item.getStandardWord()));
            resultLabel.setStyle(STYLE_FEEDBACK_WRONG);
        }

        submitButton.setVisible(false);
        submitButton.setManaged(false);
        submitButton.setDefaultButton(false);
        nextButton.setVisible(true);
        nextButton.setManaged(true);
        answerField.setDisable(true);
        nextButton.requestFocus();
        nextButton.setDefaultButton(true);
    }

    /**
     * Resets all interactive controls to their initial state so the user
     * can enter an answer for the next exercise.
     */
    private void resetUiForNextQuestion() {
        resultLabel.setText("");
        submitButton.setVisible(true);
        submitButton.setManaged(true);
        submitButton.setDefaultButton(true);
        nextButton.setVisible(false);
        nextButton.setManaged(false);
        nextButton.setDefaultButton(false);
        answerField.clear();
        answerField.setDisable(false);
        answerField.requestFocus();
    }

    /**
     * Refreshes the progress label and question label for the current exercise.
     */
    private void updateView() {
        progressLabel.setText(session.getMasteredCount() + " / " + session.getTotalItems());

        ch.zhaw.dialektly.domain.ExerciseType type = session.getCurrentExercise().orElseThrow().getType();
        currentItemToPlay = session.getCurrentExercise().orElseThrow().getItem();

        if (type == ch.zhaw.dialektly.domain.ExerciseType.AUDIO_TO_TEXT) {
            questionLabel.setVisible(false);
            questionLabel.setManaged(false);
            audioPlayButton.setVisible(true);
            audioPlayButton.setManaged(true);
            audioPlayButton.setText(i18nService.getText("session.audio.play"));

            playCurrentAudio();
        } else {
            audioPlayButton.setVisible(false);
            audioPlayButton.setManaged(false);
            questionLabel.setVisible(true);
            questionLabel.setManaged(true);
            questionLabel.setText(currentItemToPlay.getDialectWord());
        }
    }
}
