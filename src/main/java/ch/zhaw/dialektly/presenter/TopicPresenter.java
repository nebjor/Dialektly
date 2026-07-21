package ch.zhaw.dialektly.presenter;

import ch.zhaw.dialektly.Navigator;
import ch.zhaw.dialektly.domain.DialectRegion;
import ch.zhaw.dialektly.domain.ExerciseType;
import ch.zhaw.dialektly.domain.LearningSession;
import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.TopicProgress;
import ch.zhaw.dialektly.domain.User;
import ch.zhaw.dialektly.domain.VocabularyItem;
import ch.zhaw.dialektly.repository.VocabularyRepository;
import ch.zhaw.dialektly.service.I18nService;

import java.util.List;
import java.util.Objects;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.TilePane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

/**
 * Controller for {@code topic.fxml}. Presents the available
 * {@link TopicCategory} values as a grid of selectable card-style tiles
 * (each annotated with the user's progress for that topic). When the user
 * confirms a topic, the presenter builds a {@link LearningSession} from the
 * region- and topic-filtered vocabulary and delegates scene navigation to
 * {@link Navigator#showSession(User, LearningSession)}.
 *
 * <p>Constructor-injected. Wire the controller via
 * {@code FXMLLoader.setControllerFactory(c -> new TopicPresenter(user, vocabRepo, navigator))}
 * before calling {@code loader.load()}; then call {@link #start()} to
 * populate the tiles.</p>
 */
public class TopicPresenter {

    private static final int TILE_PREF_WIDTH = 160;
    private static final int TILE_PREF_HEIGHT = 110;

    @FXML
    private TilePane topicTiles;
    @FXML
    private Button continueButton;
    @FXML
    private ToggleButton textToggle;
    @FXML
    private ToggleButton audioToggle;

    private final ToggleGroup topicGroup = new ToggleGroup();
    private final ToggleGroup exerciseTypeGroup = new ToggleGroup();

    private final User user;
    private final VocabularyRepository vocabularyRepository;
    private final Navigator navigator;
    private final I18nService i18nService;

    /**
     * Creates a topic-picker presenter for the given user.
     *
     * @param user                 the currently logged-in user
     * @param vocabularyRepository repository providing the vocabulary items
     * @param navigator            used to switch to the session screen
     * @param i18nService          service providing translated UI text
     */
    public TopicPresenter(User user,
                          VocabularyRepository vocabularyRepository,
                          Navigator navigator,
                          I18nService i18nService) {
        this.user = Objects.requireNonNull(user, "user");
        this.vocabularyRepository = Objects.requireNonNull(vocabularyRepository, "vocabularyRepository");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.i18nService = Objects.requireNonNull(i18nService, "i18nService");
    }

    /**
     * Initialises the controller after all {@code @FXML} fields have been
     * injected by the FXML loader. Sets up the binding that disables the
     * continue button while no tile is selected. Tile creation itself
     * happens in {@link #start()} since it depends on injected data.
     */
    @FXML
    public void initialize() {
        textToggle.setToggleGroup(exerciseTypeGroup);
        audioToggle.setToggleGroup(exerciseTypeGroup);
        textToggle.setUserData(ExerciseType.TEXT_INPUT);
        audioToggle.setUserData(ExerciseType.AUDIO_TO_TEXT);

        continueButton.disableProperty().bind(
                topicGroup.selectedToggleProperty().isNull()
                        .or(exerciseTypeGroup.selectedToggleProperty().isNull())
        );
    }

    /**
     * Populates the tile grid using the injected user and vocabulary data.
     * Safe to call again to refresh tiles when progress changes.
     */
    public void start() {
        topicTiles.getChildren().clear();
        for (TopicCategory topic : TopicCategory.values()) {
            ToggleButton tile = new ToggleButton(tileLabel(topic));
            tile.setUserData(topic);
            tile.setToggleGroup(topicGroup);
            tile.getStyleClass().add("topic-tile");
            if (isTopicCompleted(topic)) {
                tile.getStyleClass().add("topic-tile-completed");
            }
            tile.setPrefSize(TILE_PREF_WIDTH, TILE_PREF_HEIGHT);
            tile.setGraphic(topicIcon(topic));
            tile.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
            topicTiles.getChildren().add(tile);
        }
    }

