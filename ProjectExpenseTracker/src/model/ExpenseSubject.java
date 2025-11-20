package model;


/**
 * Subject interface for the Observer pattern used by the Expense model.
 *
 * <p>Implementors maintain a list of {@link ExpenseObserver} instances and notify them
 * when the model's state mutates. Implementations should document thread-safety semantics;
 * {@code ExpenseList} provides synchronized implementations of these methods.</p>
 */
public interface ExpenseSubject {

    /**
     * Register an observer to receive notifications when the subject changes.
     *
     * @param observer the observer to register (non-null)
     * @requires observer != null
     * @effects the observer will be added to the subject's notification list if not already present
     * @throws IllegalArgumentException if {@code observer} is null
     */
    void addObserver(ExpenseObserver observer);

    /**
     * Unregister a previously-registered observer so it no longer receives notifications.
     *
     * @param observer the observer to remove (may be null)
     * @requires true
     * @effects removes {@code observer} from the notification list if present
     */
    void removeObserver(ExpenseObserver observer);

    /**
     * Notify all currently-registered observers that the subject's state has changed.
     *
     * <p>Implementations should take care to avoid holding locks while calling out to
     * observers (to prevent deadlocks). The canonical pattern is to iterate over a snapshot
     * copy of the observer list.</p>
     *
     * @requires true
     * @effects invokes {@code modelChanged()} on each registered {@link ExpenseObserver}
     */
    void notifyObservers();
}
