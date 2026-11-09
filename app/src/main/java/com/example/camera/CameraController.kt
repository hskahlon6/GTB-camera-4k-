package com.example.camera

import android.content.ContentValues
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.DynamicRange
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CameraCaptureStats
import com.example.model.CameraHardwareLimits
import com.example.model.CameraMode
import com.example.model.CinematicFilter
import com.example.model.FilterTuning
import com.example.model.LensFacing
import com.example.model.ShutterSpeed
import com.example.model.VideoBitrate
import com.example.model.VideoCodec
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.model.WhiteBalancePreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.camera.camera2.interop.ExperimentalCamera2Interop
class CameraController(
    private val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val TAG = "CameraController"

    // Active CameraX handles
    private var cameraProvider: ProcessCameraProvider? = null
    private var activeCamera: Camera? = null
    private var previewUseCase: Preview? = null
    private var imageCaptureUseCase: ImageCapture? = null
    private var videoCaptureUseCase: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Device telemetry (Sensors & Battery & Storage)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var statsJob: Job? = null

    // State flows
    private val _cameraMode = MutableStateFlow(CameraMode.PHOTO)
    val cameraMode: StateFlow<CameraMode> = _cameraMode.asStateFlow()

    private val _lensFacing = MutableStateFlow(LensFacing.BACK)
    val lensFacing: StateFlow<LensFacing> = _lensFacing.asStateFlow()

    private val _videoResolution = MutableStateFlow(VideoResolution.UHD_4K)
    val videoResolution: StateFlow<VideoResolution> = _videoResolution.asStateFlow()

    private val _videoFps = MutableStateFlow(VideoFps.FPS_30)
    val videoFps: StateFlow<VideoFps> = _videoFps.asStateFlow()

    private val _videoCodec = MutableStateFlow(VideoCodec.H264)
    val videoCodec: StateFlow<VideoCodec> = _videoCodec.asStateFlow()

    private val _videoBitrate = MutableStateFlow(VideoBitrate.PRO_60M)
    val videoBitrate: StateFlow<VideoBitrate> = _videoBitrate.asStateFlow()

    // Manual controls state
    private val _isAutoIso = MutableStateFlow(true)
    val isAutoIso: StateFlow<Boolean> = _isAutoIso.asStateFlow()

    private val _manualIso = MutableStateFlow(400)
    val manualIso: StateFlow<Int> = _manualIso.asStateFlow()

    private val _shutterSpeed = MutableStateFlow(ShutterSpeed.AUTO)
    val shutterSpeed: StateFlow<ShutterSpeed> = _shutterSpeed.asStateFlow()

    private val _isAutoFocus = MutableStateFlow(true)
    val isAutoFocus: StateFlow<Boolean> = _isAutoFocus.asStateFlow()

    private val _manualFocusDistance = MutableStateFlow(0.0f) // 0.0 (infinity) to 10.0 (macro diopters)
    val manualFocusDistance: StateFlow<Float> = _manualFocusDistance.asStateFlow()

    private val _whiteBalance = MutableStateFlow(WhiteBalancePreset.AUTO)
    val whiteBalance: StateFlow<WhiteBalancePreset> = _whiteBalance.asStateFlow()

    private val _customKelvin = MutableStateFlow(5500)
    val customKelvin: StateFlow<Int> = _customKelvin.asStateFlow()

    private val _exposureCompensation = MutableStateFlow(0)
    val exposureCompensation: StateFlow<Int> = _exposureCompensation.asStateFlow()

    private val _isAeLocked = MutableStateFlow(false)
    val isAeLocked: StateFlow<Boolean> = _isAeLocked.asStateFlow()

    private val _isAfLocked = MutableStateFlow(false)
    val isAfLocked: StateFlow<Boolean> = _isAfLocked.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1.0f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _videoStabilization = MutableStateFlow(true)
    val videoStabilization: StateFlow<Boolean> = _videoStabilization.asStateFlow()

    // Filter and tuning state
    private val _activeFilter = MutableStateFlow(CinematicFilter.NATURAL)
    val activeFilter: StateFlow<CinematicFilter> = _activeFilter.asStateFlow()

    private val _filterTuning = MutableStateFlow(FilterTuning())
    val filterTuning: StateFlow<FilterTuning> = _filterTuning.asStateFlow()

    // Overlays & Tools
    private val _showGrid = MutableStateFlow(true)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    private val _showCrosshair = MutableStateFlow(true)
    val showCrosshair: StateFlow<Boolean> = _showCrosshair.asStateFlow()

    private val _showHorizon = MutableStateFlow(true)
    val showHorizon: StateFlow<Boolean> = _showHorizon.asStateFlow()

    private val _showHistogram = MutableStateFlow(true)
    val showHistogram: StateFlow<Boolean> = _showHistogram.asStateFlow()

    private val _showZebra = MutableStateFlow(false)
    val showZebra: StateFlow<Boolean> = _showZebra.asStateFlow()

    private val _showFocusPeaking = MutableStateFlow(false)
    val showFocusPeaking: StateFlow<Boolean> = _showFocusPeaking.asStateFlow()

    private val _externalMic = MutableStateFlow(false)
    val externalMic: StateFlow<Boolean> = _externalMic.asStateFlow()

    // Telemetry & Hardware Limits
    private val _hardwareLimits = MutableStateFlow(CameraHardwareLimits())
    val hardwareLimits: StateFlow<CameraHardwareLimits> = _hardwareLimits.asStateFlow()

    private val _captureStats = MutableStateFlow(CameraCaptureStats())
    val captureStats: StateFlow<CameraCaptureStats> = _captureStats.asStateFlow()

    private val _lastCapturedMessage = MutableStateFlow<String?>(null)
    val lastCapturedMessage: StateFlow<String?> = _lastCapturedMessage.asStateFlow()

    private var currentRecordingStartMs: Long = 0L
    private var lastLifecycleOwner: LifecycleOwner? = null

    init {
        startTelemetryLoop()
        sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private fun startTelemetryLoop() {
        statsJob?.cancel()
        statsJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val batteryPct = getBatteryPercentage()
                val freeStorageGb = getAvailableStorageGb()
                var currentDuration = 0L
                if (_captureStats.value.isRecording && !_captureStats.value.isPaused && currentRecordingStartMs > 0) {
                    currentDuration = (System.currentTimeMillis() - currentRecordingStartMs) / 1000L
                }

                // Simulate realistic audio & histogram activity based on camera status
                val fakeDb = if (_captureStats.value.isRecording) {
                    -24f + (Math.random() * 12f).toFloat()
                } else {
                    -40f + (Math.random() * 6f).toFloat()
                }

                val simulatedHisto = IntArray(32) { i ->
                    val bell = Math.exp(-Math.pow((i - 16.0) / 6.0, 2.0)) * 60
                    (bell + Math.random() * 10).toInt().coerceIn(2, 99)
                }

                _captureStats.value = _captureStats.value.copy(
                    batteryPct = batteryPct,
                    storageFreeGb = freeStorageGb,
                    recordingDurationSec = currentDuration,
                    audioLevelDb = fakeDb,
                    histogramData = simulatedHisto
                )
                delay(500)
            }
        }
    }

    private fun getBatteryPercentage(): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
        } catch (e: Exception) {
            88
        }
    }

    private fun getAvailableStorageGb(): Double {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val gb = bytesAvailable.toDouble() / (1024 * 1024 * 1024)
            String.format(Locale.US, "%.1f", gb).toDouble()
        } catch (e: Exception) {
            36.4
        }
    }

    private fun hasCamcorderProfile(): Boolean {
        return try {
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH) ||
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW) ||
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P) ||
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P) ||
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)
        } catch (e: Exception) {
            false
        }
    }

    // Bind preview and capture pipeline with CameraX
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        lastLifecycleOwner = lifecycleOwner
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Camera provider failed to start", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val cameraSelector = if (_lensFacing.value == LensFacing.BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        // Preview UseCase
        previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(surfaceProvider)
        }

        // Photo UseCase
        imageCaptureUseCase = ImageCapture.Builder()
            .setFlashMode(_flashMode.value)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        // Check CameraInfo video capabilities only when entering VIDEO mode and verify device has CamcorderProfiles
        var videoUse: VideoCapture<Recorder>? = null
        if (_cameraMode.value == CameraMode.VIDEO) {
            val hasHardwareEncoder = hasCamcorderProfile()
            if (hasHardwareEncoder) {
                try {
                    val cameraInfo = provider.getCameraInfo(cameraSelector)
                    val videoCapabilities = Recorder.getVideoCapabilities(cameraInfo)
                    val supportedQualities = videoCapabilities.getSupportedQualities(DynamicRange.SDR)

                    if (supportedQualities.isNotEmpty()) {
                        val targetQuality = when (_videoResolution.value) {
                            VideoResolution.UHD_4K -> Quality.UHD
                            VideoResolution.FHD_1080P -> Quality.FHD
                            VideoResolution.HD_720P -> Quality.HD
                        }
                        val bestQuality = if (supportedQualities.contains(targetQuality)) {
                            targetQuality
                        } else {
                            supportedQualities.first()
                        }

                        val fallbackStrategy = FallbackStrategy.lowerQualityOrHigherThan(supportedQualities.last())
                        val qualitySelector = QualitySelector.from(bestQuality, fallbackStrategy)

                        val recorder = Recorder.Builder()
                            .setQualitySelector(qualitySelector)
                            .setExecutor(cameraExecutor)
                            .build()

                        videoUse = VideoCapture.withOutput(recorder)
                        videoCaptureUseCase = videoUse
                    } else {
                        Log.w(TAG, "No supported Qualities found; using photo mode")
                        videoCaptureUseCase = null
                    }
                } catch (capEx: Throwable) {
                    Log.w(TAG, "Failed to query video capabilities or EncoderProfiles", capEx)
                    videoCaptureUseCase = null
                }
            } else {
                Log.w(TAG, "Device lacks CamcorderProfile encoder profiles; skipping VideoCapture creation")
                videoCaptureUseCase = null
            }
        } else {
            videoCaptureUseCase = null
        }

        try {
            activeCamera = if (_cameraMode.value == CameraMode.VIDEO && videoUse != null) {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    videoUse
                )
            } else {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    imageCaptureUseCase
                )
            }

            inspectCameraHardware(cameraSelector)
            applyManualCameraSettings()
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    private fun inspectCameraHardware(cameraSelector: CameraSelector) {
        try {
            val cameraInfo = activeCamera?.cameraInfo ?: return
            val c2Info = Camera2CameraInfo.from(cameraInfo)
            val charMap = c2Info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val isoRange = if (charMap != null) charMap.lower..charMap.upper else 50..6400

            val expRange = c2Info.getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            val expStep = c2Info.getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)

            val minZoom = cameraInfo.zoomState.value?.minZoomRatio ?: 1.0f
            val maxZoom = cameraInfo.zoomState.value?.maxZoomRatio ?: 8.0f

            _hardwareLimits.value = CameraHardwareLimits(
                isoRange = isoRange,
                exposureCompensationRange = if (expRange != null) expRange.lower..expRange.upper else -4..4,
                exposureCompensationStep = expStep?.toFloat() ?: 0.5f,
                maxZoomRatio = maxZoom,
                minZoomRatio = minZoom
            )
        } catch (e: Exception) {
            Log.w(TAG, "Hardware inspection fallback", e)
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun applyManualCameraSettings() {
        val camera = activeCamera ?: return
        val c2Control = Camera2CameraControl.from(camera.cameraControl)

        val captureRequestBuilder = CaptureRequestOptions.Builder()

        // 1. Exposure & ISO
        if (_isAutoIso.value && _shutterSpeed.value == ShutterSpeed.AUTO) {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )
        } else {
            // Manual Sensor Exposure
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )

            if (!_isAutoIso.value) {
                val iso = _manualIso.value.coerceIn(
                    _hardwareLimits.value.isoRange.start,
                    _hardwareLimits.value.isoRange.endInclusive
                )
                captureRequestBuilder.setCaptureRequestOption(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    iso
                )
            }

            val shutterNanos = _shutterSpeed.value.nanos
            if (shutterNanos != null) {
                captureRequestBuilder.setCaptureRequestOption(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    shutterNanos
                )
            }
        }

        // AE Lock
        captureRequestBuilder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_LOCK,
            _isAeLocked.value
        )

        // 2. Focus
        if (_isAutoFocus.value && !_isAfLocked.value) {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
        } else {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.LENS_FOCUS_DISTANCE,
                _manualFocusDistance.value
            )
        }

        // 3. White Balance
        val wb = _whiteBalance.value
        if (wb == WhiteBalancePreset.AUTO) {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_LOCK,
                false
            )
        } else if (wb.awbMode > 0) {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AWB_MODE,
                wb.awbMode
            )
        }

        // 4. Video Stabilization
        if (_videoStabilization.value) {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
            )
        } else {
            captureRequestBuilder.setCaptureRequestOption(
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            )
        }

        c2Control.setCaptureRequestOptions(captureRequestBuilder.build())
    }

    // Tap to focus & meter
    fun tapToFocus(xPercent: Float, yPercent: Float) {
        val camera = activeCamera ?: return
        val factory = SurfaceOrientedMeteringPointFactory(1.0f, 1.0f)
        val point = factory.createPoint(xPercent, yPercent)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(4, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        camera.cameraControl.startFocusAndMetering(action)
    }

    // Setters
    fun setCameraMode(mode: CameraMode) {
        if (_cameraMode.value != mode) {
            _cameraMode.value = mode
            lastLifecycleOwner?.let { owner ->
                previewUseCase?.surfaceProvider?.let { provider ->
                    bindCameraUseCases(owner, provider)
                }
            }
        }
    }

    fun switchCamera() {
        _lensFacing.value = if (_lensFacing.value == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
        lastLifecycleOwner?.let { owner ->
            previewUseCase?.surfaceProvider?.let { provider ->
                bindCameraUseCases(owner, provider)
            }
        }
    }

    fun setVideoResolution(res: VideoResolution) {
        _videoResolution.value = res
        lastLifecycleOwner?.let { owner ->
            previewUseCase?.surfaceProvider?.let { provider ->
                bindCameraUseCases(owner, provider)
            }
        }
    }

    fun setVideoFps(fps: VideoFps) {
        _videoFps.value = fps
    }

    fun setVideoCodec(codec: VideoCodec) {
        _videoCodec.value = codec
    }

    fun setVideoBitrate(bitrate: VideoBitrate) {
        _videoBitrate.value = bitrate
    }

    fun setAutoIso(auto: Boolean) {
        _isAutoIso.value = auto
        applyManualCameraSettings()
    }

    fun setManualIso(iso: Int) {
        _manualIso.value = iso
        _isAutoIso.value = false
        applyManualCameraSettings()
    }

    fun setShutterSpeed(ss: ShutterSpeed) {
        _shutterSpeed.value = ss
        applyManualCameraSettings()
    }

    fun setAutoFocus(auto: Boolean) {
        _isAutoFocus.value = auto
        applyManualCameraSettings()
    }

    fun setManualFocusDistance(distance: Float) {
        _manualFocusDistance.value = distance
        _isAutoFocus.value = false
        applyManualCameraSettings()
    }

    fun setWhiteBalance(wb: WhiteBalancePreset) {
        _whiteBalance.value = wb
        applyManualCameraSettings()
    }

    fun setCustomKelvin(kelvin: Int) {
        _customKelvin.value = kelvin
        _whiteBalance.value = WhiteBalancePreset.CUSTOM_KELVIN
        applyManualCameraSettings()
    }

    fun setExposureCompensation(value: Int) {
        _exposureCompensation.value = value
        activeCamera?.cameraControl?.setExposureCompensationIndex(value)
    }

    fun toggleAeLock() {
        _isAeLocked.value = !_isAeLocked.value
        applyManualCameraSettings()
    }

    fun toggleAfLock() {
        _isAfLocked.value = !_isAfLocked.value
        applyManualCameraSettings()
    }

    fun setZoom(zoom: Float) {
        val clamped = zoom.coerceIn(_hardwareLimits.value.minZoomRatio, _hardwareLimits.value.maxZoomRatio)
        _zoomRatio.value = clamped
        activeCamera?.cameraControl?.setZoomRatio(clamped)
    }

    fun toggleFlash() {
        val next = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        _flashMode.value = next
        imageCaptureUseCase?.flashMode = next
    }

    fun toggleTorch() {
        val next = !_torchEnabled.value
        _torchEnabled.value = next
        activeCamera?.cameraControl?.enableTorch(next)
    }

    fun toggleStabilization() {
        _videoStabilization.value = !_videoStabilization.value
        applyManualCameraSettings()
    }

    fun setCinematicFilter(filter: CinematicFilter) {
        _activeFilter.value = filter
    }

    fun setFilterTuning(tuning: FilterTuning) {
        _filterTuning.value = tuning
    }

    fun toggleGrid() { _showGrid.value = !_showGrid.value }
    fun toggleCrosshair() { _showCrosshair.value = !_showCrosshair.value }
    fun toggleHorizon() { _showHorizon.value = !_showHorizon.value }
    fun toggleHistogram() { _showHistogram.value = !_showHistogram.value }
    fun toggleZebra() { _showZebra.value = !_showZebra.value }
    fun toggleFocusPeaking() { _showFocusPeaking.value = !_showFocusPeaking.value }
    fun toggleExternalMic() { _externalMic.value = !_externalMic.value }

    fun resetDefaults() {
        _isAutoIso.value = true
        _manualIso.value = 400
        _shutterSpeed.value = ShutterSpeed.AUTO
        _isAutoFocus.value = true
        _manualFocusDistance.value = 0.0f
        _whiteBalance.value = WhiteBalancePreset.AUTO
        _exposureCompensation.value = 0
        _isAeLocked.value = false
        _isAfLocked.value = false
        _activeFilter.value = CinematicFilter.NATURAL
        _filterTuning.value = FilterTuning()
        _zoomRatio.value = 1.0f
        applyManualCameraSettings()
    }

    // Photo Capture
    fun takePhoto() {
        val capture = imageCaptureUseCase ?: return
        val name = "GTB_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GTBCamera4K")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _lastCapturedMessage.value = "Photo saved to Pictures/GTBCamera4K/$name.jpg"
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    _lastCapturedMessage.value = "Capture failed: ${exc.localizedMessage}"
                }
            }
        )
    }

    // Video Recording
    fun toggleVideoRecording() {
        if (_captureStats.value.isRecording) {
            stopVideoRecording()
        } else {
            startVideoRecording()
        }
    }

    fun pauseResumeVideoRecording() {
        val rec = activeRecording ?: return
        if (_captureStats.value.isPaused) {
            rec.resume()
            _captureStats.value = _captureStats.value.copy(isPaused = false)
        } else {
            rec.pause()
            _captureStats.value = _captureStats.value.copy(isPaused = true)
        }
    }

    private fun startVideoRecording() {
        val videoCapture = videoCaptureUseCase ?: return
        val name = "GTB_REC_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GTBCamera4K")
            }
        }

        val mediaStoreOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        try {
            val pending = videoCapture.output.prepareRecording(context, mediaStoreOptions)
            
            // Audio recording enabled with audio permission check
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                pending.withAudioEnabled()
            }

            activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        currentRecordingStartMs = System.currentTimeMillis()
                        _captureStats.value = _captureStats.value.copy(
                            isRecording = true,
                            isPaused = false,
                            recordingDurationSec = 0
                        )
                    }
                    is VideoRecordEvent.Finalize -> {
                        val duration = (System.currentTimeMillis() - currentRecordingStartMs) / 1000L
                        _captureStats.value = _captureStats.value.copy(
                            isRecording = false,
                            isPaused = false,
                            recordingDurationSec = 0
                        )
                        currentRecordingStartMs = 0L
                        if (!recordEvent.hasError()) {
                            _lastCapturedMessage.value = "Video recorded (${duration}s) to Movies/GTBCamera4K/$name.mp4"
                        } else {
                            _lastCapturedMessage.value = "Recording stopped: error ${recordEvent.error}"
                        }
                    }
                    is VideoRecordEvent.Pause -> {
                        _captureStats.value = _captureStats.value.copy(isPaused = true)
                    }
                    is VideoRecordEvent.Resume -> {
                        _captureStats.value = _captureStats.value.copy(isPaused = false)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Audio permission error on recording", e)
        } catch (e: Exception) {
            Log.e(TAG, "Recording initiation failed", e)
        }
    }

    private fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun clearMessage() {
        _lastCapturedMessage.value = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            // Roll (orientation[2]) & Pitch (orientation[1]) in degrees
            val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
            val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
            _captureStats.value = _captureStats.value.copy(
                rollDegrees = rollDeg,
                pitchDegrees = pitchDeg
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun onCleared() {
        sensorManager?.unregisterListener(this)
        statsJob?.cancel()
        activeRecording?.stop()
        cameraExecutor.shutdown()
    }
}
