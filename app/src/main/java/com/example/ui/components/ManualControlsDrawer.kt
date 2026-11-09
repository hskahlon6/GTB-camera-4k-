package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraHardwareLimits
import com.example.model.CinematicFilter
import com.example.model.FilterTuning
import com.example.model.ShutterSpeed
import com.example.model.VideoBitrate
import com.example.model.VideoCodec
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.model.WhiteBalancePreset
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaGoldAccent
import com.example.ui.theme.CinemaGreenActive
import com.example.ui.theme.CinemaPanelBg
import com.example.ui.theme.CinemaPanelBorder
import com.example.ui.theme.CinemaRedRecord
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceLight
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CinemaWhiteMuted

@Composable
fun ManualControlsDrawer(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    // Video settings
    videoResolution: VideoResolution,
    onSelectResolution: (VideoResolution) -> Unit,
    videoFps: VideoFps,
    onSelectFps: (VideoFps) -> Unit,
    videoBitrate: VideoBitrate,
    onSelectBitrate: (VideoBitrate) -> Unit,
    videoCodec: VideoCodec,
    onSelectCodec: (VideoCodec) -> Unit,
    // ISO
    isAutoIso: Boolean,
    manualIso: Int,
    onSetAutoIso: (Boolean) -> Unit,
    onSetManualIso: (Int) -> Unit,
    // Shutter
    shutterSpeed: ShutterSpeed,
    onSelectShutter: (ShutterSpeed) -> Unit,
    // Focus
    isAutoFocus: Boolean,
    manualFocusDistance: Float,
    onSetAutoFocus: (Boolean) -> Unit,
    onSetManualFocus: (Float) -> Unit,
    // White Balance
    whiteBalance: WhiteBalancePreset,
    customKelvin: Int,
    onSelectWb: (WhiteBalancePreset) -> Unit,
    onSetCustomKelvin: (Int) -> Unit,
    // Exposure
    exposureCompensation: Int,
    onSetExposureCompensation: (Int) -> Unit,
    isAeLocked: Boolean,
    onToggleAeLock: () -> Unit,
    isAfLocked: Boolean,
    onToggleAfLock: () -> Unit,
    // Filters & Tuning
    activeFilter: CinematicFilter,
    onSelectFilter: (CinematicFilter) -> Unit,
    filterTuning: FilterTuning,
    onUpdateFilterTuning: (FilterTuning) -> Unit,
    // Tools toggles
    showGrid: Boolean,
    onToggleGrid: () -> Unit,
    showCrosshair: Boolean,
    onToggleCrosshair: () -> Unit,
    showHorizon: Boolean,
    onToggleHorizon: () -> Unit,
    showHistogram: Boolean,
    onToggleHistogram: () -> Unit,
    showZebra: Boolean,
    onToggleZebra: () -> Unit,
    showFocusPeaking: Boolean,
    onToggleFocusPeaking: () -> Unit,
    videoStabilization: Boolean,
    onToggleStabilization: () -> Unit,
    externalMic: Boolean,
    onToggleExternalMic: () -> Unit,
    onResetDefaults: () -> Unit,
    hardwareLimits: CameraHardwareLimits
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("EXPOSURE", "FOCUS & WB", "VIDEO FORMAT", "FILTERS", "TOOLS")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = CinemaPanelBg,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CinemaGreenActive, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GTB PRO CONTROL CONSOLE",
                        color = CinemaWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.testTag("reset_controls_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Defaults",
                            tint = CinemaTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_controls_drawer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CinemaWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CinemaDarkBg,
                contentColor = CinemaGreenActive,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CinemaGreenActive,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) CinemaGreenActive else CinemaTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> ExposureTabContent(
                        isAutoIso = isAutoIso,
                        manualIso = manualIso,
                        onSetAutoIso = onSetAutoIso,
                        onSetManualIso = onSetManualIso,
                        shutterSpeed = shutterSpeed,
                        onSelectShutter = onSelectShutter,
                        exposureCompensation = exposureCompensation,
                        onSetExposureCompensation = onSetExposureCompensation,
                        isAeLocked = isAeLocked,
                        onToggleAeLock = onToggleAeLock,
                        hardwareLimits = hardwareLimits
                    )
                    1 -> FocusAndWbTabContent(
                        isAutoFocus = isAutoFocus,
                        manualFocusDistance = manualFocusDistance,
                        onSetAutoFocus = onSetAutoFocus,
                        onSetManualFocus = onSetManualFocus,
                        isAfLocked = isAfLocked,
                        onToggleAfLock = onToggleAfLock,
                        whiteBalance = whiteBalance,
                        customKelvin = customKelvin,
                        onSelectWb = onSelectWb,
                        onSetCustomKelvin = onSetCustomKelvin
                    )
                    2 -> VideoFormatTabContent(
                        videoResolution = videoResolution,
                        onSelectResolution = onSelectResolution,
                        videoFps = videoFps,
                        onSelectFps = onSelectFps,
                        videoBitrate = videoBitrate,
                        onSelectBitrate = onSelectBitrate,
                        videoCodec = videoCodec,
                        onSelectCodec = onSelectCodec
                    )
                    3 -> FiltersTabContent(
                        activeFilter = activeFilter,
                        onSelectFilter = onSelectFilter,
                        tuning = filterTuning,
                        onUpdateTuning = onUpdateFilterTuning
                    )
                    4 -> ToolsTabContent(
                        showGrid = showGrid,
                        onToggleGrid = onToggleGrid,
                        showCrosshair = showCrosshair,
                        onToggleCrosshair = onToggleCrosshair,
                        showHorizon = showHorizon,
                        onToggleHorizon = onToggleHorizon,
                        showHistogram = showHistogram,
                        onToggleHistogram = onToggleHistogram,
                        showZebra = showZebra,
                        onToggleZebra = onToggleZebra,
                        showFocusPeaking = showFocusPeaking,
                        onToggleFocusPeaking = onToggleFocusPeaking,
                        videoStabilization = videoStabilization,
                        onToggleStabilization = onToggleStabilization,
                        externalMic = externalMic,
                        onToggleExternalMic = onToggleExternalMic
                    )
                }
            }
        }
    }
}

