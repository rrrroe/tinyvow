package com.rrrrz.tinyvow.data.privacy

import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPrivacyReportFormatterTest {
    @Test
    fun format_includesSensitiveDataDisclosureAndTableCounts() {
        val report = LocalPrivacyReportFormatter.format(
            LocalPrivacySnapshot(
                exportedAtMillis = 123L,
                tableSummaries = listOf(
                    LocalDataTableSummary(
                        tableName = "daily_app_archives",
                        description = "Per-app usage",
                        rowCount = 7,
                    ),
                ),
                localStoreSummaries = listOf(
                    LocalStoreSummary(
                        name = "managed_app_preferences",
                        description = "DataStore preferences",
                        present = true,
                        fileCount = 1,
                        byteCount = 128L,
                    ),
                ),
                runtimeDiagnostics = "Runtime diagnostics\nUsage access: Granted",
            ),
        )

        assertTrue(report.contains("\"exportedAtMillis\": 123"))
        assertTrue(report.contains("installed app package names"))
        assertTrue(report.contains("local account state"))
        assertTrue(report.contains("\"securityNotes\""))
        assertTrue(report.contains("not full table contents"))
        assertTrue(report.contains("WeRead API keys are not exported in plaintext"))
        assertTrue(report.contains("\"name\": \"daily_app_archives\""))
        assertTrue(report.contains("\"rowCount\": 7"))
        assertTrue(report.contains("\"localStores\""))
        assertTrue(report.contains("\"name\": \"managed_app_preferences\""))
        assertTrue(report.contains("\"byteCount\": 128"))
        assertTrue(report.contains("\"runtimeDiagnostics\""))
        assertTrue(report.contains("Usage access: Granted"))
    }
}
