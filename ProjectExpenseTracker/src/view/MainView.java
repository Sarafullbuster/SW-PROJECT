package view;

import controller.ExpenseController;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Final GUI for Expense Tracker
 * - Separate category & amount fields
 * - Search (regex)
 * - Export (user chooses target CSV file)
 * - Import (user selects CSV file)
 * - Auto-load on startup and auto-save on exit (default user-home CSV)
 * - Observer-driven UI updates
 */
public class MainView extends JFrame implements ExpenseObserver {
    private final ExpenseController controller;
    private final DefaultListModel<Expense> listModel;
    private final JList<Expense> expenseList;
    private final JTextField categoryField;
    private final JTextField amountField;
    private final JTextField searchField;
    private final JLabel totalLabel;

    public MainView(ExpenseController controller) {
        this.controller = controller;
        setTitle("Expense Tracker");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(640, 540);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Input grid
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        inputPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        // Buttons
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton settingsBtn = new JButton("Settings");
        JButton exportBtn = new JButton("Export List");
        JButton importBtn = new JButton("Import List");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(settingsBtn);
        btnPanel.add(exportBtn);
        btnPanel.add(importBtn);

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(6,6));
        searchField = new JTextField();
        JButton searchBtn = new JButton("Search (regex)");
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        // List & total
        listModel = new DefaultListModel<>();
        expenseList = new JList<>(listModel);
        totalLabel = new JLabel("Total: 0.00 " + SettingsManager.getInstance().getCurrency());

        JPanel topPanel = new JPanel(new BorderLayout(6,6));
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(btnPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(expenseList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addBtn.addActionListener(e -> onAdd());
        delBtn.addActionListener(e -> onDelete());
        editBtn.addActionListener(e -> onEdit());
        settingsBtn.addActionListener(e -> onSettings());
        searchBtn.addActionListener(e -> onSearch());

        exportBtn.addActionListener(e -> onExport());
        importBtn.addActionListener(e -> onImport());

        // Register as observer and initial load (auto-load)
        controller.addObserver(this);
        controller.loadData(); // attempt load on startup
        refreshList();

        // Auto-save on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.saveData(); // automatic persistence to default CSV in user-home
                dispose();
                System.exit(0);
            }
        });
    }

    private void onAdd() {
        String category = categoryField.getText().trim();
        String amountText = amountField.getText().trim();
        if (category.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both category and amount.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            if (amount < 0) {
                JOptionPane.showMessageDialog(this, "Amount must be a positive number.");
                return;
            }
            String input = category + ":" + amount;
            if (controller.addExpenseFromInput(input)) {
                categoryField.setText("");
                amountField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.");
        }
    }

    private void onDelete() {
        Expense selected = expenseList.getSelectedValue();
        if (selected != null) {
            controller.deleteExpense(selected);
        }
    }

    private void onEdit() {
        Expense selected = expenseList.getSelectedValue();
        if (selected != null) {
            String newCategory = JOptionPane.showInputDialog(this, "Edit category:", selected.getCategory());
            String newAmount = JOptionPane.showInputDialog(this, "Edit amount:", String.format("%.2f", selected.getAmount()));
            if (newCategory != null && newAmount != null) {
                try {
                    double amt = Double.parseDouble(newAmount);
                    if (amt < 0) {
                        JOptionPane.showMessageDialog(this, "Amount must be a positive number.");
                        return;
                    }
                    String newInput = newCategory + ":" + amt;
                    if (!controller.editExpense(selected, newInput)) {
                        JOptionPane.showMessageDialog(this, "Failed to update expense.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Amount must be numeric.");
                }
            }
        }
    }

    private void onSettings() {
        String cur = SettingsManager.getInstance().getCurrency();
        String newCur = JOptionPane.showInputDialog(this, "Set currency (e.g., SAR, USD):", cur);
        if (newCur != null && !newCur.trim().isEmpty()) {
            SettingsManager.getInstance().setCurrency(newCur.trim());
            refreshList();
        }
    }

    private void onSearch() {
        String regex = searchField.getText().trim();
        if (regex.isEmpty()) {
            refreshList();
            return;
        }
        List<Expense> results = controller.searchExpenses(regex);
        listModel.clear();
        for (Expense e : results) listModel.addElement(e);
        totalLabel.setText(String.format("Total: %.2f %s (search)", calculateSum(results), SettingsManager.getInstance().getCurrency()));
    }

    private void onExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export expenses to CSV");
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // ensure filename ends with .csv
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            boolean ok = controller.saveTo(file);
            JOptionPane.showMessageDialog(this, ok ? "Export successful." : "Export failed.");
        }
    }

    private void onImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import expenses from CSV");
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean ok = controller.loadFrom(file);
            JOptionPane.showMessageDialog(this, ok ? "Import successful." : "Import failed or invalid file.");
        }
    }

    private double calculateSum(List<Expense> list) {
        double s = 0.0;
        for (Expense e : list) s += e.getAmount();
        return s;
    }

    private void refreshList() {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (Expense e : controller.getAllExpenses()) listModel.addElement(e);
            totalLabel.setText(String.format("Total: %.2f %s", controller.getTotal(), SettingsManager.getInstance().getCurrency()));
        });
    }
    
    /**
     * Called automatically when the model updates.
     * Refreshes the displayed list and total.
     */
    
    @Override
    public void modelChanged() {
        refreshList();
    }
}
