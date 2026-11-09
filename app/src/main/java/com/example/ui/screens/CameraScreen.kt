package com.example.ui.screens

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraController
import com.example.model.CinematicFilter
import com.example.model.FilterTuning
import com.example.ui.components.AudioLevelMeter
import com.example.ui.components.CameraBottomControls
import com.example.ui.components.CameraOverlays
import com.example.ui.components.CameraTopBar
import com.example.ui.components.CinematicFilterRow
import com.example.ui.components.ManualControlsDrawer
import com.example.ui.components.RealtimeHistogram
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaGreenActive
import com.example.ui.theme.CinemaOverlayDark
import com.example.ui.theme.CinemaPanelBorder
import com.example.ui.theme.CinemaPureBlack
import com.example.ui.theme.CinemaRedRecord
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaWhite
import kotlinx.coroutines.delay

@androidx.camera.camera2.interop.ExperimentalCamera2Interop
@Composable
fun CameraScreen(
    cameraController: CameraController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraMode by cameraController.cameraMode.collectAsState()
    val videoResolution by cameraController.videoResolution.collectAsState()
    val videoFps by cameraController.videoFps.collectAsState()
    val videoCodec by cameraController.videoCodec.collectAsState()
    val videoBitrate by cameraController.videoBitrate.collectAsState()

    val isAutoIso by cameraController.isAutoIso.collectAsState()
    val manualIso by cameraController.manualIso.collectAsState()
    val shutterSpeed by cameraController.shutterSpeed.collectAsState()
    val isAutoFocus by cameraController.isAutoFocus.collectAsState()
    val manualFocusDistance by cameraController.manualFocusDistance.collectAsState()
    val whiteBalance by cameraController.whiteBalance.collectAsState()
    val customKelvin by cameraController.customKelvin.collectAsState()
    val exposureCompensation by cameraController.exposureCompensation.collectAsState()
    val isAeLocked by cameraController.isAeLocked.collectAsState()
    val isAfLocked by cameraController.isAfLocked.collectAsState()
    val zoomRatio by cameraController.zoomRatio.collectAsState()
    val flashMode by cameraController.flashMode.collectAsState()
    val videoStabilization by cameraController.videoStabilization.collectAsState()

    val activeFilter by cameraController.activeFilter.collectAsState()
    val filterTuning by cameraController.filterTuning.collectAsState()

    val showGrid by cameraController.showGrid.collectAsState()
    val showCrosshair by cameraController.showCrosshair.collectAsState()
    val showHorizon by cameraController.showHorizon.collectAsState()
    val showHistogram by cameraController.showHistogram.collectAsState()
    val showZebra by cameraController.showZebra.collectAsState()
    val showFocusPeaking by cameraController.showFocusPeaking.collectAsState()
    val externalMic by cameraController.externalMic.collectAsState()

    val hardwareLimits by cameraController.hardwareLimits.collectAsState()
    val captureStats by cameraController.captureStats.collectAsState()
    val lastMessage by cameraController.lastCapturedMessage.collectAsState()

    var showControlsDrawer by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var tapFocusPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    // Tap point timeout
    LaunchedEffect(tapFocusPoint) {
        if (tapFocusPoint != null) {
            delay(2500)
            tapFocusPoint = null
        }
    }

    // Auto clear message after 4s
    LaunchedEffect(lastMessage) {
        if (lastMessage != null) {
            delay(4000)
            cameraController.clearMessage()
        }
    }

    BackHandler(enabled = showControlsDrawer || showSettingsScreen) {
        if (showSettingsScreen) {
            showSettingsScreen = false
        } else if (showControlsDrawer) {
            showControlsDrawer = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaPureBlack)
    ) {
        // Full screen Live Camera Preview with Pinch-to-zoom and Tap-to-focus
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newZoom = (zoomRatio * zoom).coerceIn(hardwareLimits.minZoomRatio, hardwareLimits.maxZoomRatio)
                        cameraController.setZoom(newZoom)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val xPct = offset.x / size.width
                        val yPct = offset.y / size.height
                        cameraController.tapToFocus(xPct, yPct)
                        tapFocusPoint = Pair(offset.x, offset.y)
                    }
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        cameraController.startCamera(lifecycleOwner, surfaceProvider)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Cinematic Filter Grade Matrix Overlay
            CinematicFilterGradeOverlay(
                filter = activeFilter,
                tuning = filterTuning
            )

            // Dynamic camera tools: Rule of Thirds Grid, Crosshair, Horizon Level, Focus Peaking, Zebra
            CameraOverlays(
                modifier = Modifier.fillMaxSize(),
                showGrid = showGrid,
                showCrosshair = showCrosshair,
                showHorizon = showHorizon,
                showZebra = showZebra,
                showFocusPeaking = showFocusPeaking,
                stats = captureStats
            )

            // Tap Focus Target Indicator Box
            tapFocusPoint?.let { (x, y) ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopStart)
                        .padding(top = (y - 30).coerceAtLeast(0f).dp, start = (x - 30).coerceAtLeast(0f).dp)
                        .background(Color.Transparent, RoundedCornerShape(4.dp))
                        .border(1.5.dp, CinemaGreenActive, RoundedCornerShape(4.dp))
                )
            }
        }

        // Top Control Bar
        CameraTopBar(
            modifier = Modifier.align(Alignment.TopCenter),
            resolution = videoResolution,
            fps = videoFps,
            activeFilter = activeFilter,
            flashMode = flashMode,
            externalMic = externalMic,
            stats = captureStats,
            onToggleFlash = { cameraController.toggleFlash() },
            onSwitchCamera = { cameraController.switchCamera() },
            onOpenSettings = { showSettingsScreen = true },
            onOpenConsole = { showControlsDrawer = !showControlsDrawer }
        )

        // Real-time RGB Histogram and Audio Level Meter overlay in preview corner
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 60.dp, start = 12.dp)
        ) {
            if (showHistogram) {
                RealtimeHistogram(
                    data = captureStats.histogramData,
                    modifier = Modifier
                        .width(110.dp)
                        .height(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            AudioLevelMeter(
                db = captureStats.audioLevelDb,
                modifier = Modifier
                    .width(110.dp)
                    .height(8.dp)
            )
        }

        // Bottom Section: Cinematic LUT Filter Row & Cinema Controls
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            CinematicFilterRow(
                activeFilter = activeFilter,
                onSelectFilter = { cameraController.setCinematicFilter(it) }
            )

            CameraBottomControls(
                cameraMode = cameraMode,
                onSelectMode = { cameraController.setCameraMode(it) },
                stats = captureStats,
                onCapturePhoto = { cameraController.takePhoto() },
                onToggleRecordVideo = { cameraController.toggleVideoRecording() },
                onPauseResumeVideo = { cameraController.pauseResumeVideoRecording() },
                onOpenGallery = {
                    val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_GALLERY)
                        }
                        try {
                            context.startActivity(fallback)
                        } catch (ex: Exception) {
                            // ignore if no gallery
                        }
                    }
                },
                zoomRatio = zoomRatio,
                onZoomChange = { cameraController.setZoom(it) },
                maxZoom = hardwareLimits.maxZoomRatio,
                minZoom = hardwareLimits.minZoomRatio
            )
        }

        // Notification / Saved banner
        lastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CinemaOverlayDark)
                    .border(1.dp, CinemaGreenActive, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CinemaGreenActive,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        color = CinemaWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Slide-up DSLR Pro Manual Controls Console
        AnimatedVisibility(
            visible = showControlsDrawer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ManualControlsDrawer(
                onDismiss = { showControlsDrawer = false },
                videoResolution = videoResolution,
                onSelectResolution = { cameraController.setVideoResolution(it) },
                videoFps = videoFps,
                onSelectFps = { cameraController.setVideoFps(it) },
                videoBitrate = videoBitrate,
                onSelectBitrate = { cameraController.setVideoBitrate(it) },
                videoCodec = videoCodec,
                onSelectCodec = { cameraController.setVideoCodec(it) },
                isAutoIso = isAutoIso,
                manualIso = manualIso,
                onSetAutoIso = { cameraController.setAutoIso(it) },
                onSetManualIso = { cameraController.setManualIso(it) },
                shutterSpeed = shutterSpeed,
                onSelectShutter = { cameraController.setShutterSpeed(it) },
                isAutoFocus = isAutoFocus,
                manualFocusDistance = manualFocusDistance,
                onSetAutoFocus = { cameraController.setAutoFocus(it) },
                onSetManualFocus = { cameraController.setManualFocusDistance(it) },
                whiteBalance = whiteBalance,
                customKelvin = customKelvin,
                onSelectWb = { cameraController.setWhiteBalance(it) },
                onSetCustomKelvin = { cameraController.setCustomKelvin(it) },
                exposureCompensation = exposureCompensation,
                onSetExposureCompensation = { cameraController.setExposureCompensation(it) },
                isAeLocked = isAeLocked,
                onToggleAeLock = { cameraController.toggleAeLock() },
                isAfLocked = isAfLocked,
                onToggleAfLock = { cameraController.toggleAfLock() },
                activeFilter = activeFilter,
                onSelectFilter = { cameraController.setCinematicFilter(it) },
                filterTuning = filterTuning,
                onUpdateFilterTuning = { cameraController.setFilterTuning(it) },
                showGrid = showGrid,
                onToggleGrid = { cameraController.toggleGrid() },
                showCrosshair = showCrosshair,
                onToggleCrosshair = { cameraController.toggleCrosshair() },
                showHorizon = showHorizon,
                onToggleHorizon = { cameraController.toggleHorizon() },
                showHistogram = showHistogram,
                onToggleHistogram = { cameraController.toggleHistogram() },
                showZebra = showZebra,
                onToggleZebra = { cameraController.toggleZebra() },
                showFocusPeaking = showFocusPeaking,
                onToggleFocusPeaking = { cameraController.toggleFocusPeaking() },
                videoStabilization = videoStabilization,
                onToggleStabilization = { cameraController.toggleStabilization() },
                externalMic = externalMic,
                onToggleExternalMic = { cameraController.toggleExternalMic() },
                onResetDefaults = { cameraController.resetDefaults() },
                hardwareLimits = hardwareLimits
            )
        }

        // Settings Screen
        AnimatedVisibility(
            visible = showSettingsScreen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SettingsDialog(
                modifier = Modifier.fillMaxSize(),
                onDismiss = { showSettingsScreen = false },
                stats = captureStats,
                limits = hardwareLimits,
                onResetDefaults = { cameraController.resetDefaults() }
            )
        }
    }
}

