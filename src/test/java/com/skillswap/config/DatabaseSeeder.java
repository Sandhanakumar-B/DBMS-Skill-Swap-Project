package com.skillswap.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseSeeder {

    @Test
    @DisplayName("Seed database with sample_data.sql")
    public void seedDatabase() throws IOException, SQLException {
        Path sqlPath = Path.of("database", "sample_data.sql");
        assertTrue(Files.exists(sqlPath), "database/sample_data.sql must exist");

        String content = Files.readString(sqlPath);
        // Split statements by semicolon ignoring comments
        String[] statements = content.split(";");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String raw : statements) {
                String sql = raw.trim();
                // Strip SQL comments and empty blocks
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    stmt.execute(sql);
                }
            }
        }

        System.out.println(">>> Sample seed data successfully loaded into MySQL! <<<");
    }
}
