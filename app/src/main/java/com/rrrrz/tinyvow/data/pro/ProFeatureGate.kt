package com.rrrrz.tinyvow.data.pro

import com.rrrrz.tinyvow.data.db.GroupType

data class ProLimits(
    val controlGroupLimit: Int,
    val encourageGroupLimit: Int,
    val appsPerGroupLimit: Int,
    val customRewardLimit: Int,
    val customThemeLimit: Int,
) {
    companion object {
        val Free = ProLimits(
            controlGroupLimit = 2,
            encourageGroupLimit = 2,
            appsPerGroupLimit = 3,
            customRewardLimit = 3,
            customThemeLimit = 1,
        )

        val Pro = ProLimits(
            controlGroupLimit = Int.MAX_VALUE,
            encourageGroupLimit = Int.MAX_VALUE,
            appsPerGroupLimit = 10,
            customRewardLimit = Int.MAX_VALUE,
            customThemeLimit = 10,
        )
    }
}

object ProFeatureGate {
    const val MEMBER_THEME_ID_PREFIX = "member_"

    fun limits(isProActive: Boolean): ProLimits =
        if (isProActive) ProLimits.Pro else ProLimits.Free

    fun canAddGroup(isProActive: Boolean, type: GroupType, currentCount: Int): Boolean {
        val limit = groupLimit(isProActive, type)
        return currentCount < limit
    }

    fun canEditGroup(isProActive: Boolean, groupIndexInType: Int): Boolean =
        isProActive || groupIndexInType in 0 until ProLimits.Free.controlGroupLimit

    fun canSaveGroupApps(isProActive: Boolean, appCount: Int): Boolean =
        appCount <= limits(isProActive).appsPerGroupLimit

    fun canAddCustomReward(isProActive: Boolean, currentCustomRewardCount: Int): Boolean =
        currentCustomRewardCount < limits(isProActive).customRewardLimit

    fun canEditCustomReward(isProActive: Boolean, customRewardIndex: Int): Boolean =
        isProActive || customRewardIndex in 0 until ProLimits.Free.customRewardLimit

    fun canAddCustomTheme(isProActive: Boolean, currentCustomThemeCount: Int): Boolean =
        currentCustomThemeCount < limits(isProActive).customThemeLimit

    fun canEditCustomTheme(isProActive: Boolean, customThemeIndex: Int): Boolean =
        isProActive || customThemeIndex in 0 until ProLimits.Free.customThemeLimit

    fun canSelectTheme(isProActive: Boolean, themeId: String): Boolean =
        isProActive || !isMemberTheme(themeId)

    fun canViewAdvancedReports(isProActive: Boolean): Boolean = isProActive

    fun canUseLockScreenTimerApps(isProActive: Boolean): Boolean = isProActive

    fun isMemberTheme(themeId: String): Boolean =
        themeId.startsWith(MEMBER_THEME_ID_PREFIX)

    fun groupLimit(isProActive: Boolean, type: GroupType): Int =
        when (type) {
            GroupType.CONTROL -> limits(isProActive).controlGroupLimit
            GroupType.ENCOURAGE -> limits(isProActive).encourageGroupLimit
        }
}
