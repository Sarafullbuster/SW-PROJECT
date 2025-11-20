package model;

/**
 * Immutable Expense ID used as a unique identifier for expenses.
 *
 * <p><b>Abstraction Function (AF):</b>
 * AF(this) = the abstract identifier equal to the integer value {@code id}.
 *
 * <p><b>Representation Invariant (RI):</b>
 * - id >= 0
 *
 * <p><b>Safety from Rep Exposure:</b>
 * - Field is private and final.
 */
public final class ExpenseID {
    private final int id;

    /**
     * ADT classification: Creator.
     *
     * Create a new ExpenseID.
     *
     * @param id non-negative identifier
     * @requires id >= 0
     * @effects constructs a new immutable ExpenseID representing the given value
     * @throws IllegalArgumentException if id < 0
     */
    public ExpenseID(int id) {
        if (id < 0) throw new IllegalArgumentException("ID must be non-negative");
        this.id = id;
        checkRep();
    }

    /**
     * ADT classification: Observer.
     *
     * Return the integer value of this ID.
     *
     * @return numeric id (non-negative)
     * @requires true
     * @effects none
     */
    public int getValue() {
        return id;
    }

    /**
     * Checks the representation invariant of this ExpenseID.
     *
     * <p>The representation invariant requires that:
     * <ul>
     *   <li>{@code id} >= 0</li>
     * </ul>
     *
     * <p>If assertions are enabled, a violation will cause an {@code AssertionError}.</p>
     *
     * @effects none
     */
    private void checkRep() {
        assert id >= 0 : "Representation invariant violated: id must be >= 0";
    }



    /**
     * ADT classification: Observer (object contract).
     *
     * Two ExpenseID objects are equal iff they have the same numeric id.
     *
     * @param o other object (may be null)
     * @requires true
     * @effects none
     * @return true iff o is an ExpenseID and has the same numeric id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseID)) return false;
        ExpenseID other = (ExpenseID) o;
        return this.id == other.id;
    }

    /**
     * ADT classification: Observer (object contract).
     *
     * @requires true
     * @effects none
     * @return hash code for this id
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * ADT classification: Observer (object contract).
     *
     * @requires true
     * @effects none
     * @return string representation of id
     */
    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
