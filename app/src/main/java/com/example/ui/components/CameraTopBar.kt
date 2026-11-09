package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.camera.core.ImageCapture
import com.example.model.CameraCaptureStats
import com.example.model.CinematicFilter
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.ui.theme.CinemaCyan4K
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaGoldAccent
import com.example.ui.theme.CinemaGreenActive
import com.example.ui.theme.CinemaOverlayDark
import com.example.ui.theme.CinemaPanelBorder
import com.example.ui.theme.CinemaRedRecord
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CinemaWhiteMuted

@Composable
fun CameraTopBar(
    modifier: Modifier = Modifier,
    resolution: VideoResolution,
    fps: VideoFps,
    activeFilter: CinematicFilter,
    flashMode: Int,
    externalMic: Boolean,
    stats: CameraCaptureStats,
    onToggleFlash: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenConsole: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CinemaOverlayDark)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left telemetry: 4K badge, FPS, Filter badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Resolution badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (resolution == VideoResolution.UHD_4K) CinemaCyan4K else CinemaSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (resolution == VideoResolution.UHD_4K) "4K UHD" else resolution.label,
                        color = if (resolution == VideoResolution.UHD_4K) CinemaDarkBg else CinemaWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // FPS badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CinemaSurface)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${fps.fps} FPS",
                        color = CinemaGreenActive,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Filter LUT indicator
                if (activeFilter != CinematicFilter.NATURAL) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CinemaGoldAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activeFilter.title.uppercase(),
                            color = CinemaGoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Center Recording status indicator
            if (stats.isRecording) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CinemaRedRecord.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(CinemaRedRecord, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val min = stats.recordingDurationSec / 60
                    val sec = stats.recordingDurationSec % 60
                    Text(
                        text = String.format("%02d:%02d", min, sec) + if (stats.isPaused) " [PAUSED]" else " REC",
                        color = CinemaRedRecord,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                // Battery & Storage
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (stats.batteryPct > 20) CinemaGreenActive else CinemaRedRecord,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${stats.batteryPct}%",
                            color = CinemaWhiteMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SdCard,
                            contentDescription = "Storage",
                            tint = CinemaWhiteMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${stats.storageFreeGb}G",
                            color = CinemaWhiteMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Right shortcuts: Flash, Flip, Console, Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Flash toggle
                IconButton(
                    onClick = onToggleFlash,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("flash_toggle_button")
                ) {
                    val icon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                        else -> Icons.Default.FlashOff
                    }
                    val tint = if (flashMode == ImageCapture.FLASH_MODE_OFF) CinemaWhiteMuted else CinemaGoldAccent
                    Icon(imageVector = icon, contentDescription = "Flash", tint = tint, modifier = Modifier.size(18.dp))
                }

                // Switch Camera
                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = CinemaWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Manual Console Drawer button
                IconButton(
                    onClick = onOpenConsole,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_console_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Manual Controls",
                        tint = CinemaGreenActive,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Camera Settings",
                        tint = CinemaWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
