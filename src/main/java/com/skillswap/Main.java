package com.skillswap;

import com.skillswap.config.DatabaseConnection;

/**
 * SkillSwap - Student Skill Exchange Platform
 * Main entry point.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE         ");
        System.out.println("==================================================");
        System.out.println("Starting Phase 2: JDBC Connection Verification...");
        
        boolean isConnected = DatabaseConnection.testConnection();
        
        if (isConnected) {
            System.out.println("--------------------------------------------------");
            System.out.println("SUCCESS: Database connection is verified and active!");
            System.out.println("Phase 2 Complete. Ready for Phase 3 (Student Model & DAO).");
            System.out.println("--------------------------------------------------");
        } else {
            System.err.println("--------------------------------------------------");
            System.err.println("ERROR: Could not establish database connection.");
            System.err.println("Please check db.properties and ensure MySQL is running.");
            System.err.println("--------------------------------------------------");
        }
        
        DatabaseConnection.shutdown();
    }
}
