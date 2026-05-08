package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private data class BadgeEffectStyle(
    val glow: Color,
    val line: Color,
)

@Composable
fun AchievementBadge(
    achievement: AchievementEntity,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    animated: Boolean = false,
) {
    AchievementBadge(
        achievementId = achievement.id,
        tier = achievement.tier,
        modifier = modifier,
        locked = locked,
        animated = animated,
    )
}

@Composable
fun AchievementBadge(
    achievementId: String,
    tier: Int,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    animated: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "badge_$achievementId")
    val twinkle by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "badge_twinkle"
    )
    val drift by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "badge_drift"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (tier) {
                    AchievementTier.BRONZE -> 16000
                    AchievementTier.SILVER -> 13000
                    AchievementTier.GOLD -> 18000
                    AchievementTier.DIAMOND -> 20000
                    AchievementTier.LEGENDARY -> 24000
                    else -> 18000
                },
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "badge_spin"
    )

    val imageRes = achievementBadgeResId(achievementId)
    val effectStyle = effectStyleForTier(tier)
    val iconAlpha = if (locked) 0.56f else 1f

    Box(
        modifier = modifier.graphicsLayer {
            alpha = iconAlpha
        }
    ) {
        if (animated) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBadgeAura(tier, effectStyle, twinkle, drift, locked)
            }
        }

        if (imageRes != null) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (locked) Modifier.blur(0.8.dp) else Modifier)
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawFallbackBadge(tier, effectStyle, locked)
            }
        }

        if (animated) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBadgeMotion(tier, effectStyle, twinkle, drift, spin, locked)
            }
        }
    }
}

fun representativeAchievementIdForTier(tier: Int): String = when (tier) {
    AchievementTier.BRONZE -> "TIER_BRONZE"
    AchievementTier.SILVER -> "TIER_SILVER"
    AchievementTier.GOLD -> "TIER_GOLD"
    AchievementTier.DIAMOND -> "TIER_DIAMOND"
    AchievementTier.LEGENDARY -> "TIER_LEGEND"
    else -> "TIER_BRONZE"
}

private fun achievementBadgeResId(achievementId: String): Int? = when (achievementId) {
    "TIER_BRONZE" -> R.drawable.achievement_tier_bronze
    "TIER_SILVER" -> R.drawable.achievement_tier_silver
    "TIER_GOLD" -> R.drawable.achievement_tier_gold
    "TIER_DIAMOND" -> R.drawable.achievement_tier_diamond
    "TIER_LEGEND" -> R.drawable.achievement_tier_legendary

    "BRONZE_POINTS" -> R.drawable.achievement_bronze_points
    "BRONZE_REDEEM" -> R.drawable.achievement_bronze_redeem_points
    "BRONZE_CTRL_DAYS" -> R.drawable.achievement_bronze_control_days
    "BRONZE_CTRL_STREAK" -> R.drawable.achievement_bronze_control_streak
    "BRONZE_ENC_DAYS" -> R.drawable.achievement_bronze_encourage_days
    "BRONZE_ENC_STREAK" -> R.drawable.achievement_bronze_encourage_streak

    "SILVER_POINTS" -> R.drawable.achievement_silver_points
    "SILVER_REDEEM" -> R.drawable.achievement_silver_redeem_points
    "SILVER_CTRL_DAYS" -> R.drawable.achievement_silver_control_days
    "SILVER_CTRL_STREAK" -> R.drawable.achievement_silver_control_streak
    "SILVER_ENC_DAYS" -> R.drawable.achievement_silver_encourage_days
    "SILVER_ENC_STREAK" -> R.drawable.achievement_silver_encourage_streak

    "GOLD_POINTS" -> R.drawable.achievement_gold_points
    "GOLD_REDEEM" -> R.drawable.achievement_gold_redeem_points
    "GOLD_CTRL_DAYS" -> R.drawable.achievement_gold_control_days
    "GOLD_CTRL_STREAK" -> R.drawable.achievement_gold_control_streak
    "GOLD_ENC_DAYS" -> R.drawable.achievement_gold_encourage_days
    "GOLD_ENC_STREAK" -> R.drawable.achievement_gold_encourage_streak

    "DIAMOND_POINTS" -> R.drawable.achievement_diamond_points
    "DIAMOND_REDEEM" -> R.drawable.achievement_diamond_redeem_points
    "DIAMOND_CTRL_DAYS" -> R.drawable.achievement_diamond_control_days
    "DIAMOND_CTRL_STREAK" -> R.drawable.achievement_diamond_control_streak
    "DIAMOND_ENC_DAYS" -> R.drawable.achievement_diamond_encourage_days
    "DIAMOND_ENC_STREAK" -> R.drawable.achievement_diamond_encourage_streak

    "LEGEND_POINTS" -> R.drawable.achievement_legendary_points
    "LEGEND_REDEEM" -> R.drawable.achievement_legendary_redeem_points
    "LEGEND_CTRL_DAYS" -> R.drawable.achievement_legendary_control_days
    "LEGEND_CTRL_STREAK" -> R.drawable.achievement_legendary_control_streak
    "LEGEND_ENC_DAYS" -> R.drawable.achievement_legendary_encourage_days
    "LEGEND_ENC_STREAK" -> R.drawable.achievement_legendary_encourage_streak
    else -> null
}

