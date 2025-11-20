package model;


/**
 * Singleton class to manage user application settings (for example, currency).
 *
 * <p><b>Design Pattern:</b> Singleton.</p>
 *
 * <p><b>Purpose:</b> Ensures a single global instance of application settings that can be
 * accessed from different parts of the program. This class holds lightweight mutable
 * preferences (e.g., currency code) and is intentionally small; heavy-weight persisted
 * configuration should be managed separately.</p>
 *
 * <p><b>Thread-safety:</b> {@code getInstance()} is synchronized, and {@code setCurrency}
 * is thread-safe for simple updates. If you later add compound operations, consider using
 * more explicit concurrency control.</p>
 */
public final class SettingsManager {

    // the single static instance, lazily initialized
    private static SettingsManager instance;

    // example setting (mutable)
    private String currency = "SAR";

    /**
     * Private constructor prevents creating instances from outside this class.
     *
     * @requires true
     * @effects initializes the SettingsManager with default values (currency="SAR")
     */
    private SettingsManager() { }

    /**
     * Returns the single global instance of SettingsManager.
     *
     * <p>This method uses simple synchronization to ensure safe lazy initialization.
     * It returns the same instance for the lifetime of the JVM.</p>
     *
     * @requires true
     * @effects if no instance exists, creates one; otherwise returns the existing instance
     * @return the singleton {@code SettingsManager} instance (non-null)
     */
    public static synchronized SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    /**
     * Returns the currently selected currency code.
     *
     * @requires true
     * @effects none
     * @return currency code (non-null, uppercase string, e.g., "SAR" or "USD")
     */
    public synchronized String getCurrency() {
        return currency;
    }

    /**
     * Updates the currency setting.
     *
     * <p>The provided currency string will be trimmed and converted to upper-case.
     * If the argument is null or empty, the setting is not changed.</p>
     *
     * @param currency new currency code (non-null and non-empty after trimming)
     * @requires currency != null && !currency.trim().isEmpty()
     * @effects updates internal field {@code currency} to uppercase trimmed version
     * @throws IllegalArgumentException if the argument is null or empty after trimming
     */
    public synchronized void setCurrency(String currency) {
        if (currency == null) throw new IllegalArgumentException("currency cannot be null");
        String trimmed = currency.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("currency cannot be empty");
        this.currency = trimmed.toUpperCase();
    }
}
