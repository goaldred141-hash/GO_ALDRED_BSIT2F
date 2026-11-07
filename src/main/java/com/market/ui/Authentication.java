package com.market.ui;

import com.market.dao.ActivityLogDao;
import com.market.dao.CommodityDao;
import com.market.dao.PriceEntryDao;
import com.market.dao.UserDao;
import com.market.dao.VendorDao;
import com.market.model.User;
import com.market.util.Constants;
import com.market.util.InputUtil;

import java.util.Scanner;

/**
 * Handles the first screen of the program: login, registration and routing
 * each logged-in user to the menu of their role. No SQL belongs here.
 */
public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final CommodityDao commodityDao;
    private final VendorDao vendorDao;
    private final PriceEntryDao priceEntryDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, ActivityLogDao activityLogDao, CommodityDao commodityDao,
                          VendorDao vendorDao, PriceEntryDao priceEntryDao, Scanner scanner) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
        this.commodityDao = commodityDao;
        this.vendorDao = vendorDao;
        this.priceEntryDao = priceEntryDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=====================================");
            System.out.println(" PUBLIC MARKET PRICE MONITORING SYSTEM");
            System.out.println("=====================================");
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
                default -> System.out.println("Invalid option. Please choose 0, 1 or 2.");
            }
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        String username = InputUtil.readNonEmpty(scanner, "Username: ");
        String password = InputUtil.readNonEmpty(scanner, "Password: ");

        User user = userDao.authenticate(username, password);
        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }
        if (!"APPROVED".equals(user.status())) {
            System.out.println("Your account is still PENDING. Please wait for an administrator to approve it.");
            return;
        }

        System.out.println("\nLogin successful. Welcome, " + user.fullName() + " (" + user.role() + ")!");
        activityLogDao.logAction(user.id(), "USER_LOGIN",
                "User " + user.username() + " (ID: " + user.id() + ") logged in.");
        routeToMenu(user);
        logout(user);
    }

    /** Called when the role menu returns, i.e. the user chose "Logout". */
    private void logout(User user) {
        activityLogDao.logAction(user.id(), "USER_LOGOUT",
                "User " + user.username() + " (ID: " + user.id() + ") logged out.");
    }

    /** Sends the user to the menu that matches the role stored in the database. */
    private void routeToMenu(User user) {
        switch (user.role()) {
            case "ADMIN" -> new AdminMenu(userDao, activityLogDao, priceEntryDao, scanner, user).show();
            case "OFFICER" -> new OfficerMenu(commodityDao, vendorDao, priceEntryDao, scanner, user).show();
            case "VIEWER" -> new ViewerMenu(priceEntryDao, scanner, user).show();
            default -> System.out.println("Unknown role '" + user.role() + "'. Please contact an administrator.");
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER NEW ACCOUNT ---");

        String username;
        while (true) {
            username = InputUtil.readNonEmpty(scanner, "Choose a username: ");
            if (username.contains(" ")) {
                System.out.println("Username must not contain spaces.");
            } else if (userDao.usernameExists(username)) {
                System.out.println("That username is already taken.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            password = InputUtil.readNonEmpty(scanner, "Choose a password (min. 6 characters): ");
            if (password.length() < 6) {
                System.out.println("Password is too short.");
                continue;
            }
            String confirm = InputUtil.readNonEmpty(scanner, "Confirm password: ");
            if (password.equals(confirm)) {
                break;
            }
            System.out.println("Passwords do not match. Try again.");
        }

        String fullName = InputUtil.readNonEmpty(scanner, "Full name: ");
        String role = chooseRole();

        if (userDao.registerUser(username, password, fullName, role)) {
            System.out.println("\nRegistration successful! Your account is PENDING until an administrator approves it.");
        } else {
            System.out.println("Registration failed. Please try again.");
        }
    }

    private String chooseRole() {
        System.out.println("Select your role:");
        System.out.println("1. Market Administrator (" + Constants.ROLES.get(0) + ")");
        System.out.println("2. Price Monitoring Officer (" + Constants.ROLES.get(1) + ")");
        System.out.println("3. Consumer / Public Viewer (" + Constants.ROLES.get(2) + ")");
        while (true) {
            int choice = InputUtil.readInt(scanner, "Role (1-3): ");
            if (choice >= 1 && choice <= Constants.ROLES.size()) {
                return Constants.ROLES.get(choice - 1);
            }
            System.out.println("Invalid choice. Please enter 1, 2 or 3.");
        }
    }
}
