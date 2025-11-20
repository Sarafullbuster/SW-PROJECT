package model;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * ExpenseList ADT: maintains a collection of Expense objects, aggregated by category.
 *
 * <p><b>Abstraction Function (AF):</b>
 *  AF(this) = a mapping from category names to financial expenses, 
 *  where each category (case-insensitive, trimmed string) maps to 
 *  a single Expense object representing the aggregated total for that category.
 *  The abstract value is the set of these category-expense pairs.
 *
 * <p><b>Representation Invariant (RI):</b>
 *   - map != null
 *   - observers != null
 *   - no key in map is null
 *   - no value in map is null
 *   - every Expense.amount >= 0
 *
 *
 * <p><b>Safety from Rep Exposure:</b>
 *   - map and observers are private and final.
 *   - public methods return defensive/unmodifiable copies where appropriate.
 */
public class ExpenseList implements ExpenseSubject {
    private final LinkedHashMap<String, Expense> map;
    private final List<ExpenseObserver> observers;
    private static final Pattern PARSE_PATTERN =
            Pattern.compile("^\\s*([^:]+?)\\s*:\\s*([-+]?[0-9]*\\.?[0-9]+)\\s*$");
    private final Path persistencePath;

    /**
     * ADT classification: Creator.
     *
     * Create a new, empty ExpenseList.
     *
     * @requires true
     * @effects constructs an empty ExpenseList ready to accept expenses
     */
    public ExpenseList() {
        this.map = new LinkedHashMap<>();
        this.observers = new ArrayList<>();
        this.persistencePath = Paths.get(System.getProperty("user.home"), "expense_tracker_data.csv");
        checkRep();
    }

    /**
     * ADT classification: Mutator.
     *
     * Add or merge an expense. If category already exists (case-insensitive),
     * its amount will be increased (aggregate behavior).
     *
     * @param e expense to add (non-null)
     * @requires e != null
     * @effects if category exists, its amount is increased (existing aggregate preserved and replaced
     *          with a new Expense); otherwise a new entry is created.
     * @throws IllegalArgumentException if e == null
     */
    public synchronized void addExpense(Expense e) {
        if (e == null) throw new IllegalArgumentException("Expense cannot be null");
        String key = normalizeCategory(e.getCategory());
        Expense existing = map.get(key);
        if (existing != null) {
            // keep existing's ID; produce a new Expense with combined amount
            Expense merged = existing.withAddedAmount(e.getAmount());
            map.put(key, merged); // replace
        } else {
            map.put(key, e);
        }
        checkRep();
        // notify outside synchronized to avoid locking during observer callbacks
        notifyObservers();
    }

    /**
     * ADT classification: Mutator.
     *
     * Remove an expense by object (category aggregate entry).
     *
     * @param e expense to remove (may be null)
     * @requires true
     * @effects removes any aggregated entry for e.getCategory(); notifies observers if changed
     */
    public synchronized void removeExpense(Expense e) {
        if (e == null) return;
        map.remove(normalizeCategory(e.getCategory()));
        checkRep();
        notifyObservers();
    }

    /**
     * ADT classification: Mutator.
     *
     * Update an expense identified by id: change category and/or amount.
     * If category changes and collides with existing category, amounts are merged.
     *
     * @param id id of expense to update (non-null)
     * @param newCategory new category (non-null, non-empty)
     * @param newAmount new amount (>= 0)
     * @requires id != null && newCategory != null && !newCategory.isEmpty() && newAmount >= 0
     * @effects updates the corresponding Expense (possibly moving/merging it in the map), notifies observers
     * @return true if an expense with given id existed and was updated, false otherwise
     * @throws IllegalArgumentException for invalid args
     */
    public synchronized boolean updateExpense(ExpenseID id, String newCategory, double newAmount) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (newCategory == null || newCategory.isEmpty()) throw new IllegalArgumentException("newCategory invalid");
        if (newAmount < 0) throw new IllegalArgumentException("newAmount must be >= 0");

