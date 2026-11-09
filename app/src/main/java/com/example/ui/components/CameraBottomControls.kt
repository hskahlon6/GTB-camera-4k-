package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraCaptureStats
import com.example.model.CameraMode
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaGoldAccent
import com.example.ui.theme.CinemaGreenActive
import com.example.ui.theme.CinemaOverlayDark
import com.example.ui.theme.CinemaPanelBorder
import com.example.ui.theme.CinemaRedGlow
import com.example.ui.theme.CinemaRedRecord
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceLight
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CinemaWhiteMuted

@Composable
fun CameraBottomControls(
    modifier: Modifier = Modifier,
    cameraMode: CameraMode,
    onSelectMode: (CameraMode) -> Unit,
    stats: CameraCaptureStats,
    onCapturePhoto: () -> Unit,
    onToggleRecordVideo: () -> Unit,
    onPauseResumeVideo: () -> Unit,
    onOpenGallery: () -> Unit,
    zoomRatio: Float,
    onZoomChange: (Float) -> Unit,
    maxZoom: Float,
    minZoom: Float
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CinemaOverlayDark)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Zoom Preset Buttons (0.6x / 1x / 2x / 5x)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CinemaSurface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val zoomPresets = listOf(1.0f, 2.0f, 5.0f, 10.0f)
            zoomPresets.forEach { preset ->
                val isSelected = (zoomRatio - preset) in -0.1f..0.1f
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) CinemaGoldAccent else Color.Transparent)
                        .clickable { onZoomChange(preset) }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${preset.toInt()}×",
                        color = if (isSelected) CinemaDarkBg else CinemaWhiteMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mode Switcher (PHOTO / VIDEO)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CinemaDarkBg)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (cameraMode == CameraMode.VIDEO) CinemaSurfaceLight else Color.Transparent)
                    .clickable { if (!stats.isRecording) onSelectMode(CameraMode.VIDEO) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("mode_video_button")
            ) {
                Text(
                    text = "VIDEO 4K",
                    color = if (cameraMode == CameraMode.VIDEO) CinemaGreenActive else CinemaWhiteMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (cameraMode == CameraMode.PHOTO) CinemaSurfaceLight else Color.Transparent)
                    .clickable { if (!stats.isRecording) onSelectMode(CameraMode.PHOTO) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("mode_photo_button")
            ) {
                Text(
                    text = "PHOTO PRO",
                    color = if (cameraMode == CameraMode.PHOTO) CinemaGreenActive else CinemaWhiteMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Triggers Row (Gallery Shortcut | Big Shutter / Rec Button | Pause/Aux)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery Shortcut
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CinemaSurface)
                    .border(1.5.dp, CinemaPanelBorder, CircleShape)
                    .clickable { onOpenGallery() }
                    .testTag("gallery_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = "Gallery",
                    tint = CinemaWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Central Shutter Button
            if (cameraMode == CameraMode.VIDEO) {
                // Video Record Trigger with Cinema Ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (stats.isRecording) CinemaRedGlow else Color.Transparent)
                        .border(3.dp, CinemaWhite, CircleShape)
                        .padding(6.dp)
                        .clickable { onToggleRecordVideo() }
                        .testTag("record_video_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (stats.isRecording) {
                        // Square stop icon inside
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CinemaRedRecord)
                        )
                    } else {
                        // Large round red cinema record button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(CircleShape)
                                .background(CinemaRedRecord)
                        )
                    }
                }
            } else {
                // Photo Shutter Button (Classic DSLR white & silver ring)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, CinemaWhite, CircleShape)
                        .padding(6.dp)
                        .clickable { onCapturePhoto() }
                        .testTag("capture_photo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(CircleShape)
                            .background(CinemaWhite)
                    )
                }
            }

            // Pause/Resume or Secondary Trigger
            if (stats.isRecording) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CinemaSurface)
                        .border(1.5.dp, CinemaPanelBorder, CircleShape)
                        .clickable { onPauseResumeVideo() }
                        .testTag("pause_resume_video_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (stats.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause / Resume Recording",
                        tint = CinemaGoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                // Audio dB mini meter / Status shortcut
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CinemaSurface)
                        .border(1.5.dp, CinemaPanelBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MIC",
                            color = CinemaGreenActive,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stats.audioLevelDb.toInt()}dB",
                            color = CinemaWhiteMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
