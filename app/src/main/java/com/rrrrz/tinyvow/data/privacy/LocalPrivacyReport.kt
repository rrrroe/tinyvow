package com.rrrrz.tinyvow.data.privacy

data class LocalDataTableSummary(
    val tableName: String,
    val description: String,
    val rowCount: Int,
)

data class LocalPrivacySnapshot(
    val exportedAtMillis: Long,
    val tableSummaries: List<LocalDataTableSummary>,
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
            appendLine("      \"local points, rewards, reward inventory, active reward effects, achievements, and theme preferences\"")
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
