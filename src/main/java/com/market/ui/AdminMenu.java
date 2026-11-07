package com.market.ui;

import com.market.dao.ActivityLogDao;
import com.market.dao.PriceEntryDao;
import com.market.dao.UserDao;
import com.market.model.ActivityLog;
import com.market.model.PriceEntry;
import com.market.model.User;
import com.market.util.Constants;
import com.market.util.TablePrinter;

import java.util.List;
import java.util.Scanner;

/** Market Administrator menu. All database work is delegated to the DAO layer. */
public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final PriceEntryDao priceEntryDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, ActivityLogDao activityLogDao, PriceEntryDao priceEntryDao,
                     Scanner scanner, User currentAdmin) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
        this.priceEntryDao = priceEntryDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== ADMIN MENU (" + currentAdmin.username() + ") =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User (Role & Status)");
            System.out.println("5. Validate Price Entries");
            System.out.println("6. View System Audit Logs");
            System.out.println("0. Logout");
            int choice = readIntInput("Choose an option: ");

            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUserAccount();
                case 4 -> updateUser();
                case 5 -> validatePriceEntries();
                case 6 -> auditLogMenu();
                case 0 -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose from the menu.");
            }
        }
    }

    // ---------------------------------------------------------------- features

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending registrations.");
            return;
        }
        printUserTable(pending);
    }

    private void approveUserAccount() {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("There are no accounts waiting for approval.");
            return;
        }
        printUserTable(pending);

        while (true) {
            int targetId = readIntInput("Enter the User ID to approve (0 to cancel): ");
            if (targetId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            boolean isPending = pending.stream().anyMatch(u -> u.id() == targetId);
            if (!isPending) {
                System.out.println("User ID " + targetId + " is not in the pending list. Try again.");
                continue;
            }

            if (userDao.approveUser(targetId, currentAdmin.id())) {
                System.out.println("SUCCESS: User ID " + targetId + " has been APPROVED.");
            } else {
                System.out.println("Approval failed. Please try again.");
            }
            return;
        }
    }

    /** Lets the admin change another user's role and status. 0 cancels at every step. */
    private void updateUser() {
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        printUserTable(users);

        User target = null;
        while (target == null) {
            int targetUserId = readIntInput("Enter the User ID to update (0 to cancel): ");
            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }
            if (targetUserId == currentAdmin.id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }
            for (User u : users) {
                if (u.id() == targetUserId) {
                    target = u;
                    break;
                }
            }
            if (target == null) {
                System.out.println("User ID " + targetUserId + " does not exist. Try again.");
            }
        }

        System.out.println("Selected: " + target.username() + " (current role: " + target.role()
                + ", current status: " + target.status() + ")");

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }
        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(target.id(), newRole, newStatus, currentAdmin.id())) {
            System.out.println("SUCCESS: User ID " + target.id() + " is now " + newRole + " / " + newStatus + ".");
        } else {
            System.out.println("Update failed. Please try again.");
        }
    }

    /** Admin reviews PENDING price entries and marks them VALIDATED or REJECTED. */
    private void validatePriceEntries() {
        System.out.println("\n--- VALIDATE PRICE ENTRIES ---");
        List<PriceEntry> pending = priceEntryDao.getPriceEntriesByStatus("PENDING");
        if (pending.isEmpty()) {
            System.out.println("There are no price entries waiting for validation.");
            return;
        }
        TablePrinter.printPriceTable(pending);

        while (true) {
            int priceId = readIntInput("Enter the Price Entry ID (0 to cancel): ");
            if (priceId == 0) {
                System.out.println("Validation cancelled.");
                return;
            }
            boolean inList = pending.stream().anyMatch(p -> p.id() == priceId);
            if (!inList) {
                System.out.println("Price Entry " + priceId + " is not in the pending list. Try again.");
                continue;
            }

            String newStatus = null;
            while (newStatus == null) {
                int decision = readIntInput("1 = VALIDATE, 2 = REJECT, 0 = cancel: ");
                switch (decision) {
                    case 1 -> newStatus = "VALIDATED";
                    case 2 -> newStatus = "REJECTED";
                    case 0 -> {
                        System.out.println("Validation cancelled.");
                        return;
                    }
                    default -> System.out.println("Please enter 1, 2 or 0.");
                }
            }
            if (priceEntryDao.updateValidationStatus(priceId, newStatus, currentAdmin.id())) {
                System.out.println("SUCCESS: Price Entry #" + priceId + " is now " + newStatus + ".");
            } else {
                System.out.println("Update failed. Please try again.");
            }
            return;
        }
    }

    // ---------------------------------------------------------------- audit logs

    private void auditLogMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back");
            int choice = readIntInput("Choose an option: ");
            switch (choice) {
                case 1 -> printLogTable(activityLogDao.getAllLogs());
                case 2 -> filterLogsByUser();
                case 3 -> clearLogs();
                case 0 -> back = true;
                default -> System.out.println("Invalid option. Please choose from the menu.");
            }
        }
    }

    private void filterLogsByUser() {
        int userId = readIntInput("Enter the User ID to filter by (0 to cancel): ");
        if (userId == 0) {
            System.out.println("Filter cancelled.");
            return;
        }
        printLogTable(activityLogDao.getLogsByUserId(userId));
    }

    private void clearLogs() {
        System.out.print("This permanently deletes ALL audit logs. Type YES to confirm (anything else cancels): ");
        String answer = scanner.nextLine().trim();
        if (!answer.equals("YES")) {
            System.out.println("Clear cancelled. Nothing was deleted.");
            return;
        }
        int removed = activityLogDao.clearAllLogs();
        if (removed >= 0) {
            // Leave a trace so the clearing itself is never invisible.
            activityLogDao.logAction(currentAdmin.id(), "LOGS_CLEARED",
                    "Admin [" + currentAdmin.id() + "] cleared " + removed + " log entries.");
            System.out.println("SUCCESS: " + removed + " log entries deleted.");
        }
    }

    /** Aligned log table. Long descriptions wrap onto extra lines instead of being cut off. */
    private void printLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No log entries found.");
            return;
        }
        final int descWidth = 35;
        String format = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        String line = "-".repeat(98);

        System.out.println(line);
        System.out.printf(format, "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println(line);
        for (ActivityLog log : logs) {
            String desc = log.description();
            String first = desc.length() > descWidth ? desc.substring(0, descWidth) : desc;
            System.out.printf(format, log.id(), log.userId(), log.actionType(), first, log.timestamp());
            for (int i = descWidth; i < desc.length(); i += descWidth) {
                System.out.printf(format, "", "", "", desc.substring(i, Math.min(i + descWidth, desc.length())), "");
            }
        }
        System.out.println(line);
        System.out.println("Total: " + logs.size() + " log entr" + (logs.size() == 1 ? "y" : "ies"));
    }

    // ---------------------------------------------------------------- helpers

    /** Keeps asking until a whole number is typed, so letters/symbols never crash the program. */
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /** Keeps asking until a valid role is typed. Returns null if the admin enters 0 to cancel. */
    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role " + Constants.ROLES + " (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (Constants.ROLES.contains(input)) {
                return input;
            }
            System.out.println("Invalid role. Valid roles are: " + Constants.ROLES);
        }
    }

    /** Keeps asking until a valid status is typed. Returns null if the admin enters 0 to cancel. */
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status " + Constants.USER_STATUSES + " (0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (Constants.USER_STATUSES.contains(input)) {
                return input;
            }
            System.out.println("Invalid status. Valid statuses are: " + Constants.USER_STATUSES);
        }
    }

    /** Prints any list of users as an aligned table. */
    private void printUserTable(List<User> users) {
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(17) + "+" + "-".repeat(14) + "+" + "-".repeat(12) + "+";
        String rowFormat = "| %-5s | %-15s | %-12s | %-10s |%n";

        System.out.println(border);
        System.out.printf(rowFormat, "ID", "Username", "Role", "Status");
        System.out.println(border);
        for (User u : users) {
            System.out.printf(rowFormat, u.id(), u.username(), u.role(), u.status());
        }
        System.out.println(border);
        System.out.println("Total: " + users.size() + " user(s)");
    }
}
