package com.skillswap;

import com.skillswap.config.DatabaseConnection;
import com.skillswap.ui.ConsoleMenu;

/**
 * SkillSwap - Student Skill Exchange Platform
 * Main application entry point launching the interactive console application.
 */
public class Main {
    public static void main(String[] args) {
        if (!DatabaseConnection.testConnection()) {
            System.err.println("Fatal: Database is unavailable. Please check db.properties and MySQL server.");
            System.exit(1);
        }

        try {
            ConsoleMenu menu = new ConsoleMenu();
            menu.start();
        } finally {
            DatabaseConnection.shutdown();
        }
    }
}
