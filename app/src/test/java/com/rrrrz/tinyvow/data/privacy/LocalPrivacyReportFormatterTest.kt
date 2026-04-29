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
            ),
        )

        assertTrue(report.contains("\"exportedAtMillis\": 123"))
        assertTrue(report.contains("installed app package names"))
        assertTrue(report.contains("\"name\": \"daily_app_archives\""))
        assertTrue(report.contains("\"rowCount\": 7"))
    }
}
