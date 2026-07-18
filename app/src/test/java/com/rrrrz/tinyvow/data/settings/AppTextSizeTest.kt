package com.rrrrz.tinyvow.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppTextSizeTest {
    @Test
    fun fromStorageValue_mapsKnownValuesAndFallsBackToStandard() {
        assertEquals(AppTextSize.STANDARD, AppTextSize.fromStorageValue(null))
        assertEquals(AppTextSize.STANDARD, AppTextSize.fromStorageValue("unknown"))
        assertEquals(AppTextSize.EXTRA_SMALL, AppTextSize.fromStorageValue("extra_small"))
        assertEquals(AppTextSize.SMALL, AppTextSize.fromStorageValue("small"))
        assertEquals(AppTextSize.STANDARD, AppTextSize.fromStorageValue("standard"))
        assertEquals(AppTextSize.LARGE, AppTextSize.fromStorageValue("large"))
        assertEquals(AppTextSize.LARGE, AppTextSize.fromStorageValue("extra_large"))
    }

    @Test
    fun fontScales_areOrderedAroundTheExistingStandardSize() {
        assertEquals(1f, AppTextSize.STANDARD.fontScale, 0f)
        assertEquals(0.875f, AppTextSize.EXTRA_SMALL.fontScale, 0f)
        assertTrue(AppTextSize.EXTRA_SMALL.fontScale < AppTextSize.SMALL.fontScale)
        assertTrue(AppTextSize.SMALL.fontScale < AppTextSize.STANDARD.fontScale)
        assertTrue(AppTextSize.STANDARD.fontScale < AppTextSize.LARGE.fontScale)
    }
}
