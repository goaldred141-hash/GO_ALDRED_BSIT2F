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
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("0. Logout");
            int choice = readIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUserAccount(admin);
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

    // ------------------------------------------------------------------ Deliverable 5

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        printUserTable(userDao.getAllUsers());
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No accounts are awaiting approval.");
            return;
        }
        printUserTable(pending);
    }

    private void approveUserAccount(User admin) {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No accounts are awaiting approval.");
            return;
        }
        printUserTable(pending);

        User target = null;
        while (target == null) {
            int id = readIntInput("Enter Target User ID to approve (0 to cancel): ");
            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }
            for (User u : pending) {
                if (u.id() == id) {
                    target = u;
                    break;
                }
            }
            if (target == null) {
                System.out.println("User ID " + id + " is not in the pending list. Try again.");
            }
        }

        boolean ok = userDao.approveUser(target.id());
        if (ok) {
            System.out.println("SUCCESS: User '" + target.username() + "' (ID " + target.id()
                    + ", " + target.role() + ") has been APPROVED.");
        } else {
            System.out.println("Approval failed. Please try again.");
        }
    }

    /** Aligned table: ID | Username | Role | Status. */
    private void printUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        String border = "+-------+-----------------+--------------+------------+";
        System.out.println(border);
        System.out.println(String.format("| %-5s | %-15s | %-12s | %-10s |", "ID", "Username", "Role", "Status"));
        System.out.println(border);
        for (User u : users) {
            String name = u.username().length() > 15 ? u.username().substring(0, 12) + "..." : u.username();
            System.out.println(String.format("| %-5s | %-15s | %-12s | %-10s |",
                    u.id(), name, u.role(), u.status()));
        }
        System.out.println(border);
    }
}
