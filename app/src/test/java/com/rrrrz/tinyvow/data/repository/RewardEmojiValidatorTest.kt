package com.rrrrz.tinyvow.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardEmojiValidatorTest {
    @Test
    fun acceptsSingleEmoji() {
        assertTrue(RewardEmojiValidator.isValidSingleEmoji("🎁"))
    }

    @Test
    fun acceptsZwJSequenceEmoji() {
        assertTrue(RewardEmojiValidator.isValidSingleEmoji("👨‍👩‍👧‍👦"))
    }

    @Test
    fun acceptsFlagEmoji() {
        assertTrue(RewardEmojiValidator.isValidSingleEmoji("🇨🇳"))
    }

    @Test
    fun acceptsKeycapEmoji() {
        assertTrue(RewardEmojiValidator.isValidSingleEmoji("1️⃣"))
    }

    @Test
    fun rejectsPlainText() {
        assertFalse(RewardEmojiValidator.isValidSingleEmoji("tea"))
    }

    @Test
    fun rejectsMultipleEmojiClusters() {
        assertFalse(RewardEmojiValidator.isValidSingleEmoji("🎁🎉"))
    }
}
