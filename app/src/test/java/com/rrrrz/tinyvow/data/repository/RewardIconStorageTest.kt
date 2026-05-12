package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RewardIconSource
import com.rrrrz.tinyvow.data.db.RewardType
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardIconStorageTest {
    @Test
    fun clearAllRemovesImportedIconDirectory() {
        val rootDir = Files.createTempDirectory("reward-icons-").toFile()
        val storage = RewardIconStorage(rootDir)
        File(rootDir, "icon.png").writeText("demo")

        storage.clearAll()

        assertFalse(rootDir.exists())
    }

    @Test
    fun cleanupDeletesUnreferencedImportedIcon() {
        val shouldDelete =
            shouldDeleteImportedRewardIcon(
                path = "E:/icons/a.png",
                activeRewards = listOf(importedReward(id = "reward-a", path = "E:/icons/a.png")),
                excludeRewardId = "reward-a",
            )

        assertTrue(shouldDelete)
    }

    @Test
    fun cleanupKeepsImportedIconStillUsedByAnotherReward() {
        val shouldDelete =
            shouldDeleteImportedRewardIcon(
                path = "E:/icons/a.png",
                activeRewards =
                    listOf(
                        importedReward(id = "reward-a", path = "E:/icons/a.png"),
                        importedReward(id = "reward-b", path = "E:/icons/a.png"),
                    ),
                excludeRewardId = "reward-a",
            )

        assertFalse(shouldDelete)
    }

    private fun importedReward(
        id: String,
        path: String,
    ): RedemptionEntity =
        RedemptionEntity(
            id = id,
            title = "Tea break",
            description = "",
            pointCost = 100,
            rewardType = RewardType.CUSTOM,
            iconSource = RewardIconSource.IMPORTED_FILE,
            iconValue = path,
            createdAt = 1L,
            updatedAt = 1L,
        )
}
