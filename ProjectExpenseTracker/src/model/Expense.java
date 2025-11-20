package model;

import java.util.Objects;

/**
 * Represents a single expense entry (immutable).
 *
 * <p><b>Abstraction Function (AF):</b>
 * AF(this) = a financial expense represented as (id, category, amount).
 *
 * <p><b>Representation Invariant (RI):</b>
 * - id != null
 * - category != null && !category.isEmpty()
 * - amount >= 0
 *
 * <p><b>Safety from Rep Exposure:</b>
 * - All fields are private and final; class is immutable.
 * - No methods return internal mutable objects.
 */
public final class Expense {

    private final ExpenseID id;   // immutable identifier
    private final String category;      // immutable category
    private final double amount;        // immutable, >= 0

    /**
     * ADT classification: Creator.
     *
     * Create a new Expense with the given id, category, and amount.
     *
     * @param id unique identifier for this expense (non-null)
     * @param category expense category (non-null, non-empty)
     * @param amount non-negative amount
     * @requires id != null && category != null && !category.isEmpty() && amount >= 0
     * @effects constructs a new immutable Expense object with the given fields
     * @throws NullPointerException if id or category is null
     * @throws IllegalArgumentException if category is empty or amount < 0
     */
    public Expense(ExpenseID id, String category, double amount) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");
        if (category.isEmpty()) throw new IllegalArgumentException("category cannot be empty");
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.amount = amount;
        checkRep();
    }

    /**
     * ADT classification: Observer.
     *
     * Return the immutable id of this expense.
     *
     * @return expense id (non-null)
     * @requires true
     * @effects none
     */
    public ExpenseID getId() { return id; }

    /**
     * ADT classification: Observer.
     *
     * Return the category string.
     *
     * @return category (non-null, non-empty)
     * @requires true
     * @effects none
     */
    public String getCategory() { return category; }

    /**
     * ADT classification: Observer.
     *
     * Return the amount.
     *
     * @return amount (>= 0)
     * @requires true
     * @effects none
     */
    public double getAmount() { return amount; }

    /**
     * ADT classification: Producer.
     *
     * Produce a new Expense with added amount (producer).
     * Does not modify this object.
     *
     * @param delta non-negative amount to add
     * @return a new Expense whose amount = this.amount + delta (same id, same category)
     * @requires delta >= 0
     * @effects none
     * @throws IllegalArgumentException if delta < 0
     */
    public Expense withAddedAmount(double delta) {
        if (delta < 0) throw new IllegalArgumentException("delta must be >= 0");
        return new Expense(this.id, this.category, this.amount + delta);
    }

    /**
     * ADT classification: Producer.
     *
     * Produce a new Expense with a new category and amount (producer).
     * Useful for moving an expense into another category.
     *
     * @param newCategory non-null, non-empty category
     * @param newAmount non-negative amount
     * @return a new Expense with same id, new category, and newAmount
     * @requires newCategory != null && !newCategory.isEmpty() && newAmount >= 0
     * @effects none
     * @throws NullPointerException if newCategory is null
     * @throws IllegalArgumentException if newCategory is empty or newAmount < 0
     */
    public Expense withCategoryAndAmount(String newCategory, double newAmount) {
        return new Expense(this.id, newCategory, newAmount);
    }

    /**
     * Checks the representation invariant of this Expense.
     *
     * <p>The representation invariant requires that:
     * <ul>
     *   <li>{@code id} is non-null</li>
     *   <li>{@code category} is non-null and non-empty</li>
     *   <li>{@code amount} >= 0</li>
     * </ul>
     *
     * <p>If assertions are enabled, any violation will cause an {@code AssertionError}.</p>
     *
     * @effects none
     */
    private void checkRep() {
        assert id != null : "RI violated: id is null";
        assert category != null && !category.isEmpty() : "RI violated: invalid category";
        assert amount >= 0 : "RI violated: amount < 0";
    }



    /**
     * ADT classification: Observer (object contract).
     *
     * Return a short human-readable representation.
     *
     * @return string like "category - amount"
     * @requires true
     * @effects none
     */
    @Override
    public String toString() {
        return String.format("%s - %.2f", category, amount);
    }

    /**
     * ADT classification: Observer (object contract).
     *
     * Equality is based on ExpenseID (logical identity).
     * Two expenses are equal if they share the same id.
     *
     * @param obj other object (may be null)
     * @requires true
     * @effects none
     * @return true if obj is an Expense with same id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Expense)) return false;
        Expense other = (Expense) obj;
        return id.equals(other.id);
    }

    /**
     * ADT classification: Observer (object contract).
     *
     * Hash code consistent with equals (based on id).
     *
     * @requires true
     * @effects none
     * @return hash code based on id
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
