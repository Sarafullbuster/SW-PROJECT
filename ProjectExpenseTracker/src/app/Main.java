package app;

import model.*;
import controller.*;
import view.*;

/**
 * Entry point — creates Model, Controller, and View, then starts the app.
 */

public class Main {
    public static void main(String[] args) {
        ExpenseList model = new ExpenseList();
        ExpenseController controller = new ExpenseController(model);

        javax.swing.SwingUtilities.invokeLater(() -> {
        	MainView view = new MainView(controller);
            view.setVisible(true);
        }); 
    }
}