@Composable
private fun ExposureTabContent(
    isAutoIso: Boolean,
    manualIso: Int,
    onSetAutoIso: (Boolean) -> Unit,
    onSetManualIso: (Int) -> Unit,
    shutterSpeed: ShutterSpeed,
    onSelectShutter: (ShutterSpeed) -> Unit,
    exposureCompensation: Int,
    onSetExposureCompensation: (Int) -> Unit,
    isAeLocked: Boolean,
    onToggleAeLock: () -> Unit,
    hardwareLimits: CameraHardwareLimits
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ISO Header & Auto toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ISO SENSITIVITY: ${if (isAutoIso) "AUTO ($manualIso)" else manualIso}",
                color = if (isAutoIso) CinemaGreenActive else CinemaGoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            ElevatedButton(
                onClick = { onSetAutoIso(!isAutoIso) },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (isAutoIso) CinemaGreenActive else CinemaSurfaceLight
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = if (isAutoIso) "AUTO ISO ON" else "SWITCH TO AUTO",
                    fontSize = 10.sp,
                    color = if (isAutoIso) CinemaDarkBg else CinemaWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ISO Slider
        Slider(
            value = manualIso.toFloat(),
            onValueChange = { onSetManualIso(it.toInt()) },
            valueRange = hardwareLimits.isoRange.start.toFloat()..hardwareLimits.isoRange.endInclusive.toFloat(),
            steps = 20,
            colors = SliderDefaults.colors(
                thumbColor = CinemaGoldAccent,
                activeTrackColor = CinemaGoldAccent,
                inactiveTrackColor = CinemaSurfaceLight
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Shutter Speed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHUTTER SPEED (EXPOSURE TIME)",
                color = CinemaWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isAeLocked) CinemaRedRecord else CinemaSurfaceLight)
                    .clickable { onToggleAeLock() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isAeLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "AE Lock",
                    tint = CinemaWhite,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAeLocked) "AE LOCKED" else "AE UNLOCKED",
                    color = CinemaWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShutterSpeed.values().forEach { ss ->
                val isSelected = shutterSpeed == ss
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) CinemaGreenActive else CinemaSurface)
                        .clickable { onSelectShutter(ss) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = ss.label,
                        color = if (isSelected) CinemaDarkBg else CinemaWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Exposure Compensation (EV)
        Text(
            text = "EXPOSURE COMPENSATION: ${if (exposureCompensation > 0) "+$exposureCompensation" else "$exposureCompensation"} EV",
            color = CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Slider(
            value = exposureCompensation.toFloat(),
            onValueChange = { onSetExposureCompensation(it.toInt()) },
            valueRange = hardwareLimits.exposureCompensationRange.start.toFloat()..hardwareLimits.exposureCompensationRange.endInclusive.toFloat(),
            steps = (hardwareLimits.exposureCompensationRange.endInclusive - hardwareLimits.exposureCompensationRange.start - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = CinemaGreenActive,
                activeTrackColor = CinemaGreenActive,
                inactiveTrackColor = CinemaSurfaceLight
            )
        )
    }
}

@Composable
private fun FocusAndWbTabContent(
    isAutoFocus: Boolean,
    manualFocusDistance: Float,
    onSetAutoFocus: (Boolean) -> Unit,
    onSetManualFocus: (Float) -> Unit,
    isAfLocked: Boolean,
    onToggleAfLock: () -> Unit,
    whiteBalance: WhiteBalancePreset,
    customKelvin: Int,
    onSelectWb: (WhiteBalancePreset) -> Unit,
    onSetCustomKelvin: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Focus Mode & Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FOCUS: ${if (isAutoFocus) "CONTINUOUS AF" else "MANUAL (${String.format("%.1f", manualFocusDistance)} Dpt)"}",
                color = if (isAutoFocus) CinemaGreenActive else CinemaGoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ElevatedButton(
                    onClick = { onSetAutoFocus(!isAutoFocus) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isAutoFocus) CinemaGreenActive else CinemaSurfaceLight
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = if (isAutoFocus) "AF AUTO" else "MF MANUAL",
                        fontSize = 10.sp,
                        color = if (isAutoFocus) CinemaDarkBg else CinemaWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isAfLocked) CinemaRedRecord else CinemaSurfaceLight)
                        .clickable { onToggleAfLock() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAfLocked) "AF LOCK" else "AF UNLOCK",
                        color = CinemaWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Manual Focus Slider (0.0 = Infinity, 10.0 = Macro)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("∞ Infinity", color = CinemaTextSecondary, fontSize = 10.sp)
            Text("Macro (10cm)", color = CinemaTextSecondary, fontSize = 10.sp)
        }
        Slider(
            value = manualFocusDistance,
            onValueChange = { onSetManualFocus(it) },
            valueRange = 0.0f..10.0f,
            colors = SliderDefaults.colors(
                thumbColor = CinemaGoldAccent,
                activeTrackColor = CinemaGoldAccent,
                inactiveTrackColor = CinemaSurfaceLight
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // White Balance Preset Selector
        Text(
            text = "WHITE BALANCE / COLOR TEMPERATURE",
            color = CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WhiteBalancePreset.values().forEach { wb ->
                val isSelected = whiteBalance == wb
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) CinemaGreenActive else CinemaSurface)
                        .clickable { onSelectWb(wb) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = wb.label,
                        color = if (isSelected) CinemaDarkBg else CinemaWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Custom Kelvin Slider if active
        if (whiteBalance == WhiteBalancePreset.CUSTOM_KELVIN) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "COLOR TEMP: ${customKelvin}K",
                color = CinemaGoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = customKelvin.toFloat(),
                onValueChange = { onSetCustomKelvin(it.toInt()) },
                valueRange = 2500f..9500f,
                steps = 70,
                colors = SliderDefaults.colors(
                    thumbColor = CinemaGoldAccent,
                    activeTrackColor = CinemaGoldAccent,
                    inactiveTrackColor = CinemaSurfaceLight
                )
            )
        }
    }
}

