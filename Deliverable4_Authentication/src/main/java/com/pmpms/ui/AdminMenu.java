package com.pmpms.ui;

import com.pmpms.dao.UserDao;
import com.pmpms.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menu for the Market Administrator. No SQL in this class - everything goes through DAOs. */
public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== ADMIN MENU - " + admin.fullName() + " =====");
            System.out.println("(Admin features arrive in Deliverable 5.)");
            System.out.println("0. Logout");
            int choice = readIntInput("Choose an option: ");
            switch (choice) {
                case 0 -> running = false;
                default -> System.out.println("Invalid option. Please choose from the menu.");
            }
        }
    }

    // ------------------------------------------------------------------ input helpers

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /** Keeps asking until the user types a whole number - letters/symbols never crash the app. */
    private int readIntInput(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter numbers only.");
            }
        }
    }
}
