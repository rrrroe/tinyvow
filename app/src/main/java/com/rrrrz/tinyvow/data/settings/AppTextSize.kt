package com.rrrrz.tinyvow.data.settings

enum class AppTextSize(
    val storageValue: String,
    val fontScale: Float,
) {
    EXTRA_SMALL(
        storageValue = "extra_small",
        fontScale = 0.875f,
    ),
    SMALL(
        storageValue = "small",
        fontScale = 0.9375f,
    ),
    STANDARD(
        storageValue = "standard",
        fontScale = 1f,
    ),
    LARGE(
        storageValue = "large",
        fontScale = 1.1f,
    ),
    ;

    companion object {
        fun fromStorageValue(value: String?): AppTextSize =
            when (value) {
                "extra_large" -> LARGE
                else -> entries.firstOrNull { it.storageValue == value } ?: STANDARD
            }
    }
}
