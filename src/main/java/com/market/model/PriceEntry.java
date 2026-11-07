package com.market.model;

/** A price entry joined with its commodity and vendor names, ready for display. */
public record PriceEntry(int id, String commodityName, String stallNo, String vendorName,
                         double price, String dateRecorded, String validationStatus) {
}
