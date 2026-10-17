package com.skillswap.service;

import com.skillswap.model.PlatformReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    @Test
    @DisplayName("Should generate non-null platform report with valid statistics")
    void testGeneratePlatformSummary() throws SQLException {
        ReportService reportService = new ReportService();
        PlatformReport report = reportService.generatePlatformSummary(null);

        assertNotNull(report);
        assertTrue(report.getTotalStudents() >= 0);
        assertTrue(report.getTotalSkills() >= 0);
        assertTrue(report.getTotalRequests() >= 0);

        String formatted = reportService.formatReport(report);
        assertNotNull(formatted);
        assertTrue(formatted.contains("PLATFORM OVERVIEW"));
        assertTrue(formatted.contains("EXCHANGE REQUESTS STATUS"));
    }
}
