package com.pmpms;

import com.pmpms.dao.DatabaseInitializer;
import com.pmpms.dao.UserDao;
import com.pmpms.ui.Authentication;

import java.util.Scanner;

/**
 * Single startup entry point.
 * Flow: Initialize Database -> Instantiate DAOs & Utilities -> Authentication.start()
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize database (tables must exist before any menu appears)
        DatabaseInitializer.initialize();

        // 2. Instantiate shared DAOs and utilities (one instance of each, used everywhere)
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // 3. Delegate everything else to the Authentication UI
        Authentication authentication = new Authentication(userDao, scanner);
        authentication.start();

        scanner.close();
    }
}
