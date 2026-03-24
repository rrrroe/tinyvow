package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaboratoryScreen(
    onAddPoints: (Double) -> Unit,
    onResetSummary: () -> Unit,
    onTriggerSummary: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("实验室 (调试工具)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("积分模拟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAddPoints(10.0) }, modifier = Modifier.weight(1f)) { Text("+10") }
                Button(onClick = { onAddPoints(100.0) }, modifier = Modifier.weight(1f)) { Text("+100") }
            }
            
            HorizontalDivider()
            
            Text("战报测试", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(
                onClick = onResetSummary,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("重置战报状态 (清除今日记录)")
            }
            
            Button(
                onClick = onTriggerSummary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("直接触发战报弹窗")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("提示：重置后重启 App 即可看到满足条件的昨日战报。", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
