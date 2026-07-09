package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.content.ContentUris
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowIconSurface
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OfflineFocusRestReminderRingtonePickerSheet(
    currentRingtoneUri: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeColors = LocalThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val loadState by produceState(
        initialValue = RestReminderRingtoneLoadState(
            options = defaultRestReminderRingtoneOptions(),
            loading = true,
        ),
        context,
    ) {
        value =
            withContext(Dispatchers.IO) {
                RestReminderRingtoneLoadState(
                    options = loadRestReminderRingtoneOptions(context),
                    loading = false,
                )
            }
    }
    val options = loadState.options
    var selectedUri by remember(currentRingtoneUri) { mutableStateOf(currentRingtoneUri) }
    var previewRingtone by remember { mutableStateOf<Ringtone?>(null) }
    var previewingKey by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { previewRingtone?.stop() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TinyVowIconSurface(
                    icon = Icons.Default.Notifications,
                    contentDescription = null,
                    containerColor = themeColors.base.copy(alpha = 0.12f),
                    contentColor = themeColors.base,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = AppText.t("offline_focus_rest_reminder_ringtone_picker_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    Text(
                        text = AppText.t("offline_focus_rest_reminder_ringtone_picker_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(TinyVowRadius.Card),
                color = themeColors.surfaceSoft,
                border = BorderStroke(1.dp, themeColors.borderSoft),
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (loadState.loading) {
                        item(key = "loading") {
                            RestReminderRingtoneLoadingRow()
                        }
                    }
                    items(options, key = { it.key }) { option ->
                        val selected = option.uriString == selectedUri
                        val previewing = option.key == previewingKey
                        RestReminderRingtoneRow(
                            option = option,
                            selected = selected,
                            previewing = previewing,
                            onSelect = { selectedUri = option.uriString },
                            onPreview = {
                                val current = previewRingtone
                                if (previewing) {
                                    runCatching { current?.stop() }
                                    previewRingtone = null
                                    previewingKey = null
                                } else {
                                    runCatching { current?.stop() }
                                    val uri = option.uriString?.let(Uri::parse) ?: defaultRestReminderRingtoneUri()
                                    previewRingtone =
                                        runCatching {
                                            RingtoneManager.getRingtone(context, uri)?.also { it.play() }
                                        }.getOrNull()
                                    previewingKey = option.key
                                }
                            },
                        )
                    }
                }
            }

            TinyVowButton(
                text = AppText.t("offline_focus_rest_reminder_ringtone_use"),
                onClick = {
                    runCatching { previewRingtone?.stop() }
                    onSelect(selectedUri)
                },
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RestReminderRingtoneLoadingRow() {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(TinyVowRadius.Control),
        color = themeColors.surfaceGlass,
        border = BorderStroke(1.dp, themeColors.borderSoft),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = themeColors.base,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = AppText.t("offline_focus_rest_reminder_ringtone_loading"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text = AppText.t("offline_focus_rest_reminder_ringtone_loading_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun RestReminderRingtoneRow(
    option: RestReminderRingtoneOption,
    selected: Boolean,
    previewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable(onClick = onSelect),
        shape = RoundedCornerShape(TinyVowRadius.Control),
        color = if (selected) themeColors.base.copy(alpha = 0.12f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, themeColors.base.copy(alpha = 0.28f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Notifications,
                contentDescription = null,
                tint = if (selected) themeColors.base else themeColors.inkMuted,
                modifier = Modifier.size(22.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) themeColors.inkStrong else themeColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (option.isDefault) {
                    Text(
                        text = AppText.t("offline_focus_rest_reminder_default_ringtone"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            TextButton(onClick = onPreview) {
                Icon(
                    imageVector = if (previewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text =
                        AppText.t(
                            if (previewing) {
                                "offline_focus_rest_reminder_ringtone_stop_preview"
                            } else {
                                "offline_focus_rest_reminder_ringtone_preview"
                            },
                        ),
                    maxLines = 1,
                )
            }
        }
    }
}

private data class RestReminderRingtoneLoadState(
    val options: List<RestReminderRingtoneOption>,
    val loading: Boolean,
)

private data class RestReminderRingtoneOption(
    val title: String,
    val uriString: String?,
    val isDefault: Boolean = false,
) {
    val key: String = uriString ?: "__default__"
}

private fun defaultRestReminderRingtoneOption(): RestReminderRingtoneOption =
    RestReminderRingtoneOption(
        title = AppText.t("offline_focus_rest_reminder_ringtone_system_default"),
        uriString = null,
        isDefault = true,
    )

private fun defaultRestReminderRingtoneOptions(): List<RestReminderRingtoneOption> =
    listOf(
        defaultRestReminderRingtoneOption(),
        RestReminderRingtoneOption(
            title = AppText.t("offline_focus_rest_reminder_ringtone_default_notification"),
            uriString = Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
            isDefault = true,
        ),
        RestReminderRingtoneOption(
            title = AppText.t("offline_focus_rest_reminder_ringtone_default_alarm"),
            uriString = Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
            isDefault = true,
        ),
    )

private fun loadRestReminderRingtoneOptions(context: Context): List<RestReminderRingtoneOption> {
    val options = defaultRestReminderRingtoneOptions().toMutableList()
    runCatching {
        val manager =
            RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_ALL)
            }
        val cursor = manager.cursor ?: return@runCatching
        cursor.use {
            while (it.moveToNext()) {
                val uriString = manager.getRingtoneUri(it.position)?.toString() ?: continue
                val title =
                    runCatching { manager.getRingtone(it.position)?.getTitle(context) }
                        .getOrNull()
                        ?.takeIf { value -> value.isNotBlank() }
                        ?: uriString
                options += RestReminderRingtoneOption(title = title, uriString = uriString)
            }
        }
    }
    appendMediaStoreRingtoneOptions(context, MediaStore.Audio.Media.INTERNAL_CONTENT_URI, options)
    appendMediaStoreRingtoneOptions(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, options)
    return options.distinctBy { it.key }
}

internal fun defaultRestReminderRingtoneUri(): Uri =
    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

private fun appendMediaStoreRingtoneOptions(
    context: Context,
    collectionUri: Uri,
    options: MutableList<RestReminderRingtoneOption>,
) {
    val projection =
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
        )
    val selection =
        "${MediaStore.Audio.Media.IS_RINGTONE} != 0 OR " +
            "${MediaStore.Audio.Media.IS_NOTIFICATION} != 0 OR " +
            "${MediaStore.Audio.Media.IS_ALARM} != 0"
    runCatching {
        context.contentResolver.query(
            collectionUri,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE LOCALIZED ASC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collectionUri, id).toString()
                val title = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: uri
                options += RestReminderRingtoneOption(title = title, uriString = uri)
            }
        }
    }
}
