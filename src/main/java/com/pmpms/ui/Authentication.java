package com.pmpms.ui;

import com.pmpms.dao.UserDao;
import com.pmpms.model.User;
import com.pmpms.util.InputUtil;
import com.pmpms.util.PasswordUtil;

import java.util.Optional;
import java.util.Scanner;

/** Login, registration and role-based routing. No SQL in this class. */
public class Authentication {

    private static final String[] ROLES = {"ADMIN", "OFFICER", "CONSUMER"};
    private static final String[] ROLE_LABELS = {
            "Market Administrator", "Price Monitoring Officer", "Consumer / Public Viewer"};

    private final UserDao userDao;
    private final Scanner scanner;
    private final AdminMenu adminMenu;
    private final OfficerMenu officerMenu;
    private final ConsumerMenu consumerMenu;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.adminMenu = new AdminMenu(userDao, scanner);
        this.officerMenu = new OfficerMenu(userDao, scanner);
        this.consumerMenu = new ConsumerMenu(userDao, scanner);
    }

    public void start() {
        System.out.println("=========================================");
        System.out.println("  PUBLIC MARKET PRICE MONITORING SYSTEM");
        System.out.println("=========================================");
        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            int choice = InputUtil.readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 1 -> login();
                case 2 -> register();
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose 0-2.");
            }
        }
    }

    // ------------------------------------------------------------------ registration

    private void register() {
        System.out.println("\n--- REGISTER NEW ACCOUNT ---");
        String username;
        while (true) {
            username = InputUtil.readLine(scanner, "Username (3-20 letters/numbers, 0 to cancel): ");
            if (username.equals("0")) {
                System.out.println("Registration cancelled.");
                return;
            }
            if (!username.matches("[A-Za-z0-9_]{3,20}")) {
                System.out.println("Invalid username. Use 3-20 letters, numbers or underscore.");
            } else if (userDao.usernameExists(username)) {
                System.out.println("Username already taken. Try another.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            password = InputUtil.readLine(scanner, "Password (min 6 characters): ");
            if (password.length() >= 6) {
                break;
            }
            System.out.println("Password too short.");
        }

        String fullName;
        while (true) {
            fullName = InputUtil.readLine(scanner, "Full name: ");
            if (!fullName.isBlank()) {
                break;
            }
            System.out.println("Full name cannot be empty.");
        }

        System.out.println("Select role:");
        for (int i = 0; i < ROLES.length; i++) {
            System.out.printf("  %d. %s (%s)%n", i + 1, ROLES[i], ROLE_LABELS[i]);
        }
        int roleChoice;
        while (true) {
            roleChoice = InputUtil.readInt(scanner, "Role number: ");
            if (roleChoice >= 1 && roleChoice <= ROLES.length) {
                break;
            }
            System.out.println("Invalid role. Choose 1-" + ROLES.length + ".");
        }

        int newId = userDao.registerUser(username, PasswordUtil.hash(password), fullName, ROLES[roleChoice - 1]);
        if (newId > 0) {
            System.out.println("SUCCESS: Account created (ID " + newId + ", role " + ROLES[roleChoice - 1] + ").");
            System.out.println("Status: PENDING - an administrator must approve it before you can log in.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    // ------------------------------------------------------------------ login / logout

    private void login() {
        System.out.println("\n--- LOGIN ---");
        String username = InputUtil.readLine(scanner, "Username: ");
        String password = InputUtil.readLine(scanner, "Password: ");

        Optional<User> found = userDao.authenticate(username, PasswordUtil.hash(password));
        if (found.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }
        User user = found.get();
        if (!"APPROVED".equalsIgnoreCase(user.status())) {
            System.out.println("Your account status is " + user.status()
                    + ". Please wait for an administrator to approve it.");
            return;
        }

        System.out.println("Welcome, " + user.fullName() + "! (" + user.role() + ")");
        routeToMenu(user);
        logout(user);
    }

    private void logout(User user) {
        System.out.println("You have been logged out.");
    }

    /** Sends the logged-in user to the menu that matches their role. */
    private void routeToMenu(User user) {
        switch (user.role().toUpperCase()) {
            case "ADMIN" -> adminMenu.start(user);
            case "OFFICER" -> officerMenu.start(user);
            case "CONSUMER" -> consumerMenu.start(user);
            default -> System.out.println("Unknown role '" + user.role() + "'. Contact an administrator.");
        }
    }
}
