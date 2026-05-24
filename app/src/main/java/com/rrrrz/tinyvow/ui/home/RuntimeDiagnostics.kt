package com.rrrrz.tinyvow.ui.home

internal data class RuntimeDiagnosticItem(
    val label: String,
    val value: String,
    val isHealthy: Boolean? = null,
)

internal data class RuntimeDiagnostics(
    val generatedAt: String,
    val items: List<RuntimeDiagnosticItem>,
) {
    fun asPlainText(title: String): String =
        buildString {
            appendLine(title)
            appendLine(generatedAt)
            items.forEach { item ->
                appendLine("${item.label}: ${item.value}")
            }
        }.trim()
}
