package com.rrrrz.tinyvow.data.privacy

data class LocalDataTableSummary(
    val tableName: String,
    val description: String,
    val rowCount: Int,
)

data class LocalStoreSummary(
    val name: String,
    val description: String,
    val present: Boolean,
    val fileCount: Int,
    val byteCount: Long,
)

data class LocalPrivacySnapshot(
    val exportedAtMillis: Long,
    val tableSummaries: List<LocalDataTableSummary>,
    val localStoreSummaries: List<LocalStoreSummary> = emptyList(),
    val runtimeDiagnostics: String? = null,
)

object LocalPrivacyReportFormatter {
    fun format(snapshot: LocalPrivacySnapshot): String {
        return buildString {
            appendLine("{")
            appendLine("  \"app\": \"Tiny Vow\",")
            appendLine("  \"exportedAtMillis\": ${snapshot.exportedAtMillis},")
            appendLine("  \"storage\": {")
            appendLine("    \"policy\": \"This export is generated locally on this device. Tiny Vow does not upload this file automatically.\",")
            appendLine("    \"sensitiveDataTypes\": [")
            appendLine("      \"installed app package names and labels selected by the user\",")
            appendLine("      \"usage duration, open counts, session counts, night usage, and block events\",")
            appendLine("      \"local points, rewards, reward inventory, active reward effects, reward effect benefits, achievements, and theme preferences\",")
            appendLine("      \"local account state, domestic activation state, imported reward icon files, and special app usage cache\"")
            appendLine("    ],")
            appendLine("    \"securityNotes\": [")
            appendLine("      \"This file contains table counts and local storage summaries, not full table contents.\",")
            appendLine("      \"Saved WeRead API keys are not exported in plaintext.\"")
            appendLine("    ]")
            appendLine("  },")
            appendLine("  \"tables\": [")
            snapshot.tableSummaries.forEachIndexed { index, table ->
                appendLine("    {")
                appendLine("      \"name\": \"${escape(table.tableName)}\",")
                appendLine("      \"description\": \"${escape(table.description)}\",")
                appendLine("      \"rowCount\": ${table.rowCount}")
                append("    }")
                if (index != snapshot.tableSummaries.lastIndex) append(",")
                appendLine()
            }
            appendLine("  ]")
            if (snapshot.localStoreSummaries.isNotEmpty()) {
                appendLine("  ,")
                appendLine("  \"localStores\": [")
                snapshot.localStoreSummaries.forEachIndexed { index, store ->
                    appendLine("    {")
                    appendLine("      \"name\": \"${escape(store.name)}\",")
                    appendLine("      \"description\": \"${escape(store.description)}\",")
                    appendLine("      \"present\": ${store.present},")
                    appendLine("      \"fileCount\": ${store.fileCount},")
                    appendLine("      \"byteCount\": ${store.byteCount}")
                    append("    }")
                    if (index != snapshot.localStoreSummaries.lastIndex) append(",")
                    appendLine()
                }
                appendLine("  ]")
            }
            snapshot.runtimeDiagnostics?.takeIf { it.isNotBlank() }?.let { diagnostics ->
                appendLine("  ,")
                appendLine("  \"runtimeDiagnostics\": \"${escape(diagnostics)}\"")
            }
            appendLine("}")
        }
    }

    private fun escape(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
}
