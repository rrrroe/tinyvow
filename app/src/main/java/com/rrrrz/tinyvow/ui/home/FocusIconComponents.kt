package com.rrrrz.tinyvow.ui.home

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import java.io.File

internal val FocusPresetIconKeys =
    listOf(
        "focus_icon_words",
        "focus_icon_class",
        "focus_icon_piano",
        "focus_icon_guitar",
        "focus_icon_diary",
        "focus_icon_organize",
        "focus_icon_housework",
        "focus_icon_water",
        "focus_icon_nap",
        "focus_icon_morning",
        "focus_icon_strength",
        "focus_icon_stretch",
        "focus_icon_meditate",
        "focus_icon_paint",
        "focus_icon_yoga_pose",
        "focus_icon_note",
        "focus_icon_code",
        "focus_icon_graduation",
        "focus_icon_running",
        "focus_icon_read_book",
        "focus_icon_nature",
        "focus_icon_calendar",
        "focus_icon_breathing",
        "focus_icon_checklist",
        "focus_icon_journal_alt",
        "focus_icon_lamp",
        "focus_icon_pomodoro",
        "focus_icon_target",
        "focus_icon_rest",
        "focus_icon_mountain",
    )

internal fun focusPresetIconResource(iconKey: String): Int? =
    when (iconKey) {
        "focus_icon_words", "focus_icon_reading", "reading" -> R.drawable.focus_icon_words
        "focus_icon_class", "focus_icon_study", "learning" -> R.drawable.focus_icon_class
        "focus_icon_piano", "focus_icon_music", "focus_icon_instrument" -> R.drawable.focus_icon_piano
        "focus_icon_guitar", "focus_icon_create" -> R.drawable.focus_icon_guitar
        "focus_icon_diary", "focus_icon_writing" -> R.drawable.focus_icon_diary
        "focus_icon_organize", "focus_icon_planning" -> R.drawable.focus_icon_organize
        "focus_icon_housework", "focus_icon_chores" -> R.drawable.focus_icon_housework
        "focus_icon_water", "focus_icon_cooking" -> R.drawable.focus_icon_water
        "focus_icon_nap", "focus_icon_meditation", "focus_icon_yoga" -> R.drawable.focus_icon_nap
        "focus_icon_morning", "focus_icon_fitness", "exercise", "focus_icon_walking" -> R.drawable.focus_icon_morning
        "focus_icon_strength" -> R.drawable.focus_icon_strength
        "focus_icon_stretch" -> R.drawable.focus_icon_stretch
        "focus_icon_meditate" -> R.drawable.focus_icon_meditate
        "focus_icon_paint", "focus_icon_drawing" -> R.drawable.focus_icon_paint
        "focus_icon_yoga_pose" -> R.drawable.focus_icon_yoga_pose
        "focus_icon_note" -> R.drawable.focus_icon_note
        "focus_icon_code", "focus_icon_coding" -> R.drawable.focus_icon_code
        "focus_icon_graduation" -> R.drawable.focus_icon_graduation
        "focus_icon_running" -> R.drawable.focus_icon_running
        "focus_icon_read_book" -> R.drawable.focus_icon_read_book
        "focus_icon_nature", "focus_icon_gardening" -> R.drawable.focus_icon_nature
        "focus_icon_calendar" -> R.drawable.focus_icon_calendar
        "focus_icon_breathing" -> R.drawable.focus_icon_breathing
        "focus_icon_checklist" -> R.drawable.focus_icon_checklist
        "focus_icon_journal_alt" -> R.drawable.focus_icon_journal_alt
        "focus_icon_lamp" -> R.drawable.focus_icon_lamp
        "focus_icon_pomodoro" -> R.drawable.focus_icon_pomodoro
        "focus_icon_target" -> R.drawable.focus_icon_target
        "focus_icon_rest" -> R.drawable.focus_icon_rest
        "focus_icon_mountain" -> R.drawable.focus_icon_mountain
        else -> null
    }

internal fun focusPresetIconVector(iconKey: String): ImageVector? =
    when (iconKey) {
        "reading", "focus_icon_reading" -> Icons.AutoMirrored.Filled.MenuBook
        "exercise", "focus_icon_fitness" -> Icons.Default.FitnessCenter
        "learning", "focus_icon_study" -> Icons.Default.School
        "writing", "focus_icon_writing" -> Icons.Default.Edit
        "meditation", "focus_icon_meditation" -> Icons.Default.SelfImprovement
        "creation", "focus_icon_create" -> Icons.Default.AutoAwesome
        "focus_icon_coding" -> Icons.Default.Code
        "focus_icon_music" -> Icons.Default.MusicNote
        "focus_icon_language" -> Icons.Default.Translate
        "focus_icon_cooking" -> Icons.Default.Restaurant
        "focus_icon_chores" -> Icons.Default.CleaningServices
        "focus_icon_walking" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "focus_icon_yoga" -> Icons.Default.Spa
        "focus_icon_planning" -> Icons.AutoMirrored.Filled.EventNote
        "focus_icon_drawing" -> Icons.Default.Palette
        "focus_icon_gardening" -> Icons.Default.LocalFlorist
        "focus_icon_handcraft" -> Icons.Default.Build
        "focus_icon_instrument" -> Icons.Default.Piano
        "focus_icon_companion" -> Icons.Default.Favorite
        "focus_icon_photo" -> Icons.Default.PhotoCamera
        else -> null
    }

@Composable
internal fun FocusTypeIcon(
    iconKey: String,
    customIconPath: String?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val customBitmap =
        remember(customIconPath) {
            customIconPath
                ?.takeIf { it.isNotBlank() }
                ?.let { path -> runCatching { BitmapFactory.decodeFile(File(path).absolutePath) }.getOrNull() }
        }
    val presetIcon = focusPresetIconVector(iconKey)
    val presetIconResource = focusPresetIconResource(iconKey)
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (presetIconResource == null) color else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        when {
            customBitmap != null -> {
                Image(
                    bitmap = customBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                )
            }
            presetIconResource != null -> {
                Image(
                    painter = painterResource(id = presetIconResource),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            presetIcon != null -> {
                Icon(
                    imageVector = presetIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.56f),
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize(0.54f),
                )
            }
        }
    }
}

@Composable
internal fun FocusHomeIcon(modifier: Modifier = Modifier) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = themeColors.base.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.20f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = themeColors.base,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
