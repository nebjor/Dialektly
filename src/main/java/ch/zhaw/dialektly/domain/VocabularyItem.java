package ch.zhaw.dialektly.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a single vocabulary entry in the Dialektly data set.
 * Each item pairs a Swiss German dialect word with its standard German
 * equivalent and belongs to a specific {@link TopicCategory} and
 * {@link DialectRegion}. An optional audio file path supports audio-based exercises.
 */
public class VocabularyItem {

    private final UUID itemId;
    private final String dialectWord;
    private final String standardWord;
    private final TopicCategory topic;
    private final DialectRegion region;
    private String audioFilePath;

    /**
     * Creates a new vocabulary item. A random {@link UUID} is assigned automatically.
     *
     * @param dialectWord  the Swiss German dialect form of the word
     * @param standardWord the standard German (Hochdeutsch) translation
     * @param topic        the topic category this item belongs to
     * @param region       the dialect region this item is specific to
     */
    public VocabularyItem(String dialectWord, String standardWord, TopicCategory topic, DialectRegion region) {
        this.itemId = UUID.randomUUID();
        this.dialectWord = Objects.requireNonNull(dialectWord, "dialectWord");
        this.standardWord = Objects.requireNonNull(standardWord, "standardWord");
        this.topic = Objects.requireNonNull(topic, "topic");
        this.region = Objects.requireNonNull(region, "region");
    }

    /**
     * Returns the appropriate word form for the given application language.
     *
     * @param lang the current UI language; currently always returns {@link #getStandardWord()}
     * @return the standard German word (dialect support pending)
     */
    public String getTranslation(AppLanguage lang) {
        // TODO: return dialectWord or standardWord depending on lang
        return standardWord;
    }

    /**
     * Returns the unique identifier for this vocabulary item.
     *
     * @return the item UUID
     */
    public UUID getItemId() { return itemId; }

    /**
     * Returns the Swiss German dialect word.
     *
     * @return the dialect form
     */
    public String getDialectWord() { return dialectWord; }

    /**
     * Returns the standard German (Hochdeutsch) equivalent.
     *
     * @return the standard German translation
     */
    public String getStandardWord() { return standardWord; }

    /**
     * Returns the topic category this item is grouped under.
     *
     * @return the topic category
     */
    public TopicCategory getTopic() { return topic; }

    /**
     * Returns the dialect region this item belongs to.
     *
     * @return the dialect region
     */
    public DialectRegion getRegion() { return region; }

    /**
     * Returns the path to the audio file for this item, if one has been set.
     *
     * @return the audio file path, or an empty {@link Optional} if none is set
     */
    public Optional<String> getAudioFilePath() { return Optional.ofNullable(audioFilePath); }

    /**
     * Sets the path to the audio file for this item.
     *
     * @param audioFilePath the path to the audio resource, or {@code null} to clear
     */
    public void setAudioFilePath(String audioFilePath) { this.audioFilePath = audioFilePath; }
}
