package ch.zhaw.dialektly.repository;

import ch.zhaw.dialektly.Storage;
import ch.zhaw.dialektly.domain.DialectRegion;
import ch.zhaw.dialektly.domain.TopicCategory;
import ch.zhaw.dialektly.domain.VocabularyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Holds the vocabulary list in memory. Loaded once from {@link Storage} on
 * application start — read-only, no user-driven mutation.
 */
public class VocabularyRepository {

    private final List<VocabularyItem> vocabulary;

    /**
     * Loads the vocabulary from the given storage and keeps it in memory.
     *
     * @param storage the persistence backend; must not be {@code null}
     */
    public VocabularyRepository(Storage storage) {
        Objects.requireNonNull(storage, "storage");
        this.vocabulary = new ArrayList<>(storage.loadVocabulary());
    }

    /**
     * Returns an unmodifiable copy of the full vocabulary list.
     *
     * @return all vocabulary items; never {@code null}
     */
    public List<VocabularyItem> getAll() {
        return List.copyOf(vocabulary);
    }

    /**
     * Returns the vocabulary items that match both the given dialect region
     * and topic category.
     *
     * @param region the dialect region to filter by
     * @param topic  the topic category to filter by
     * @return a new list of matching items; empty if none match
     */
    public List<VocabularyItem> getByRegionAndTopic(DialectRegion region, TopicCategory topic) {
        List<VocabularyItem> filtered = new ArrayList<>();
        for (VocabularyItem item : vocabulary) {
            if (item.getRegion() == region && item.getTopic() == topic) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
