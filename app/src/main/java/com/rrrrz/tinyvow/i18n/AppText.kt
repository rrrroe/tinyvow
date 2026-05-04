package com.rrrrz.tinyvow.i18n

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object AppText {
    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var currentLanguage: AppLanguage = AppLanguage.SYSTEM

    fun attach(context: Context) {
        appContext = context.applicationContext
    }

    fun currentLanguage(): AppLanguage = currentLanguage

    fun setLanguage(language: AppLanguage, context: Context? = appContext) {
        currentLanguage = language
        context?.let { applyFrameworkLocale(it.applicationContext, language) }
    }

    fun t(key: String, vararg args: Any?): String {
        val context = createLocalizedContext(appContext ?: return key, currentLanguage)
        val id = context.resources.getIdentifier(key, "string", context.packageName)
        val template = if (id != 0) context.getString(id) else key
        if (args.isEmpty()) return template
        return runCatching {
            String.format(localeFor(context), template, *args)
        }.getOrElse {
            template
        }
    }

    fun localizedContext(context: Context, language: AppLanguage = currentLanguage): Context =
        createLocalizedContext(context, language)

    private fun createLocalizedContext(context: Context, language: AppLanguage): Context {
        if (language.languageTag == null) return context
        val locale = Locale.forLanguageTag(language.languageTag)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun applyFrameworkLocale(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        runCatching {
            val targetTags = language.languageTag.orEmpty()
            val localeManager = context.getSystemService(LocaleManager::class.java) ?: return@runCatching
            if (localeManager.applicationLocales.toLanguageTags() == targetTags) return@runCatching
            localeManager.applicationLocales =
                if (targetTags.isBlank()) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(targetTags)
                }
        }
    }

    private fun localeFor(context: Context): Locale =
        context.resources.configuration.locales.get(0) ?: Locale.getDefault()
}