@Composable
private fun CinematicFilterGradeOverlay(
    filter: CinematicFilter,
    tuning: FilterTuning
) {
    if (filter == CinematicFilter.NATURAL && tuning.contrast == 1.0f && tuning.saturation == 1.0f && tuning.temperature == 0f && tuning.tint == 0f) {
        return
    }

    // Build 4x5 ColorMatrix for real-time LUT film grading
    val matrix = remember(filter, tuning) {
        buildCinematicColorMatrix(filter, tuning)
    }

    // Draw the LUT color grading layer across the CameraX preview stream
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = Color.Black,
            colorFilter = ColorFilter.colorMatrix(matrix),
            blendMode = androidx.compose.ui.graphics.BlendMode.Color
        )
    }

    // Secondary subtle tone curve mapping for shadows and highlights
    val tintOverlay = when (filter) {
        CinematicFilter.TEAL_AND_ORANGE -> Color(0x1E00E5FF)
        CinematicFilter.HOLLYWOOD_FILM -> Color(0x1EFFB300)
        CinematicFilter.WARM_FILM -> Color(0x22FF9100)
        CinematicFilter.COOL_FILM -> Color(0x222979FF)
        CinematicFilter.VINTAGE -> Color(0x25C7903D)
        CinematicFilter.BLACK_AND_WHITE -> Color(0x35000000)
        CinematicFilter.DOCUMENTARY -> Color(0x18A7B0BA)
        CinematicFilter.MOODY -> Color(0x251A237E)
        CinematicFilter.PORTRAIT -> Color(0x18FF80AB)
        CinematicFilter.NATURAL -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tintOverlay)
    )
}