private fun effectStyleForTier(tier: Int): BadgeEffectStyle = when (tier) {
    AchievementTier.BRONZE -> BadgeEffectStyle(
        glow = Color(0x66FFD58D),
        line = Color(0xFFF6C465),
    )
    AchievementTier.SILVER -> BadgeEffectStyle(
        glow = Color(0x558DBDFF),
        line = Color(0xFFEAF3FF),
    )
    AchievementTier.GOLD -> BadgeEffectStyle(
        glow = Color(0x44FFD772),
        line = Color(0xFFFFD77B),
    )
    AchievementTier.DIAMOND -> BadgeEffectStyle(
        glow = Color(0x4474C8FF),
        line = Color(0xFFF0F8FF),
    )
    AchievementTier.LEGENDARY -> BadgeEffectStyle(
        glow = Color(0x44D78BFF),
        line = Color(0xFFFFD98D),
    )
    else -> effectStyleForTier(AchievementTier.BRONZE)
}

private fun DrawScope.drawBadgeMotion(
    tier: Int,
    style: BadgeEffectStyle,
    twinkle: Float,
    drift: Float,
    spin: Float,
    locked: Boolean,
) {
    val strength = if (locked) 0.5f else 1f
    when (tier) {
        AchievementTier.BRONZE -> {
            val stars = listOf(
                Offset(size.width * 0.18f, size.height * 0.22f),
                Offset(size.width * 0.84f, size.height * 0.26f),
                Offset(size.width * 0.72f, size.height * 0.84f),
            )
            stars.forEachIndexed { index, point ->
                val local = 0.35f + ((twinkle + index * 0.18f) % 1f) * 0.65f
                drawDiamondStar(
                    center = point,
                    radius = size.minDimension * (0.028f + local * 0.01f),
                    color = style.line.copy(alpha = strength * local)
                )
            }
        }
        AchievementTier.SILVER -> {
            val angle = ((spin * 0.9f - 90f) * PI / 180f).toFloat()
            val orbit = Offset(
                x = center().x + cos(angle) * size.minDimension * 0.23f,
                y = center().y + sin(angle) * size.minDimension * 0.23f,
            )
            drawCircle(
                color = style.glow.copy(alpha = strength * 0.22f),
                radius = size.minDimension * 0.28f,
                center = center(),
                style = Stroke(width = size.minDimension * 0.012f)
            )
            drawDiamondStar(
                center = orbit,
                radius = size.minDimension * 0.045f,
                color = style.line.copy(alpha = strength * (0.5f + twinkle * 0.35f))
            )
        }
        AchievementTier.GOLD -> {
            rotate(spin * 0.25f, pivot = center()) {
                repeat(8) { index ->
                    val angle = (index * 45f * PI / 180f).toFloat()
                    val start = Offset(
                        center().x + cos(angle) * size.minDimension * 0.14f,
                        center().y + sin(angle) * size.minDimension * 0.14f,
                    )
                    val end = Offset(
                        center().x + cos(angle) * size.minDimension * 0.42f,
                        center().y + sin(angle) * size.minDimension * 0.42f,
                    )
                    drawLine(
                        color = style.line.copy(alpha = strength * (0.12f + twinkle * 0.18f)),
                        start = start,
                        end = end,
                        strokeWidth = size.minDimension * 0.012f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            val shimmerX = size.width * drift
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, style.glow.copy(alpha = strength * 0.22f), Color.Transparent),
                    start = Offset(shimmerX - 100f, 0f),
                    end = Offset(shimmerX + 100f, size.height),
                ),
                start = Offset(shimmerX - 60f, size.height * 0.15f),
                end = Offset(shimmerX + 60f, size.height * 0.85f),
                strokeWidth = size.minDimension * 0.09f,
                cap = StrokeCap.Round,
            )
        }
        AchievementTier.DIAMOND -> {
            rotate(spin * 0.3f, pivot = center()) {
                val ringRadius = size.minDimension * 0.42f
                drawCircle(
                    color = style.line.copy(alpha = strength * 0.18f),
                    radius = ringRadius,
                    center = center(),
                    style = Stroke(width = size.minDimension * 0.014f)
                )
                drawLine(
                    color = style.line.copy(alpha = strength * 0.2f),
                    start = Offset(center().x, size.height * 0.08f),
                    end = Offset(center().x, size.height * 0.92f),
                    strokeWidth = size.minDimension * 0.01f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = style.line.copy(alpha = strength * 0.2f),
                    start = Offset(size.width * 0.08f, center().y),
                    end = Offset(size.width * 0.92f, center().y),
                    strokeWidth = size.minDimension * 0.01f,
                    cap = StrokeCap.Round,
                )
            }
            drawDiamondStar(
                center = Offset(size.width * 0.84f, size.height * 0.18f),
                radius = size.minDimension * 0.032f,
                color = style.line.copy(alpha = strength * (0.45f + twinkle * 0.25f))
            )
        }
        AchievementTier.LEGENDARY -> {
            repeat(3) { index ->
                drawCircle(
                    color = style.line.copy(alpha = strength * (0.08f + index * 0.04f + twinkle * 0.03f)),
                    radius = size.minDimension * (0.18f + index * 0.1f),
                    center = center(),
                    style = Stroke(width = size.minDimension * 0.01f),
                )
            }
            rotate(spin * 0.2f, pivot = center()) {
                repeat(12) { index ->
                    val angle = ((index * 30f) * PI / 180f).toFloat()
                    drawLine(
                        color = style.line.copy(alpha = strength * 0.16f),
                        start = Offset(
                            center().x + cos(angle) * size.minDimension * 0.12f,
                            center().y + sin(angle) * size.minDimension * 0.12f,
                        ),
                        end = Offset(
                            center().x + cos(angle) * size.minDimension * 0.44f,
                            center().y + sin(angle) * size.minDimension * 0.44f,
                        ),
                        strokeWidth = size.minDimension * 0.01f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            drawDiamondStar(
                center = Offset(size.width * 0.16f, size.height * 0.22f),
                radius = size.minDimension * 0.036f,
                color = style.line.copy(alpha = strength * (0.4f + twinkle * 0.25f))
            )
            drawDiamondStar(
                center = Offset(size.width * 0.82f, size.height * 0.78f),
                radius = size.minDimension * 0.03f,
                color = style.line.copy(alpha = strength * (0.35f + twinkle * 0.2f))
            )
        }
    }
}

private fun DrawScope.drawBadgeAura(
    tier: Int,
    style: BadgeEffectStyle,
    twinkle: Float,
    drift: Float,
    locked: Boolean,
) {
    val strength = if (locked) 0.42f else 1f
    val auraAlpha = when (tier) {
        AchievementTier.BRONZE -> 0.12f
        AchievementTier.SILVER -> 0.13f
        AchievementTier.GOLD -> 0.16f
        AchievementTier.DIAMOND -> 0.18f
        AchievementTier.LEGENDARY -> 0.2f
        else -> 0.12f
    } * strength
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                style.glow.copy(alpha = auraAlpha * (0.8f + twinkle * 0.25f)),
                style.glow.copy(alpha = auraAlpha * 0.32f),
                Color.Transparent,
            ),
            center = center(),
            radius = size.minDimension * 0.64f,
        ),
        radius = size.minDimension * 0.64f,
        center = center(),
    )
    if (tier >= AchievementTier.GOLD) {
        val shimmerX = size.width * drift
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    style.glow.copy(alpha = auraAlpha * 0.8f),
                    Color.Transparent,
                ),
                start = Offset(shimmerX - size.minDimension * 0.4f, 0f),
                end = Offset(shimmerX + size.minDimension * 0.4f, size.height),
            ),
            start = Offset(shimmerX - size.minDimension * 0.18f, size.height * 0.08f),
            end = Offset(shimmerX + size.minDimension * 0.18f, size.height * 0.92f),
            strokeWidth = size.minDimension * 0.14f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawFallbackBadge(
    tier: Int,
    style: BadgeEffectStyle,
    locked: Boolean,
) {
    val alpha = if (locked) 0.44f else 0.88f
    val radius = min(size.width, size.height) * 0.46f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(style.glow.copy(alpha = alpha), Color.Transparent),
            center = center(),
            radius = radius * 1.2f,
        ),
        radius = radius * 1.2f,
        center = center(),
    )
    drawCircle(
        color = style.line.copy(alpha = alpha),
        radius = radius * 0.78f,
        center = center(),
        style = Stroke(width = size.minDimension * 0.03f),
    )
    drawDiamondStar(center(), size.minDimension * 0.16f, style.line.copy(alpha = alpha))
}

private fun DrawScope.drawDiamondStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.46f, center.y - radius * 0.22f)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + radius * 0.46f, center.y + radius * 0.22f)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.46f, center.y + radius * 0.22f)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - radius * 0.46f, center.y - radius * 0.22f)
        close()
    }
    drawPath(path, color = color, style = Stroke(width = radius * 0.28f, cap = StrokeCap.Round))
}

private fun DrawScope.center(): Offset = Offset(size.width / 2f, size.height / 2f)
