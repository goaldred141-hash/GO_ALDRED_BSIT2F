package com.market.util;

import java.util.List;

/** Central place for the valid role / status values used across the system. */
public final class Constants {

    private Constants() {
    }

    /** ADMIN = Market Administrator, OFFICER = Price Monitoring Officer, VIEWER = Consumer / Public Viewer */
    public static final List<String> ROLES = List.of("ADMIN", "OFFICER", "VIEWER");

    public static final List<String> USER_STATUSES = List.of("APPROVED", "PENDING");
}