    /**
     * Loads the icon image for the given topic category.
     *
     * @param topic the topic to load the icon for
     * @return an {@link ImageView} with the topic icon, or an empty one if not found
     */
    private ImageView topicIcon(TopicCategory topic) {
        String iconName = switch (topic) {
            case ALLTAG     -> "everyday-life.png";
            case EINKAUFEN  -> "shopping.png";
            case ARBEIT     -> "work.png";
            case GESUNDHEIT -> "health.png";
            case WOHNEN     -> "home.png";
            default         -> null;
        };
        ImageView iv = new ImageView();
        if (iconName != null) {
            var stream = getClass().getResourceAsStream(
                    "/ch/zhaw/dialektly/images/topics/" + iconName);
            if (stream != null) {
                iv.setImage(new Image(stream));
            }
        }
        iv.setFitHeight(36);
        iv.setFitWidth(36);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Handles a click on the continue button. Reads the currently selected
     * topic, builds a {@link LearningSession} from the user's region and
     * the chosen topic, and forwards to
     * {@link Navigator#showSession(User, LearningSession)}. Acts as a no-op
     * if no tile is selected or no vocabulary is available for the
     * region/topic combination.
     */
    @FXML
    private void onContinueClicked() {
        Toggle selected = topicGroup.getSelectedToggle();
        if (selected == null) {
            return;
        }
        TopicCategory topic = (TopicCategory) selected.getUserData();
        List<VocabularyItem> items =
                vocabularyRepository.getByRegionAndTopic(user.getSelectedRegion(), topic);

        ExerciseType type = (ExerciseType) exerciseTypeGroup.getSelectedToggle().getUserData();
        if (type == null) {
            return;
        }

        if (type == ExerciseType.AUDIO_TO_TEXT) {
            items = items.stream()
                    .filter(item -> item.getAudioFilePath()
                            .map(path -> getClass().getResource(path) != null)
                            .orElse(false))
                    .toList();
        }

        if (items.isEmpty()) {
            return;
        }

        LearningSession session = new LearningSession(topic, items, type);
        navigator.showSession(user, session);
    }
    /**
     * Navigates back to the home screen.
     */
    @FXML
    public void onBack() {
        navigator.showHome(user);
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

    /**
     * Builds the multi-line label shown on a topic tile. When no vocabulary
     * data is available for the topic, only the topic name is returned;
     * otherwise the label includes the learned / total count and a
     * checkmark when the topic is fully completed.
     *
     * @param topic the topic to render
     * @return the label text for the tile, possibly containing line breaks
     */
    private String tileLabel(TopicCategory topic) {
        String displayName = i18nService.getText(topic.getDisplayKey());
        int total = totalWordsFor(topic);
        if (total == 0) {
            return displayName;
        }
        TopicProgress tp = user.getProgress().getTopicProgress().get(topic);
        double pct = (tp != null) ? tp.getCombinedPercentage() : 0.0;
        String mark = (tp != null && tp.isBothCompleted()) ? "  ✓" : "";
        return displayName + mark + "\n" + (int) Math.round(pct) + " %";
    }

    /**
     * Counts the vocabulary items belonging to the given topic that match
     * the user's selected dialect region.
     *
     * @param topic the topic to count items for
     * @return the number of vocabulary items in scope for that topic
     */
    private int totalWordsFor(TopicCategory topic) {
        DialectRegion region = user.getSelectedRegion();
        return vocabularyRepository.getByRegionAndTopic(region, topic).size();
    }

    /**
     * Reads the number of items the current user has already learned in the
     * given topic. Returns zero if no {@link TopicProgress} entry exists for
     * that topic yet.
     *
     * @param topic the topic to read progress for
     * @return the number of mastered items for that topic
     */
    private int learnedCountFor(TopicCategory topic) {
        TopicProgress progress = user.getProgress().getTopicProgress().get(topic);
        return (progress != null) ? progress.getLearnedCount() : 0;
    }

    /**
     * Indicates whether every vocabulary item in the given topic has been
     * learned. Topics with no vocabulary in scope are never reported as
     * completed.
     *
     * @param topic the topic to check
     * @return {@code true} if learned count meets or exceeds total count
     * and the total is positive, {@code false} otherwise
     */
    private boolean isTopicCompleted(TopicCategory topic) {
        int total = totalWordsFor(topic);
        return total > 0 && learnedCountFor(topic) >= total;
    }

}
