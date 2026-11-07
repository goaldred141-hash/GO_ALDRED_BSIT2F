package com.market.model;

/**
 * Immutable representation of a row in the users table.
 * Because this is a record, the accessors are id(), username(), fullName(), role(), status().
 */
public record User(int id, String username, String fullName, String role, String status) {
}
