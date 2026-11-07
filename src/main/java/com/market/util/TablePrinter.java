package com.market.util;

import com.market.model.Commodity;
import com.market.model.PriceEntry;
import com.market.model.Vendor;

import java.util.List;

/** Console table helpers (formatting only, no database code). */
public final class TablePrinter {

    private TablePrinter() {
    }

    public static void printPriceTable(List<PriceEntry> entries) {
        String row = "| %-5s | %-18s | %-8s | %-18s | %10s | %-12s | %-10s |%n";
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(20) + "+" + "-".repeat(10) + "+"
                + "-".repeat(20) + "+" + "-".repeat(12) + "+" + "-".repeat(14) + "+" + "-".repeat(12) + "+";
        System.out.println(border);
        System.out.printf(row, "ID", "Commodity", "Stall", "Vendor", "Price (P)", "Date", "Status");
        System.out.println(border);
        for (PriceEntry e : entries) {
            System.out.printf(row, e.id(), cut(e.commodityName(), 18), cut(e.stallNo(), 8),
                    cut(e.vendorName(), 18), String.format("%.2f", e.price()), e.dateRecorded(),
                    e.validationStatus());
        }
        System.out.println(border);
    }

    public static void printCommodityTable(List<Commodity> list) {
        String row = "| %-5s | %-20s | %-15s | %-8s |%n";
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(22) + "+" + "-".repeat(17) + "+" + "-".repeat(10) + "+";
        System.out.println(border);
        System.out.printf(row, "ID", "Commodity", "Category", "Unit");
        System.out.println(border);
        for (Commodity c : list) {
            System.out.printf(row, c.id(), cut(c.name(), 20), cut(c.category(), 15), cut(c.unit(), 8));
        }
        System.out.println(border);
    }

    public static void printVendorTable(List<Vendor> list) {
        String row = "| %-5s | %-8s | %-20s | %-15s |%n";
        String border = "+" + "-".repeat(7) + "+" + "-".repeat(10) + "+" + "-".repeat(22) + "+" + "-".repeat(17) + "+";
        System.out.println(border);
        System.out.printf(row, "ID", "Stall", "Vendor", "Contact");
        System.out.println(border);
        for (Vendor v : list) {
            System.out.printf(row, v.id(), cut(v.stallNo(), 8), cut(v.vendorName(), 20),
                    cut(v.contactNo() == null ? "" : v.contactNo(), 15));
        }
        System.out.println(border);
    }

    private static String cut(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 1) + "~";
    }
}
