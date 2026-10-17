package com.skillswap.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionTest {

    @Test
    @DisplayName("Verify DatabaseConnection.getConnection() returns active open connection")
    void testGetConnection() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            assertNotNull(conn, "Connection object should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
            assertTrue(conn.isValid(2), "Connection should be valid within 2 seconds timeout");
        }
    }

    @Test
    @DisplayName("Verify DatabaseConnection.testConnection() returns true")
    void testTestConnection() {
        boolean result = DatabaseConnection.testConnection();
        assertTrue(result, "testConnection() should return true when database is accessible");
    }
}
