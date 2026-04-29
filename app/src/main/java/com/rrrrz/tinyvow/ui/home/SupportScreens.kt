package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val SUPPORT_EMAIL = "rrrr.zhao@gmail.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedbackScreen(
    onBack: () -> Unit,
    onSendFeedback: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帮助与反馈") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HelpCard(
                title = "权限为什么需要手动开启？",
                body = "Tiny Vow 需要使用情况访问来统计 App 使用时长；无障碍服务只用于识别前台 App 并在超额时显示阻断页。两个权限都会在打开系统设置前先展示说明并征得同意。",
            )
            HelpCard(
                title = "订阅失败或无法购买怎么办？",
                body = "请确认应用是从 Google Play 测试轨道或正式渠道安装，并且 Play Console 已配置 tinyvow_pro 订阅商品。未配置时，本地安装包只能看到订阅入口，不能完成真实购买。",
            )
            HelpCard(
                title = "本地数据如何处理？",
                body = "应用内可以导出本地数据，也可以清除本地数据。第一版不会自动上传你的分组、使用记录、积分、主题和阻断记录。",
            )
            HelpCard(
                title = "反馈时请附带什么信息？",
                body = "建议说明手机型号、系统版本、问题出现的页面、复现步骤，以及是否开启了使用情况访问或无障碍服务。",
            )
            Button(
                onClick = onSendFeedback,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Text("发送反馈")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    onBack: () -> Unit,
    onSendEmail: () -> Unit,
    onCopyEmail: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("联系我们") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Tiny Vow 支持邮箱",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = SUPPORT_EMAIL,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "你可以通过邮件反馈问题、建议、订阅异常或账号相关需求。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onSendEmail,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Text("发送邮件")
                }
                OutlinedButton(
                    onClick = onCopyEmail,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Text("复制邮箱")
                }
            }
        }
    }
}

@Composable
private fun HelpCard(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
