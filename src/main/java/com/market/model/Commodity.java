package com.market.model;

/** One row of the commodities table (only the columns the menus need). */
public record Commodity(int id, String name, String category, String unit) {
}
