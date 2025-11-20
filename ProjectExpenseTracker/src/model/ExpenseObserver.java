package model;

/**
 * Observer interface for receiving notifications from the model when its state changes.
 *
 * <p><b>Design Pattern:</b> Observer.</p>
 *
 * <p><b>Purpose:</b> Allows a view or controller to update automatically whenever the model
 * data changes, maintaining synchronization between the application's components.
 * Implementations should keep their model access brief and non-blocking; heavy work should be
 * dispatched to background threads if needed.</p>
 */
public interface ExpenseObserver {

    /**
     * Called when the observed model's data has changed.
     *
     * <p>Implementations should respond by reading the model's public API (for example,
     * calling {@code getExpenses()} or {@code getTotal()} on the model) and updating the UI
     * or internal state accordingly.</p>
     *
     * @requires true
     * @effects Notifies the observer that the model has been modified; the observer may
     *          refresh its display or update internal caches.
     */
    void modelChanged();
}