@Composable
private fun VideoFormatTabContent(
    videoResolution: VideoResolution,
    onSelectResolution: (VideoResolution) -> Unit,
    videoFps: VideoFps,
    onSelectFps: (VideoFps) -> Unit,
    videoBitrate: VideoBitrate,
    onSelectBitrate: (VideoBitrate) -> Unit,
    videoCodec: VideoCodec,
    onSelectCodec: (VideoCodec) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Resolution
        Text(
            text = "VIDEO RESOLUTION",
            color = CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VideoResolution.values().forEach { res ->
                val isSelected = videoResolution == res
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) CinemaGreenActive else CinemaSurface)
                        .clickable { onSelectResolution(res) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = res.label,
                        color = if (isSelected) CinemaDarkBg else CinemaWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Frame Rates
        Text(
            text = "FRAME RATE (FPS)",
            color = CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VideoFps.values().forEach { fps ->
                val isSelected = videoFps == fps
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) CinemaGreenActive else CinemaSurface)
                        .clickable { onSelectFps(fps) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = fps.label,
                        color = if (isSelected) CinemaDarkBg else CinemaWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bitrate & Codec
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ENCODER CODEC",
                    color = CinemaWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                VideoCodec.values().forEach { codec ->
                    val isSelected = videoCodec == codec
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) CinemaGreenActive else CinemaSurface)
                            .clickable { onSelectCodec(codec) }
                            .padding(vertical = 5.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = codec.label,
                            color = if (isSelected) CinemaDarkBg else CinemaWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET BITRATE",
                    color = CinemaWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                VideoBitrate.values().forEach { br ->
                    val isSelected = videoBitrate == br
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) CinemaGreenActive else CinemaSurface)
                            .clickable { onSelectBitrate(br) }
                            .padding(vertical = 5.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = br.label,
                            color = if (isSelected) CinemaDarkBg else CinemaWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun FiltersTabContent(
    activeFilter: CinematicFilter,
    onSelectFilter: (CinematicFilter) -> Unit,
    tuning: FilterTuning,
    onUpdateTuning: (FilterTuning) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "LIVE CINEMATIC COLOR GRADING LUTS",
            color = CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal filter presets cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CinematicFilter.values().forEach { filter ->
                val isSelected = activeFilter == filter
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onSelectFilter(filter) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CinemaGreenActive.copy(alpha = 0.2f) else CinemaSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CinemaGreenActive else CinemaPanelBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = filter.title,
                                color = if (isSelected) CinemaGreenActive else CinemaWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CinemaGreenActive,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = filter.description,
                            color = CinemaTextSecondary,
                            fontSize = 9.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tuning parameters: Contrast & Saturation & Temperature
        Text(
            text = "COLOR MATRIX TUNING",
            color = CinemaGoldAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Contrast: ${String.format("%.2f", tuning.contrast)}x", color = CinemaWhite, fontSize = 10.sp)
            Text("Saturation: ${String.format("%.2f", tuning.saturation)}x", color = CinemaWhite, fontSize = 10.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Slider(
                value = tuning.contrast,
                onValueChange = { onUpdateTuning(tuning.copy(contrast = it)) },
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = CinemaGoldAccent,
                    activeTrackColor = CinemaGoldAccent
                )
            )
            Slider(
                value = tuning.saturation,
                onValueChange = { onUpdateTuning(tuning.copy(saturation = it)) },
                valueRange = 0.0f..2.0f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = CinemaGoldAccent,
                    activeTrackColor = CinemaGoldAccent
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Temperature: ${String.format("%+.2f", tuning.temperature)}", color = CinemaWhite, fontSize = 10.sp)
            Text("Tint: ${String.format("%+.2f", tuning.tint)}", color = CinemaWhite, fontSize = 10.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Slider(
                value = tuning.temperature,
                onValueChange = { onUpdateTuning(tuning.copy(temperature = it)) },
                valueRange = -1.0f..1.0f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = CinemaGreenActive,
                    activeTrackColor = CinemaGreenActive
                )
            )
            Slider(
                value = tuning.tint,
                onValueChange = { onUpdateTuning(tuning.copy(tint = it)) },
                valueRange = -1.0f..1.0f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = CinemaGreenActive,
                    activeTrackColor = CinemaGreenActive
                )
            )
        }
    }
}

@Composable
private fun ToolsTabContent(
    showGrid: Boolean,
    onToggleGrid: () -> Unit,
    showCrosshair: Boolean,
    onToggleCrosshair: () -> Unit,
    showHorizon: Boolean,
    onToggleHorizon: () -> Unit,
    showHistogram: Boolean,
    onToggleHistogram: () -> Unit,
    showZebra: Boolean,
    onToggleZebra: () -> Unit,
    showFocusPeaking: Boolean,
    onToggleFocusPeaking: () -> Unit,
    videoStabilization: Boolean,
    onToggleStabilization: () -> Unit,
    externalMic: Boolean,
    onToggleExternalMic: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PROFESSIONAL MONITORING & OVERLAYS",
            color = CinemaWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))

        val toolItems = listOf(
            Triple("Rule of Thirds Grid", showGrid, onToggleGrid),
            Triple("Center Crosshair", showCrosshair, onToggleCrosshair),
            Triple("Electronic Horizon Level", showHorizon, onToggleHorizon),
            Triple("Real-time RGB Histogram", showHistogram, onToggleHistogram),
            Triple("Zebra Exposure Warning (70%/100%)", showZebra, onToggleZebra),
            Triple("Focus Peaking Assist (Green Edge)", showFocusPeaking, onToggleFocusPeaking),
            Triple("Video Stabilization (OIS/EIS)", videoStabilization, onToggleStabilization),
            Triple("External Microphone Priority", externalMic, onToggleExternalMic)
        )

        toolItems.chunked(2).forEach { rowTools ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTools.forEach { (label, isEnabled, onToggle) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isEnabled) CinemaGreenActive.copy(alpha = 0.15f) else CinemaSurface)
                            .clickable { onToggle() }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (isEnabled) CinemaGreenActive else CinemaWhiteMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isEnabled) CinemaGreenActive else Color.DarkGray,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
