package ch.zhaw.dialektly.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a user of the Dialektly application.
 * Stores the user's identity, language preferences, selected dialect region,
 * and their overall learning progress.
 */
public class User {

    private final UUID userId;
    private final String username;
    private AppLanguage appLanguage;
    private DialectRegion selectedRegion;
    private final UserProgress progress;

    /**
     * Creates a new user with the given username, language, and dialect region.
     *
     * @param username    the username of the user
     * @param appLanguage the preferred application language
     * @param region      the dialect region selected for learning
     */
    public User(String username, AppLanguage appLanguage, DialectRegion region) {
        this.userId = UUID.randomUUID();
        this.username = Objects.requireNonNull(username, "username");
        this.appLanguage = Objects.requireNonNull(appLanguage, "appLanguage");
        this.selectedRegion = Objects.requireNonNull(region, "region");
        this.progress = new UserProgress();
    }


    /**
     * Returns the unique identifier for this user.
     *
     * @return the user UUID
     */
    public UUID getUserId() {
        return userId; }

    /**
     * Returns the user's display name.
     *
     * @return the username
     */
    public String getUsername() {
        return username; }

    /**
     * Returns the user's preferred application language.
     *
     * @return the selected {@link AppLanguage}
     */
    public AppLanguage getAppLanguage() {
        return appLanguage; }

    /**
     * Returns the dialect region the user is currently learning.
     *
     * @return the selected {@link DialectRegion}
     */
    public DialectRegion getSelectedRegion() {
        return selectedRegion; }

    /**
     * Returns the user's learning progress record.
     *
     * @return the {@link UserProgress} instance owned by this user
     */
    public UserProgress getProgress() {
        return progress; }

    /**
     * Updates the user's preferred application language.
     *
     * @param appLanguage the new language selection
     */
    public void setAppLanguage(AppLanguage appLanguage) {
        this.appLanguage = appLanguage; }

    /**
     * Updates the dialect region the user is learning.
     *
     * @param selectedRegion the new region selection
     */
    public void setSelectedRegion(DialectRegion selectedRegion) {
        this.selectedRegion = selectedRegion; }
}
