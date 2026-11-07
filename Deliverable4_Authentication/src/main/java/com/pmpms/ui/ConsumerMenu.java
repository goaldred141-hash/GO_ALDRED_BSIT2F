package com.pmpms.ui;

import com.pmpms.dao.UserDao;
import com.pmpms.model.User;
import com.pmpms.util.InputUtil;

import java.util.Scanner;

/** Read-only menu for the Consumer / Public Viewer role. */
public class ConsumerMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public ConsumerMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start(User consumer) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== CONSUMER MENU - " + consumer.fullName() + " =====");
            System.out.println("(Consumer features arrive in Deliverable 8.)");
            System.out.println("0. Logout");
            int choice = InputUtil.readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 0 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
