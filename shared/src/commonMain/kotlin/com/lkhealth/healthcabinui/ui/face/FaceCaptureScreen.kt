package com.lkhealth.healthcabinui.ui.face

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkhealth.healthcabinui.session.AppViewModel
import com.lkhealth.healthcabinui.session.FaceCaptureMode
import com.lkhealth.healthcabinui.ui.components.CenteredContentCard
import com.lkhealth.healthcabinui.ui.components.SecondaryActionButton
import com.lkhealth.healthcabinui.ui.theme.HealthCabinColors
import com.lkhealth.healthcabinui.ui.theme.Spacing
import kotlinx.coroutines.delay

/** 人脸登录时用于演示识别成功的示例身份证号，与欢迎页"模拟刷身份证"共用同一档案。 */
private const val DEMO_LOGIN_UID = "110101199001011234"
private const val DEMO_LOGIN_NAME = "张三"

private enum class FaceStage { DETECTING, COUNTDOWN, PROCESSING, SUCCESS }

/**
 * 人脸采集/识别的共用界面，对应旧系统 CreateFaceUC（采集）与 RFaceUC（识别）——
 * 大标题 + 大片空白预览区 + 底部三条注意事项 + 单个"退出"按钮，贴近参考截图的布局。
 */
@Composable
fun FaceCaptureScreen(mode: FaceCaptureMode, uid: String?, viewModel: AppViewModel) {
    var stage by remember { mutableStateOf(FaceStage.DETECTING) }
    var countdown by remember { mutableStateOf(3) }
    var voteCount by remember { mutableStateOf(0) }

    LaunchedEffect(mode, uid) {
        stage = FaceStage.DETECTING
        delay(1100)
        if (mode == FaceCaptureMode.ENROLL) {
            stage = FaceStage.COUNTDOWN
            for (remaining in 3 downTo 1) {
                countdown = remaining
                delay(700)
            }
            stage = FaceStage.PROCESSING
            delay(600)
            stage = FaceStage.SUCCESS
            delay(900)
            viewModel.completeFaceEnrollment()
        } else {
            stage = FaceStage.PROCESSING
            for (vote in 1..3) {
                voteCount = vote
                delay(500)
            }
            stage = FaceStage.SUCCESS
            delay(700)
            viewModel.onFaceRecognized(DEMO_LOGIN_UID)
        }
    }

    CenteredContentCard(maxWidth = 900.dp) {
        Text(
            "请正视摄像头！",
            style = MaterialTheme.typography.displayMedium,
            color = HealthCabinColors.Success,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Please face the camera!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.xl))
        Box(Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
            Text(
                text = statusText(mode, stage, voteCount, countdown),
                style = MaterialTheme.typography.titleLarge,
                color = if (stage == FaceStage.SUCCESS) HealthCabinColors.Success else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxl)) {
            FaceTip(Icons.Filled.RemoveRedEye, "不能佩戴眼镜")
            FaceTip(Icons.Filled.Face, "不能遮挡面部")
            FaceTip(Icons.Filled.SlowMotionVideo, "放慢动作")
        }

        Spacer(Modifier.height(Spacing.xl))
        SecondaryActionButton(text = "退出", onClick = viewModel::goBack)
    }
}

@Composable
private fun FaceTip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun statusText(mode: FaceCaptureMode, stage: FaceStage, voteCount: Int, countdown: Int): String =
    if (mode == FaceCaptureMode.ENROLL) {
        when (stage) {
            FaceStage.DETECTING -> "检测到人脸，保持不动..."
            FaceStage.COUNTDOWN -> "${countdown}秒后自动拍照..."
            FaceStage.PROCESSING -> "正在采集..."
            FaceStage.SUCCESS -> "人脸采集成功"
        }
    } else {
        when (stage) {
            FaceStage.DETECTING -> "请正对摄像头，保持面部清晰"
            FaceStage.COUNTDOWN -> "请正对摄像头，保持面部清晰"
            FaceStage.PROCESSING -> "识别中：$DEMO_LOGIN_NAME ($voteCount/3)"
            FaceStage.SUCCESS -> "识别成功：$DEMO_LOGIN_NAME"
        }
    }
