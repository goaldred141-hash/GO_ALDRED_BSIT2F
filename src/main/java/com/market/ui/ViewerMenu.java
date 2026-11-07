package com.market.ui;

import com.market.dao.PriceEntryDao;
import com.market.model.PriceEntry;
import com.market.model.User;
import com.market.util.InputUtil;
import com.market.util.TablePrinter;

import java.util.List;
import java.util.Scanner;

/** Consumer / Public Viewer menu: read-only access to validated prices. */
public class ViewerMenu {

    private final PriceEntryDao priceEntryDao;
    private final Scanner scanner;
    private final User currentUser;

    public ViewerMenu(PriceEntryDao priceEntryDao, Scanner scanner, User currentUser) {
        this.priceEntryDao = priceEntryDao;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== VIEWER MENU (" + currentUser.username() + ") =====");
            System.out.println("1. View Current Validated Prices");
            System.out.println("0. Logout");
            int choice = InputUtil.readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1 -> {
                    List<PriceEntry> prices = priceEntryDao.getPriceEntriesByStatus("VALIDATED");
                    if (prices.isEmpty()) {
                        System.out.println("No validated prices are available yet.");
                    } else {
                        TablePrinter.printPriceTable(prices);
                    }
                }
                case 0 -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose from the menu.");
            }
        }
    }
}
