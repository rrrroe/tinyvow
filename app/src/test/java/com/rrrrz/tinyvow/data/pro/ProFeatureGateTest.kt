package com.rrrrz.tinyvow.data.pro

import com.rrrrz.tinyvow.data.db.GroupType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProFeatureGateTest {
    @Test
    fun freeLimitsControlAndEncourageGroupsToTwoEach() {
        assertTrue(ProFeatureGate.canAddGroup(false, GroupType.CONTROL, currentCount = 1))
        assertFalse(ProFeatureGate.canAddGroup(false, GroupType.CONTROL, currentCount = 2))
        assertTrue(ProFeatureGate.canAddGroup(false, GroupType.ENCOURAGE, currentCount = 1))
        assertFalse(ProFeatureGate.canAddGroup(false, GroupType.ENCOURAGE, currentCount = 2))
        assertTrue(ProFeatureGate.canAddGroup(true, GroupType.CONTROL, currentCount = 100))
    }

    @Test
    fun limitsAppsPerGroupByEntitlement() {
        assertTrue(ProFeatureGate.canSaveGroupApps(false, appCount = 3))
        assertFalse(ProFeatureGate.canSaveGroupApps(false, appCount = 4))
        assertTrue(ProFeatureGate.canSaveGroupApps(true, appCount = 10))
        assertFalse(ProFeatureGate.canSaveGroupApps(true, appCount = 11))
    }

    @Test
    fun customRewardsUseFreeFirstThreeAndProUnlimited() {
        assertTrue(ProFeatureGate.canAddCustomReward(false, currentCustomRewardCount = 2))
        assertFalse(ProFeatureGate.canAddCustomReward(false, currentCustomRewardCount = 3))
        assertTrue(ProFeatureGate.canEditCustomReward(false, customRewardIndex = 2))
        assertFalse(ProFeatureGate.canEditCustomReward(false, customRewardIndex = 3))
        assertTrue(ProFeatureGate.canAddCustomReward(true, currentCustomRewardCount = 1000))
    }

    @Test
    fun customThemesUseFreeFirstOneAndProTen() {
        assertTrue(ProFeatureGate.canAddCustomTheme(false, currentCustomThemeCount = 0))
        assertFalse(ProFeatureGate.canAddCustomTheme(false, currentCustomThemeCount = 1))
        assertTrue(ProFeatureGate.canEditCustomTheme(false, customThemeIndex = 0))
        assertFalse(ProFeatureGate.canEditCustomTheme(false, customThemeIndex = 1))
        assertTrue(ProFeatureGate.canAddCustomTheme(true, currentCustomThemeCount = 9))
        assertFalse(ProFeatureGate.canAddCustomTheme(true, currentCustomThemeCount = 10))
    }

    @Test
    fun memberThemesAndAdvancedReportsRequirePro() {
        assertTrue(ProFeatureGate.canSelectTheme(false, "preset_sakura_mint"))
        assertFalse(ProFeatureGate.canSelectTheme(false, "member_aurora_pro"))
        assertTrue(ProFeatureGate.canSelectTheme(true, "member_aurora_pro"))
        assertFalse(ProFeatureGate.canViewAdvancedReports(false))
        assertTrue(ProFeatureGate.canViewAdvancedReports(true))
        assertFalse(ProFeatureGate.canUseLockScreenTimerApps(false))
        assertTrue(ProFeatureGate.canUseLockScreenTimerApps(true))
    }

    @Test
    fun freeUsersCanOnlyEditItemsInsideFreeQuota() {
        assertTrue(ProFeatureGate.canEditGroup(false, groupIndexInType = 1))
        assertFalse(ProFeatureGate.canEditGroup(false, groupIndexInType = 2))
        assertTrue(ProFeatureGate.canEditGroup(true, groupIndexInType = 100))
    }
}
