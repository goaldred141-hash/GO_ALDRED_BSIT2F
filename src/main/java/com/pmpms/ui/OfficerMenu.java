package com.pmpms.ui;

import com.pmpms.dao.UserDao;
import com.pmpms.model.User;
import com.pmpms.util.InputUtil;

import java.util.Scanner;

/** Menu for the Price Monitoring Officer role. */
public class OfficerMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public OfficerMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start(User officer) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== OFFICER MENU - " + officer.fullName() + " =====");
            System.out.println("(Officer features arrive in Deliverable 8.)");
            System.out.println("0. Logout");
            int choice = InputUtil.readInt(scanner, "Choose an option: ");
            switch (choice) {
                case 0 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
