package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 成就等级常量
 * 0=Seed, 1=Bronze, 2=Silver, 3=Gold, 4=Diamond, 5=Legendary
 */
object AchievementTier {
    const val SEED = 0
    const val BRONZE = 1
    const val SILVER = 2
    const val GOLD = 3
    const val DIAMOND = 4
    const val LEGENDARY = 5
}

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val description: String,
    
    val requirement: String, // JSON: {"type":"points"|"redemptions"|"control_days"|"control_streak"|"encourage_days"|"encourage_streak","value":10}
    
    /** 成就等级: 0=Seed, 1=Bronze, 2=Silver, 3=Gold, 4=Diamond, 5=Legendary */
    val tier: Int = AchievementTier.SEED,
    
    /** 展示用 emoji 图标 */
    @ColumnInfo(name = "icon_emoji")
    val iconEmoji: String = "⭐",
    
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false,
    
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null
)