private fun buildCinematicColorMatrix(
    filter: CinematicFilter,
    tuning: FilterTuning
): ColorMatrix {
    val cm = ColorMatrix()

    // 1. Base Contrast & Saturation
    val c = filter.contrast * tuning.contrast
    val s = filter.saturation * tuning.saturation
    val tR = filter.tempTintR + tuning.temperature * 0.2f
    val tG = filter.tempTintG - tuning.tint * 0.1f
    val tB = filter.tempTintB - tuning.temperature * 0.2f + tuning.tint * 0.1f
    val brightnessShift = (filter.brightness + tuning.exposure * 0.1f) * 255f

    // Standard Luminance weights
    val lr = 0.2126f
    val lg = 0.7152f
    val lb = 0.0722f

    val invSat = 1.0f - s
    val rR = (invSat * lr + s) * c * tR
    val rG = (invSat * lg) * c
    val rB = (invSat * lb) * c

    val gR = (invSat * lr) * c
    val gG = (invSat * lg + s) * c * tG
    val gB = (invSat * lb) * c

    val bR = (invSat * lr) * c
    val bG = (invSat * lg) * c
    val bB = (invSat * lb + s) * c * tB

    val offset = (1.0f - c) * 128f + brightnessShift

    val values = floatArrayOf(
        rR, rG, rB, 0f, offset,
        gR, gG, gB, 0f, offset,
        bR, bG, bB, 0f, offset,
        0f, 0f, 0f, 1f, 0f
    )

    return ColorMatrix(values)
}
