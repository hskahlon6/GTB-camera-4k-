package com.example.model

enum class CameraMode {
    PHOTO,
    VIDEO
}

enum class LensFacing {
    BACK,
    FRONT
}

enum class VideoResolution(val label: String, val width: Int, val height: Int) {
    UHD_4K("4K UHD", 3840, 2160),
    FHD_1080P("1080p FHD", 1920, 1080),
    HD_720P("720p HD", 1280, 720)
}

enum class VideoFps(val fps: Int, val label: String) {
    FPS_24(24, "24 fps Cinema"),
    FPS_25(25, "25 fps PAL"),
    FPS_30(30, "30 fps Standard"),
    FPS_50(50, "50 fps PAL High"),
    FPS_60(60, "60 fps Smooth"),
    FPS_120(120, "120 fps Slow-Mo")
}

enum class VideoCodec(val label: String, val mime: String) {
    H264("H.264 (AVC)", "video/avc"),
    H265("H.265 (HEVC)", "video/hevc")
}

enum class VideoBitrate(val label: String, val bps: Int) {
    CINEMA_100M("100 Mbps (Master)", 100_000_000),
    PRO_60M("60 Mbps (High)", 60_000_000),
    STD_35M("35 Mbps (Standard)", 35_000_000),
    ECO_18M("18 Mbps (Economy)", 18_000_000)
}

enum class ShutterSpeed(val label: String, val nanos: Long?) {
    AUTO("AUTO", null),
    SS_1_8000("1/8000", 125_000L),
    SS_1_4000("1/4000", 250_000L),
    SS_1_2000("1/2000", 500_000L),
    SS_1_1000("1/1000", 1_000_000L),
    SS_1_500("1/500", 2_000_000L),
    SS_1_250("1/250", 4_000_000L),
    SS_1_125("1/125", 8_000_000L),
    SS_1_60("1/60", 16_666_667L),
    SS_1_30("1/30", 33_333_333L),
    SS_1_15("1/15", 66_666_667L),
    SS_1_8("1/8", 125_000_000L)
}

enum class WhiteBalancePreset(val label: String, val awbMode: Int, val kelvin: Int) {
    AUTO("AUTO", 1, 5500), // CONTROL_AWB_MODE_AUTO
    DAYLIGHT("DAYLIGHT", 5, 5600), // CONTROL_AWB_MODE_DAYLIGHT
    CLOUDY("CLOUDY", 6, 6500), // CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
    TUNGSTEN("TUNGSTEN", 3, 3200), // CONTROL_AWB_MODE_INCANDESCENT
    FLUORESCENT("FLUORESCENT", 4, 4000), // CONTROL_AWB_MODE_FLUORESCENT
    CUSTOM_KELVIN("KELVIN", 0, 5000)
}

enum class CinematicFilter(
    val title: String,
    val description: String,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val tempTintR: Float = 1.0f,
    val tempTintG: Float = 1.0f,
    val tempTintB: Float = 1.0f,
    val brightness: Float = 0.0f
) {
    NATURAL("Natural", "Unprocessed sensor color grade", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f),
    TEAL_AND_ORANGE("Teal & Orange", "Hollywood blockbuster cinema aesthetic", 1.25f, 1.3f, 1.25f, 0.95f, 1.35f, 0.0f),
    HOLLYWOOD_FILM("Hollywood Film", "Rich cinematic blacks with warm highlights", 1.2f, 1.15f, 1.1f, 1.0f, 0.9f, 0.02f),
    WARM_FILM("Warm Film", "Golden hour sunset tones with soft contrast", 1.1f, 1.1f, 1.22f, 1.05f, 0.88f, 0.03f),
    COOL_FILM("Cool Film", "Moody Nordic blue undertones", 1.15f, 1.05f, 0.9f, 1.02f, 1.22f, 0.0f),
    VINTAGE("Vintage 70s", "Classic analogue warm film stock feel", 1.1f, 0.85f, 1.15f, 1.1f, 0.92f, 0.04f),
    BLACK_AND_WHITE("Black & White", "High-contrast monochrome panchromatic", 1.35f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f),
    DOCUMENTARY("Documentary", "Realistic low-saturation newsreel grade", 1.05f, 0.8f, 1.0f, 1.0f, 1.0f, -0.02f),
    MOODY("Moody Noir", "Deep shadows with punchy midtone contrast", 1.35f, 0.9f, 0.95f, 0.92f, 1.05f, -0.05f),
    PORTRAIT("Portrait Glow", "Flattering warm skin tones and balanced contrast", 1.05f, 1.1f, 1.12f, 1.02f, 0.98f, 0.03f)
}

data class FilterTuning(
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val highlights: Float = 0.0f,
    val shadows: Float = 0.0f,
    val sharpness: Float = 0.0f,
    val temperature: Float = 0.0f, // -1.0 (cool) to +1.0 (warm)
    val tint: Float = 0.0f, // -1.0 (green) to +1.0 (magenta)
    val exposure: Float = 0.0f // -2.0 to +2.0
)

data class CameraHardwareLimits(
    val isoRange: ClosedRange<Int> = 100..3200,
    val exposureCompensationRange: ClosedRange<Int> = -4..4,
    val exposureCompensationStep: Float = 0.5f,
    val supportedResolutions: List<VideoResolution> = listOf(VideoResolution.UHD_4K, VideoResolution.FHD_1080P, VideoResolution.HD_720P),
    val isManualFocusSupported: Boolean = true,
    val isManualIsoSupported: Boolean = true,
    val isStabilizationSupported: Boolean = true,
    val isRawSupported: Boolean = false,
    val maxZoomRatio: Float = 10f,
    val minZoomRatio: Float = 1f
)

data class CameraCaptureStats(
    val batteryPct: Int = 85,
    val storageFreeGb: Double = 42.5,
    val recordingDurationSec: Long = 0L,
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val audioLevelDb: Float = -18f,
    val histogramData: IntArray = IntArray(32) { 10 + (it * 3 % 40) },
    val rollDegrees: Float = 0f, // Horizon level
    val pitchDegrees: Float = 0f
)
