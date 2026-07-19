package com.rrrrz.tinyvow.data.apps

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagedAppTest {
    private val app = ManagedApp(
        packageName = "com.tencent.mm",
        appName = "微信",
        appNameZh = "微信",
        appNameEn = "WeChat",
    )

    @Test
    fun matchesSearchQuery_matchesPackageChineseAndEnglishNames() {
        assertTrue(app.matchesSearchQuery("tencent"))
        assertTrue(app.matchesSearchQuery("微信"))
        assertTrue(app.matchesSearchQuery("wechat"))
        assertFalse(app.matchesSearchQuery("telegram"))
    }
}
