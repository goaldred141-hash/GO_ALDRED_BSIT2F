package com.pmpms.util;

import java.util.Scanner;

/** Crash-proof console input helpers that all menus share (one Scanner for the whole app). */
public final class InputUtil {

    private InputUtil() {
    }

    public static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            String line = readLine(scanner, prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            String line = readLine(scanner, prompt);
            try {
                double value = Double.parseDouble(line);
                if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("Invalid input. Enter a positive number.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (e.g. 55.50).");
            }
        }
    }
}
