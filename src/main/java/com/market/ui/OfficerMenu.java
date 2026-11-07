package com.market.ui;

import com.market.dao.CommodityDao;
import com.market.dao.PriceEntryDao;
import com.market.dao.VendorDao;
import com.market.model.Commodity;
import com.market.model.User;
import com.market.model.Vendor;
import com.market.util.InputUtil;
import com.market.util.TablePrinter;

import java.util.List;
import java.util.Scanner;

/** Price Monitoring Officer menu: encodes commodities, vendors and daily prices. */
public class OfficerMenu {

    private final CommodityDao commodityDao;
    private final VendorDao vendorDao;
    private final PriceEntryDao priceEntryDao;
    private final Scanner scanner;
    private final User currentUser;

    public OfficerMenu(CommodityDao commodityDao, VendorDao vendorDao, PriceEntryDao priceEntryDao,
                       Scanner scanner, User currentUser) {
        this.commodityDao = commodityDao;
        this.vendorDao = vendorDao;
        this.priceEntryDao = priceEntryDao;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== OFFICER MENU (" + currentUser.username() + ") =====");
            System.out.println("1. Add Commodity");
            System.out.println("2. Register Vendor / Stall");
            System.out.println("3. Encode Daily Price");
            System.out.println("4. View All Price Entries");
            System.out.println("0. Logout");
            int choice = InputUtil.readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1 -> addCommodity();
                case 2 -> registerVendor();
                case 3 -> encodePrice();
                case 4 -> viewPriceEntries();
                case 0 -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose from the menu.");
            }
        }
    }

    private void addCommodity() {
        System.out.println("\n--- ADD COMMODITY ---");
        String name = InputUtil.readNonEmpty(scanner, "Commodity name (e.g. Regular Milled Rice): ");
        String category = InputUtil.readNonEmpty(scanner, "Category (Rice / Vegetables / Fish / Meat / Fruits): ");
        String unit = InputUtil.readNonEmpty(scanner, "Unit of measure (e.g. kg): ");
        if (commodityDao.addCommodity(name, category, unit, currentUser.id())) {
            System.out.println("SUCCESS: Commodity '" + name + "' added.");
        }
    }

    private void registerVendor() {
        System.out.println("\n--- REGISTER VENDOR / STALL ---");
        String stallNo = InputUtil.readNonEmpty(scanner, "Stall number: ");
        String vendorName = InputUtil.readNonEmpty(scanner, "Vendor name: ");
        String contactNo = InputUtil.readNonEmpty(scanner, "Contact number: ");
        if (vendorDao.addVendor(stallNo, vendorName, contactNo, currentUser.id())) {
            System.out.println("SUCCESS: Vendor '" + vendorName + "' registered at stall " + stallNo + ".");
        }
    }

    private void encodePrice() {
        System.out.println("\n--- ENCODE DAILY PRICE ---");
        List<Commodity> commodities = commodityDao.getActiveCommodities();
        List<Vendor> vendors = vendorDao.getActiveVendors();
        if (commodities.isEmpty() || vendors.isEmpty()) {
            System.out.println("Please add at least one commodity and one vendor first.");
            return;
        }

        TablePrinter.printCommodityTable(commodities);
        int commodityId;
        while (true) {
            commodityId = InputUtil.readInt(scanner, "Commodity ID (0 to cancel): ");
            if (commodityId == 0) {
                System.out.println("Encoding cancelled.");
                return;
            }
            final int id = commodityId;
            if (commodities.stream().anyMatch(c -> c.id() == id)) {
                break;
            }
            System.out.println("Commodity ID not found. Try again.");
        }

        TablePrinter.printVendorTable(vendors);
        int vendorId;
        while (true) {
            vendorId = InputUtil.readInt(scanner, "Vendor ID (0 to cancel): ");
            if (vendorId == 0) {
                System.out.println("Encoding cancelled.");
                return;
            }
            final int id = vendorId;
            if (vendors.stream().anyMatch(v -> v.id() == id)) {
                break;
            }
            System.out.println("Vendor ID not found. Try again.");
        }

        double price = InputUtil.readDouble(scanner, "Price in pesos (0 to cancel): ");
        if (price == 0) {
            System.out.println("Encoding cancelled.");
            return;
        }

        if (priceEntryDao.addPriceEntry(commodityId, vendorId, price, currentUser.id())) {
            System.out.println("SUCCESS: Price entry saved with status PENDING.");
        }
    }

    private void viewPriceEntries() {
        System.out.println("\n--- ALL PRICE ENTRIES ---");
        var entries = priceEntryDao.getAllPriceEntries();
        if (entries.isEmpty()) {
            System.out.println("No price entries yet.");
            return;
        }
        TablePrinter.printPriceTable(entries);
    }
}
