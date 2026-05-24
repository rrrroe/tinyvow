package com.rrrrz.tinyvow.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeDiagnosticsTest {
    @Test
    fun asPlainTextFormatsTitleTimestampAndRows() {
        val diagnostics =
            RuntimeDiagnostics(
                generatedAt = "2026-05-23 12:00",
                items =
                    listOf(
                        RuntimeDiagnosticItem("Usage access", "Granted", isHealthy = true),
                        RuntimeDiagnosticItem("Accessibility", "Disabled", isHealthy = false),
                    ),
            )

        assertEquals(
            """
            Runtime diagnostics
            2026-05-23 12:00
            Usage access: Granted
            Accessibility: Disabled
            """.trimIndent(),
            diagnostics.asPlainText("Runtime diagnostics"),
        )
    }
}
