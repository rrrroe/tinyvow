package com.rrrrz.tinyvow.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppLanguageTest {
    @Test
    fun fromStorageValue_mapsKnownValuesAndFallsBackToSystem() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStorageValue(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStorageValue("unknown"))
        assertEquals(AppLanguage.ZH_CN, AppLanguage.fromStorageValue("zh-CN"))
        assertEquals(AppLanguage.EN, AppLanguage.fromStorageValue("en"))
    }

    @Test
    fun defaultStringResourcesDoNotContainChineseText() {
        val defaultValues = File("src/main/res/values")
        assertTrue(defaultValues.exists())
        val defaultText = defaultValues.listFiles { file -> file.extension == "xml" }
            .orEmpty()
            .joinToString("\n") { it.readText() }
        assertFalse(Regex("[\\u3400-\\u9fff]").containsMatchIn(defaultText))
    }

    @Test
    fun defaultStringResourcesDoNotContainMachineTranslationResidue() {
        val defaultStrings = File("src/main/res/values").listFiles { file -> file.extension == "xml" }
            .orEmpty()
            .joinToString("\n") { it.readText() }
        val residues = listOf("texttext", "todaytext", "textapp", "usagetext", "daystext", "apptext")
        residues.forEach { residue ->
            assertFalse("Unexpected machine translation residue: $residue", defaultStrings.contains(residue))
        }
    }

    @Test
    fun generatedAutoStringKeysAreRemovedFromMainSources() {
        val generatedAutoKey = Regex("auto_[0-9a-f]{12}")
        val mainFiles = File("src/main").walkTopDown().filter { it.isFile }
        val offenders = mainFiles.filter { generatedAutoKey.containsMatchIn(it.readText()) }.toList()
        assertTrue("Generated auto string keys remain in: ${offenders.joinToString { it.path }}", offenders.isEmpty())
    }

    @Test
    fun localizedStringResourcesHaveMatchingKeysAndPlaceholders() {
        val defaultStrings = parseStringResources(File("src/main/res/values"))
        val chineseStrings = parseStringResources(File("src/main/res/values-zh-rCN"))

        assertEquals(defaultStrings.keys.sorted(), chineseStrings.keys.sorted())
        defaultStrings.forEach { (key, defaultValue) ->
            assertEquals(
                "Placeholder mismatch for $key",
                placeholders(defaultValue),
                placeholders(chineseStrings.getValue(key)),
            )
        }
    }

    @Test
    fun mainSourceStringKeysExistInResources() {
        val resourceKeys = buildSet {
            addAll(parseStringResources(File("src/main/res/values/app_texts.xml")).keys)
            addAll(parseStringResources(File("src/main/res/values/strings.xml")).keys)
        }
        val mainSourceText = File("src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .joinToString("\n") { it.readText() }

        val referencedKeys = buildSet {
            Regex("""AppText\.t\("([^"]+)""")
                .findAll(mainSourceText)
                .map { it.groupValues[1] }
                .filterNot { it.contains('$') }
                .forEach { add(it) }
            Regex("""stringResource\(R\.string\.([A-Za-z0-9_]+)""")
                .findAll(mainSourceText)
                .forEach { add(it.groupValues[1]) }
        }

        assertTrue(
            "Missing string resources: ${
                referencedKeys.filterNot { it in resourceKeys }.sorted().joinToString()
            }",
            referencedKeys.all { it in resourceKeys },
        )
    }

    @Test
    fun localeConfigDeclaresSupportedLanguages() {
        val localeConfig = File("src/main/res/xml/locales_config.xml")
        assertTrue(localeConfig.exists())
        val content = localeConfig.readText()
        assertTrue(content.contains("android:name=\"en\""))
        assertTrue(content.contains("android:name=\"zh-CN\""))
    }

    @Test
    fun editableAppCopyLivesInDedicatedResourceFiles() {
        val defaultAppTexts = File("src/main/res/values/app_texts.xml")
        val chineseAppTexts = File("src/main/res/values-zh-rCN/app_texts.xml")
        assertTrue(defaultAppTexts.exists())
        assertTrue(chineseAppTexts.exists())
        assertTrue(parseStringResources(defaultAppTexts).size > 100)
        assertTrue(parseStringResources(chineseAppTexts).size > 100)
    }

    private fun parseStringResources(path: File): Map<String, String> {
        assertTrue(path.exists())
        val text = if (path.isDirectory) {
            path.listFiles { file -> file.extension == "xml" }
                .orEmpty()
                .joinToString("\n") { it.readText() }
        } else {
            path.readText()
        }
        return Regex("""<string\s+name="([^"]+)"[^>]*>([\s\S]*?)</string>""")
            .findAll(text)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun placeholders(value: String): List<String> =
        Regex("""%(?!%)(\d+\$)?(\.\d+)?[sdf]""")
            .findAll(value)
            .map { it.value }
            .toList()
}