        Expense found = null;
        String foundKey = null;
        for (Map.Entry<String, Expense> ent : map.entrySet()) {
            if (ent.getValue().getId().equals(id)) {
                found = ent.getValue();
                foundKey = ent.getKey();
                break;
            }
        }
        if (found == null) return false;

        // remove old entry
        map.remove(foundKey);

        String newKey = normalizeCategory(newCategory);
        if (map.containsKey(newKey)) {
            // Merge into existing aggregate: preserve existing aggregate's ID
            Expense existing = map.get(newKey);
            Expense merged = existing.withAddedAmount(newAmount);
            map.put(newKey, merged);
        } else {
            // Move the found expense to new category, preserving found.id
            Expense updated = found.withCategoryAndAmount(newCategory, newAmount);
            map.put(newKey, updated);
        }

        checkRep();
        notifyObservers();
        return true;
    }

    /**
     * ADT classification: Observer.
     *
     * Return a defensive unmodifiable list of current aggregated expenses (in insertion order).
     *
     * @return an unmodifiable List containing current Expense objects (defensive copy)
     * @requires true
     * @effects none
     */
    public synchronized List<Expense> getExpenses() {
        return Collections.unmodifiableList(new ArrayList<>(map.values()));
    }

    /**
     * ADT classification: Observer.
     *
     * Compute total amount of all expenses.
     *
     * @return total sum (>= 0)
     * @requires true
     * @effects none
     */
    public synchronized double getTotal() {
        double sum = 0.0;
        for (Expense e : map.values()) sum += e.getAmount();
        return sum;
    }

    /**
     * ADT classification: Producer.
     *
     * Parse "category:amount" into an Expense with the provided id.
     * Uses regular expression parsing.
     *
     * @param input user input string like "Food:12.50"
     * @param id id to assign to the produced Expense (non-null)
     * @return Expense or null if parsing failed
     * @requires id != null
     * @effects none
     * @throws IllegalArgumentException if id == null
     */
    public synchronized Expense parseExpense(String input, ExpenseID id) {
        if (id == null) throw new IllegalArgumentException("id cannot be null");
        if (input == null) return null;
        Matcher m = PARSE_PATTERN.matcher(input);
        if (m.matches()) {
            String category = m.group(1).trim();
            double amount = Double.parseDouble(m.group(2));
            if (category.isEmpty() || amount < 0) return null;
            return new Expense(id, category, amount);
        }
        return null;
    }

    /**
     * ADT classification: Observer.
     *
     * Search expenses by regular expression applied to category string (case-insensitive).
     *
     * @param regex regular expression (non-null)
     * @return list of matching Expense objects (defensive copy)
     * @requires regex != null
     * @effects none
     */
    public synchronized List<Expense> searchByRegex(String regex) {
        if (regex == null) return Collections.emptyList();
        Pattern p;
        try {
            p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ex) {
            return Collections.emptyList();
        }
        List<Expense> result = new ArrayList<>();
        for (Expense e : map.values()) {
            if (p.matcher(e.getCategory()).find()) result.add(e); // Expense immutable -> safe to return
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * ADT classification: Observer (IO).
     *
     * Save current aggregated expenses to default file under user home.
     *
     * @return true if save succeeded, false otherwise
     * @requires true
     * @effects writes a CSV file to user's home (may create/overwrite)
     */
    public synchronized boolean saveToDefaultFile() {
        return saveToFile(persistencePath.toFile());
    }

    /**
     * ADT classification: Observer (IO).
     *
     * Save current aggregated expenses to a given file.
     *
     * @param f destination file (non-null)
     * @return true if saved successfully
     * @requires f != null
     * @effects writes CSV representation of current state to file f
     * @throws IllegalArgumentException if f == null
     */
    public synchronized boolean saveToFile(File f) {
        if (f == null) throw new IllegalArgumentException("file cannot be null");
        try (BufferedWriter w = Files.newBufferedWriter(f.toPath())) {
            for (Expense e : map.values()) {
                String cat = e.getCategory();
                if (cat.contains(",") || cat.contains("\"")) {
                    cat = "\"" + cat.replace("\"", "\"\"") + "\"";
                }
                w.write(cat + "," + String.format(Locale.ROOT, "%.2f", e.getAmount()));
                w.newLine();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * ADT classification: Mutator (IO).
     *
     * Load aggregated expenses from default file (overwrites current map).
     *
     * @return true if loaded successfully (file exists and parsed), false otherwise
     * @requires true
     * @effects clears current data and loads new aggregated data from default file, notifies observers
     */
    public synchronized boolean loadFromDefaultFile() {
        return loadFromFile(persistencePath.toFile());
    }

    /**
     * ADT classification: Mutator (IO).
     *
     * Load aggregated expenses from given file (overwrites current map).
     *
     * @param f source file (non-null)
     * @return true if load succeeded, false otherwise
     * @requires f != null
     * @effects clears and repopulates the map based on file contents; notifies observers on success
     * @throws IllegalArgumentException if f == null
     */
    public synchronized boolean loadFromFile(File f) {
        if (f == null) throw new IllegalArgumentException("file cannot be null");
        if (!f.exists()) return false;
        try (BufferedReader r = Files.newBufferedReader(f.toPath())) {
            map.clear();
            String line;
            int idCounter = 1;
            while ((line = r.readLine()) != null) {
            	String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String category = parts[0].trim();
                    double amount = Double.parseDouble(parts[1]);
                    ExpenseID id = new ExpenseID(idCounter++);
                    Expense e = new Expense(id, category, amount);
                    map.put(normalizeCategory(category), e);
                }
            }
            checkRep();
            notifyObservers();
            return true;
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Observer subsystem (public API of ExpenseSubject interface)
    /**
     * ADT classification: Mutator (observer management).
     *
     * Add an observer to this ExpenseList.
     *
     * @param o observer to add (non-null)
     * @requires o != null
     * @effects registers observer so it will be notified on model changes
     * @throws IllegalArgumentException if o == null
     */
    @Override
    public synchronized void addObserver(ExpenseObserver o) {
        if (o == null) throw new IllegalArgumentException("observer cannot be null");
        if (!observers.contains(o)) observers.add(o);
    }

    /**
     * ADT classification: Mutator (observer management).
     *
     * Remove an observer from this ExpenseList.
     *
     * @param o observer to remove (may be null)
     * @requires true
     * @effects removes observer if present
     */
    @Override
    public synchronized void removeObserver(ExpenseObserver o) {
        observers.remove(o);
    }

    /**
     * ADT classification: Observer (notification).
     *
     * Notify all registered observers of a change. Notifications are invoked
     * outside of the internal synchronization lock to avoid deadlock/hanging.
     *
     * @requires true
     * @effects calls modelChanged() on each registered observer (may throw runtime exceptions which are caught)
     */
    @Override
    public void notifyObservers() {
        List<ExpenseObserver> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(observers);
        }
        for (ExpenseObserver o : snapshot) {
            try {
                o.modelChanged();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String normalizeCategory(String cat) {
        return cat == null ? "" : cat.trim().toLowerCase();
    }

    /**
     * Check the representation invariant and throw AssertionError if violated.
     *
     * <p>RI:
     * <ul>
     *   <li>map != null</li>
     *   <li>observers != null</li>
     *   <li>no key in map is null</li>
     *   <li>no value in map is null</li>
     *   <li>every Expense.amount >= 0</li>
     * </ul>
     *
     * @effects none
     */
    private void checkRep() {
        assert map != null : "map is null";
        assert observers != null : "observers list is null";

        for (Map.Entry<String, Expense> e : map.entrySet()) {
            assert e.getKey() != null : "null key in map";
            assert e.getValue() != null : "null value in map";
            assert e.getValue().getAmount() >= 0 : "negative amount";
        }
    }

}