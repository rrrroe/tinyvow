package com.rrrrz.tinyvow.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.RoundedCornerShape
import com.rrrrz.tinyvow.data.db.AchievementEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    achievements: List<AchievementEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的成就") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(achievements) { achievement ->
                AchievementBadge(achievement)
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: AchievementEntity) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    val grayscaleMatrix = remember {
        ColorMatrix().apply { setToSaturation(0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
            .graphicsLayer {
                if (!achievement.isUnlocked) {
                    alpha = 0.8f
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .then(
                    if (achievement.isUnlocked) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primaryContainer
                                ),
                                start = androidx.compose.ui.geometry.Offset(shimmerOffset, shimmerOffset),
                                end = androidx.compose.ui.geometry.Offset(shimmerOffset + 100f, shimmerOffset + 100f)
                            ),
                            CircleShape
                        )
                    } else {
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
                            .blur(4.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (achievement.isUnlocked) Color(0xFFFFD700) 
                       else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            achievement.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface 
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Text(
            achievement.description,
            style = MaterialTheme.typography.labelSmall,
            color = if (achievement.isUnlocked) MaterialTheme.colorScheme.outline 
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
