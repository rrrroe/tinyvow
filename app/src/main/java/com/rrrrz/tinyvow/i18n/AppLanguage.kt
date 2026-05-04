package com.rrrrz.tinyvow.i18n

enum class AppLanguage(
    val storageValue: String,
    val languageTag: String?,
) {
    SYSTEM("system", null),
    ZH_CN("zh-CN", "zh-CN"),
    EN("en", "en");

    companion object {
        fun fromStorageValue(value: String?): AppLanguage =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}
