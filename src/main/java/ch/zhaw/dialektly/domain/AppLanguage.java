package ch.zhaw.dialektly.domain;

/**
 * The language used to display the application UI.
 * Stored per user and used by presenters and {@link ch.zhaw.dialektly.Navigator}
 * to select the correct {@code ResourceBundle}.
 */
public enum AppLanguage {
    /** German (Deutsch) — the default application language. */
    DE,
    /** English — the alternative application language. */
    EN
}