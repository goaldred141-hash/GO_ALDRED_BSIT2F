package com.market;

import com.market.dao.ActivityLogDao;
import com.market.dao.CommodityDao;
import com.market.dao.DatabaseConnection;
import com.market.dao.DatabaseInitializer;
import com.market.dao.PriceEntryDao;
import com.market.dao.UserDao;
import com.market.dao.VendorDao;
import com.market.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

/** Single entry point of the application. It only starts things up; no business logic here. */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize the database (creates all tables, including activity_logs)
        try {
            DatabaseInitializer.initialize();
            System.out.println("Connected to SQLite database: " + DatabaseConnection.getDatabasePath());
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Could not initialize the database: " + e.getMessage());
            return;
        }

        // 2. Instantiate DAOs & utilities ONCE and share them everywhere
        Scanner scanner = new Scanner(System.in);
        ActivityLogDao activityLogDao = new ActivityLogDao();
        UserDao userDao = new UserDao(activityLogDao);
        CommodityDao commodityDao = new CommodityDao(activityLogDao);
        VendorDao vendorDao = new VendorDao(activityLogDao);
        PriceEntryDao priceEntryDao = new PriceEntryDao(activityLogDao);

        // 3. Hand control to the Authentication class
        new Authentication(userDao, activityLogDao, commodityDao, vendorDao, priceEntryDao, scanner).start();

        scanner.close();
    }
}
