package com.rrrrz.tinyvow.data.repository

internal object RewardEmojiValidator {
    fun isValidSingleEmoji(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return false
        if (!containsEmojiProperty(trimmed)) return false
        val cluster = parseEmojiCluster(trimmed, 0) ?: return false
        return cluster == trimmed.length
    }

    private fun containsEmojiProperty(value: String): Boolean {
        var index = 0
        while (index < value.length) {
            val codePoint = value.codePointAt(index)
            if (hasEmojiProperty(codePoint)) {
                return true
            }
            index += Character.charCount(codePoint)
        }
        return false
    }

    private fun parseEmojiCluster(value: String, start: Int): Int? {
        var index = start
        val first = value.codePointAt(index)
        when {
            isRegionalIndicator(first) -> {
                index += Character.charCount(first)
                if (index >= value.length) return null
                val second = value.codePointAt(index)
                if (!isRegionalIndicator(second)) return null
                index += Character.charCount(second)
                return if (index == value.length) index else null
            }
            isKeycapBase(first) -> {
                index += Character.charCount(first)
                index = consumeVariationSelectors(value, index)
                if (index >= value.length) return null
                val keycap = value.codePointAt(index)
                if (keycap != KEYCAP_ENCLOSING) return null
                index += Character.charCount(keycap)
                return if (index == value.length) index else null
            }
            isEmojiBase(first) -> {
                index = consumeEmojiUnit(value, index) ?: return null
            }
            else -> return null
        }

        while (index < value.length) {
            val next = value.codePointAt(index)
            if (next == ZERO_WIDTH_JOINER) {
                index += Character.charCount(next)
                index = consumeEmojiUnit(value, index) ?: return null
            } else if (isTagSpec(next)) {
                index += Character.charCount(next)
                while (index < value.length) {
                    val tag = value.codePointAt(index)
                    index += Character.charCount(tag)
                    if (tag == CANCEL_TAG) {
                        break
                    }
                    if (!isTagSpec(tag)) return null
                }
            } else {
                return null
            }
        }
        return index
    }

    private fun consumeEmojiUnit(value: String, start: Int): Int? {
        if (start >= value.length) return null
        val base = value.codePointAt(start)
        if (!isEmojiBase(base)) return null
        var index = start + Character.charCount(base)
        index = consumeVariationSelectors(value, index)
        if (index < value.length) {
            val modifier = value.codePointAt(index)
            if (isEmojiModifier(modifier)) {
                index += Character.charCount(modifier)
            }
        }
        return index
    }

    private fun consumeVariationSelectors(value: String, start: Int): Int {
        var index = start
        while (index < value.length) {
            val codePoint = value.codePointAt(index)
            if (codePoint == VARIATION_SELECTOR_15 || codePoint == VARIATION_SELECTOR_16) {
                index += Character.charCount(codePoint)
            } else {
                break
            }
        }
        return index
    }

    private fun hasEmojiProperty(codePoint: Int): Boolean =
        AndroidIcuEmojiPropertyChecker.hasEmojiProperty(codePoint) || fallbackEmojiProperty(codePoint)

    private fun fallbackEmojiProperty(codePoint: Int): Boolean =
        when {
            codePoint in 0x1F1E6..0x1F1FF -> true
            codePoint in 0x1F300..0x1F5FF -> true
            codePoint in 0x1F600..0x1F64F -> true
            codePoint in 0x1F680..0x1F6FF -> true
            codePoint in 0x1F700..0x1F77F -> true
            codePoint in 0x1F780..0x1F7FF -> true
            codePoint in 0x1F800..0x1F8FF -> true
            codePoint in 0x1F900..0x1F9FF -> true
            codePoint in 0x1FA70..0x1FAFF -> true
            codePoint in 0x2600..0x26FF -> true
            codePoint in 0x2700..0x27BF -> true
            codePoint in 0x2300..0x23FF -> true
            codePoint == 0x00A9 || codePoint == 0x00AE || codePoint == 0x203C || codePoint == 0x2049 -> true
            codePoint == 0x2122 || codePoint == 0x2139 || codePoint == 0x3030 || codePoint == 0x303D -> true
            codePoint == 0x3297 || codePoint == 0x3299 || codePoint == 0x24C2 -> true
            isKeycapBase(codePoint) -> true
            else -> false
        }

    private fun isEmojiBase(codePoint: Int): Boolean =
        hasEmojiProperty(codePoint) && !isRegionalIndicator(codePoint) && !isKeycapBase(codePoint)

    private fun isRegionalIndicator(codePoint: Int): Boolean = codePoint in 0x1F1E6..0x1F1FF

    private fun isEmojiModifier(codePoint: Int): Boolean = codePoint in 0x1F3FB..0x1F3FF

    private fun isKeycapBase(codePoint: Int): Boolean =
        codePoint in '0'.code..'9'.code || codePoint == '#'.code || codePoint == '*'.code

    private fun isTagSpec(codePoint: Int): Boolean = codePoint in 0xE0020..0xE007E || codePoint == CANCEL_TAG

    private object AndroidIcuEmojiPropertyChecker {
        private val checker: ((Int) -> Boolean)? =
            runCatching {
                val uCharacter = Class.forName("android.icu.lang.UCharacter")
                val uProperty = Class.forName("android.icu.lang.UProperty")
                val property = uProperty.getField("EMOJI").getInt(null)
                val method =
                    uCharacter.getMethod(
                        "hasBinaryProperty",
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                    )
                ({ codePoint: Int ->
                    method.invoke(null, codePoint, property) as Boolean
                })
            }.getOrNull()

        fun hasEmojiProperty(codePoint: Int): Boolean =
            runCatching { checker?.invoke(codePoint) == true }.getOrDefault(false)
    }

    private const val ZERO_WIDTH_JOINER = 0x200D
    private const val VARIATION_SELECTOR_15 = 0xFE0E
    private const val VARIATION_SELECTOR_16 = 0xFE0F
    private const val KEYCAP_ENCLOSING = 0x20E3
    private const val CANCEL_TAG = 0xE007F
}
