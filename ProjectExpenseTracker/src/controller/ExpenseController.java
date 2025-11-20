package controller;

import model.*;
import java.io.File;
import java.util.List;

/**
 * Controller mediates between the view and the model.
 */
public class ExpenseController {
    private final ExpenseList model;
    private int nextId = 1;

    public ExpenseController(ExpenseList model) {
        this.model = model;
    }

    public boolean addExpenseFromInput(String input) {
        ExpenseID id = new ExpenseID(nextId++);
        Expense e = model.parseExpense(input, id);
        if (e != null) {
            model.addExpense(e);
            return true;
        } else {
            nextId--;
            return false;
        }
    }

    public boolean editExpense(Expense target, String newInput) {
        if (target == null) return false;
        Expense parsed = model.parseExpense(newInput, target.getId());
        if (parsed == null) return false;
        return model.updateExpense(target.getId(), parsed.getCategory(), parsed.getAmount());
    }

    public void deleteExpense(Expense e) {
        if (e != null) model.removeExpense(e);
    }

    public List<Expense> getAllExpenses() {
        return model.getExpenses();
    }

    public double getTotal() {
        return model.getTotal();
    }

    public void addObserver(ExpenseObserver o) {
        model.addObserver(o);
    }

    public void removeObserver(ExpenseObserver o) {
        model.removeObserver(o);
    }

    /** Search by regex on category */
    public List<Expense> searchExpenses(String regex) {
        return model.searchByRegex(regex);
    }

    /** Persistence */
    public boolean saveData() {
        return model.saveToDefaultFile();
    }

    public boolean loadData() {
        return model.loadFromDefaultFile();
    }

    public boolean saveTo(File f) { return model.saveToFile(f); }
    public boolean loadFrom(File f) { return model.loadFromFile(f); }
}
