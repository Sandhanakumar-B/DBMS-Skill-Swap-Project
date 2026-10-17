package com.skillswap.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConsoleMenuTest {

    @Test
    @DisplayName("Should run console menu loop and exit cleanly on option 0")
    void testConsoleMenuCleanExit() {
        // Simulates user entering: 16 (Summary), 15 (View All Students), 0 (Exit)
        String simulatedInput = "16\n15\n0\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner testScanner = new Scanner(in);

        ConsoleMenu menu = new ConsoleMenu(
                new com.skillswap.service.StudentService(),
                new com.skillswap.service.SkillService(),
                new com.skillswap.service.ExchangeService(),
                new com.skillswap.service.SkillMatchingService(),
                new com.skillswap.service.ReportService(),
                testScanner
        );

        assertDoesNotThrow(menu::start);
    }

    @Test
    @DisplayName("Should handle invalid non-numeric input and out-of-range choices without crashing")
    void testConsoleMenuInvalidInput() {
        // Simulates user entering: "invalid", "99", "   ", "0" (Exit)
        String simulatedInput = "invalid\n99\n   \n0\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        Scanner testScanner = new Scanner(in);

        ConsoleMenu menu = new ConsoleMenu(
                new com.skillswap.service.StudentService(),
                new com.skillswap.service.SkillService(),
                new com.skillswap.service.ExchangeService(),
                new com.skillswap.service.SkillMatchingService(),
                new com.skillswap.service.ReportService(),
                testScanner
        );

        assertDoesNotThrow(menu::start);
    }
}
