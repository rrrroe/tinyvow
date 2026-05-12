package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RewardIconSource
import com.rrrrz.tinyvow.data.repository.RewardIconCatalog
import java.io.File

private sealed interface RewardVisual {
    data class Preset(val key: String) : RewardVisual

    data class ImportedFile(val path: String) : RewardVisual

    data class Emoji(val value: String) : RewardVisual

    data object Fallback : RewardVisual
}

@Composable
fun RewardIcon(
    reward: RedemptionEntity,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    RewardVisualBox(
        visual = rewardVisualFor(reward),
        size = size,
        modifier = modifier,
    )
}

@Composable
fun RewardIconPreview(
    builtinKey: String? = null,
    iconSource: RewardIconSource? = null,
    iconValue: String? = null,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    RewardVisualBox(
        visual = rewardVisualFor(builtinKey = builtinKey, iconSource = iconSource, iconValue = iconValue),
        size = size,
        modifier = modifier,
    )
}

@Composable
private fun RewardVisualBox(
    visual: RewardVisual,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val presetResId =
        when (visual) {
            is RewardVisual.Preset -> context.resources.getIdentifier(visual.key, "drawable", context.packageName)
            else -> 0
        }
    when {
        visual is RewardVisual.Preset && presetResId != 0 -> {
            Image(
                painter = painterResource(id = presetResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.size(size).clip(RoundedCornerShape(12.dp)),
            )
        }
        visual is RewardVisual.ImportedFile -> {
            AsyncImage(
                model = File(visual.path),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.size(size).clip(RoundedCornerShape(12.dp)),
            )
        }
        visual is RewardVisual.Emoji -> {
            Surface(
                modifier = modifier.size(size),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = visual.value,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        else -> {
            Surface(
                modifier = modifier.size(size),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

private fun rewardVisualFor(reward: RedemptionEntity): RewardVisual =
    rewardVisualFor(
        builtinKey = reward.builtinKey,
        iconSource = reward.iconSource,
        iconValue = reward.iconValue,
    )

private fun rewardVisualFor(
    builtinKey: String?,
    iconSource: RewardIconSource?,
    iconValue: String?,
): RewardVisual {
    RewardIconCatalog.builtinPresetKeyFor(builtinKey)?.let { return RewardVisual.Preset(it) }
    val value = iconValue?.trim().orEmpty()
    return when {
        iconSource == RewardIconSource.PRESET && RewardIconCatalog.isValidCustomPresetKey(value) -> RewardVisual.Preset(value)
        iconSource == RewardIconSource.IMPORTED_FILE && value.isNotBlank() -> RewardVisual.ImportedFile(value)
        iconSource == RewardIconSource.EMOJI && value.isNotBlank() -> RewardVisual.Emoji(value)
        else -> RewardVisual.Fallback
    }
}
