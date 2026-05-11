package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.StreakShieldTarget
import org.json.JSONObject

internal fun RewardPayload.toJson(): String =
    JSONObject().apply {
        if (minutes > 0) put("minutes", minutes)
        if (pointsMultiplier > 1.0) put("pointsMultiplier", pointsMultiplier)
        shieldTarget?.let { put("shieldTarget", it.name) }
    }.toString()

internal fun parseRewardPayload(payloadJson: String?): RewardPayload {
    if (payloadJson.isNullOrBlank()) return RewardPayload()
    return runCatching {
        val json = JSONObject(payloadJson)
        RewardPayload(
            minutes = json.optInt("minutes", 0),
            pointsMultiplier =
                when {
                    json.has("pointsMultiplier") -> json.optDouble("pointsMultiplier", 1.0)
                    json.optInt("bonusPoints", 0) > 0 -> 2.0
                    else -> 1.0
                },
            shieldTarget =
                json.optString("shieldTarget")
                    .takeIf { it.isNotBlank() }
                    ?.let(StreakShieldTarget::valueOf),
        )
    }.getOrDefault(RewardPayload())
}
