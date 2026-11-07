package com.market.model;

/** One row of the vendors table (only the columns the menus need). */
public record Vendor(int id, String stallNo, String vendorName, String contactNo) {
}
