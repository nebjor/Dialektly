package ch.zhaw.dialektly.service;

import ch.zhaw.dialektly.domain.AppLanguage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Centralised lookup service for translated UI text. Wraps the
 * {@code messages_de.properties} / {@code messages_en.properties} resource
 * bundles and exposes typed access keyed by translation IDs (e.g.
 * {@code "home.btnStart"}).
 *
 * <p>Listeners registered via {@link #addLanguageChangeListener(Runnable)}
 * are notified after the active language is swapped via
 * {@link #setLanguage(AppLanguage)}, so consumers can refresh their visible
 * labels without restarting the application.</p>
 */
public class I18nService {

    private static final String BUNDLE_NAME = "ch.zhaw.dialektly.messages";

    private AppLanguage language;
    private ResourceBundle bundle;
    private final List<Runnable> listeners = new ArrayList<>();

    /** Creates a service initialised to {@link AppLanguage#DE}. */
    public I18nService() {
        this(AppLanguage.DE);
    }

    /**
     * Creates a service initialised to the given language.
     *
     * @param initial the language to start with; must not be {@code null}
     */
    public I18nService(AppLanguage initial) {
        this.language = Objects.requireNonNull(initial, "initial");
        this.bundle = loadBundle(initial);
    }

    /**
     * Returns the translation for the given key in the active language.
     *
     * @param key the translation key (e.g. {@code "home.btnStart"})
     * @return the translated string
     * @throws java.util.MissingResourceException if the key is not in the bundle
     */
    public String getText(String key) {
        return bundle.getString(Objects.requireNonNull(key, "key"));
    }

    /**
     * Returns a {@link MessageFormat}-formatted translation. The bundle
     * value may contain {@code {0}}, {@code {1}}, etc. placeholders that
     * are filled with the given arguments.
     *
     * @param key  the translation key
     * @param args the values for the {@code {n}} placeholders in the template
     * @return the formatted translated string
     */
    public String getText(String key, Object... args) {
        return MessageFormat.format(getText(key), args);
    }

    /**
     * Switches the active language and notifies all registered listeners.
     * No-op when the requested language equals the current one.
     *
     * @param newLanguage the language to switch to; must not be {@code null}
     */
    public void setLanguage(AppLanguage newLanguage) {
        Objects.requireNonNull(newLanguage, "newLanguage");
        if (newLanguage == this.language) {
            return;
        }
        this.language = newLanguage;
        this.bundle = loadBundle(newLanguage);
        // Snapshot to allow listeners to add/remove themselves during iteration
        // without causing ConcurrentModificationException.
        for (Runnable listener : new ArrayList<>(listeners)) {
            listener.run();
        }
    }

    /**
     * Returns the currently active language.
     *
     * @return the active {@link AppLanguage}
     */
    public AppLanguage getLanguage() {
        return language;
    }

    /**
     * Returns the underlying {@link ResourceBundle}, suitable for passing
     * to {@code FXMLLoader.setResources(...)} so {@code %key} attributes in
     * FXML resolve.
     *
     * @return the active resource bundle
     */
    public ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Registers a callback to be invoked whenever the active language changes.
     *
     * @param listener the callback; must not be {@code null}
     */
    public void addLanguageChangeListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Removes a previously-registered language change callback.
     *
     * @param listener the callback to remove
     */
    public void removeLanguageChangeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Loads the resource bundle for the given language using UTF-8 encoding,
     * overriding the default ISO-8859-1 behaviour of {@link ResourceBundle}.
     *
     * @param lang the language to load
     * @return the loaded {@link ResourceBundle}
     */
    private static ResourceBundle loadBundle(AppLanguage lang) {
        Locale locale = (lang == AppLanguage.EN) ? Locale.ENGLISH : Locale.GERMAN;
        return ResourceBundle.getBundle(BUNDLE_NAME, locale, new UTF8Control());
    }

    /**
     * A {@link ResourceBundle.Control} implementation that reads
     * {@code .properties} files as UTF-8 instead of the default ISO-8859-1.
     */
    private static class UTF8Control extends ResourceBundle.Control {

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream is = loader.getResourceAsStream(resourceName)) {
                if (is == null) return null;
                return new PropertyResourceBundle(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
    }
}