package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraCaptureStats
import com.example.ui.theme.CinemaGreenActive
import com.example.ui.theme.CinemaOverlayDark
import com.example.ui.theme.CinemaPeakingGreen
import com.example.ui.theme.CinemaRedRecord
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CinemaZebraYellow
import kotlin.math.abs

@Composable
fun CameraOverlays(
    modifier: Modifier = Modifier,
    showGrid: Boolean,
    showCrosshair: Boolean,
    showHorizon: Boolean,
    showZebra: Boolean,
    showFocusPeaking: Boolean,
    stats: CameraCaptureStats
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Grid lines (Rule of Thirds) and Center Crosshair & Horizon Level
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Rule of Thirds Grid
            if (showGrid) {
                val oneThirdX = width / 3f
                val twoThirdX = width * 2f / 3f
                val oneThirdY = height / 3f
                val twoThirdY = height * 2f / 3f

                val gridColor = Color(0x66FFFFFF)
                val strokeWidth = 1.dp.toPx()

                // Vertical lines
                drawLine(gridColor, Offset(oneThirdX, 0f), Offset(oneThirdX, height), strokeWidth)
                drawLine(gridColor, Offset(twoThirdX, 0f), Offset(twoThirdX, height), strokeWidth)

                // Horizontal lines
                drawLine(gridColor, Offset(0f, oneThirdY), Offset(width, oneThirdY), strokeWidth)
                drawLine(gridColor, Offset(0f, twoThirdY), Offset(width, twoThirdY), strokeWidth)
            }

            // 2. Center Crosshair
            if (showCrosshair) {
                val cx = width / 2f
                val cy = height / 2f
                val crossColor = CinemaWhite.copy(alpha = 0.8f)
                val armLength = 20.dp.toPx()
                val gap = 6.dp.toPx()

                // Horizontal crosshair
                drawLine(crossColor, Offset(cx - armLength, cy), Offset(cx - gap, cy), 1.5.dp.toPx())
                drawLine(crossColor, Offset(cx + gap, cy), Offset(cx + armLength, cy), 1.5.dp.toPx())

                // Vertical crosshair
                drawLine(crossColor, Offset(cx, cy - armLength), Offset(cx, cy - gap), 1.5.dp.toPx())
                drawLine(crossColor, Offset(cx, cy + gap), Offset(cx, cy + armLength), 1.5.dp.toPx())

                // Center dot
                drawCircle(CinemaGreenActive, radius = 2.dp.toPx(), center = Offset(cx, cy))
            }

            // 3. Electronic Horizon Level (Artificial Horizon)
            if (showHorizon) {
                val cx = width / 2f
                val cy = height / 2f
                val roll = stats.rollDegrees
                val isLevel = abs(roll) < 1.0f
                val horizonColor = if (isLevel) CinemaGreenActive else Color(0xDDFFB300)

                val barHalfWidth = 70.dp.toPx()
                val angleRad = Math.toRadians(-roll.toDouble())
                val cos = Math.cos(angleRad).toFloat()
                val sin = Math.sin(angleRad).toFloat()

                val p1 = Offset(cx - barHalfWidth * cos, cy - barHalfWidth * sin)
                val p2 = Offset(cx + barHalfWidth * cos, cy + barHalfWidth * sin)

                drawLine(horizonColor, p1, p2, strokeWidth = 2.dp.toPx())

                // Stationary horizon ticks
                val tickColor = Color(0x66FFFFFF)
                drawLine(tickColor, Offset(cx - barHalfWidth - 10f, cy), Offset(cx - barHalfWidth, cy), 2.dp.toPx())
                drawLine(tickColor, Offset(cx + barHalfWidth, cy), Offset(cx + barHalfWidth + 10f, cy), 2.dp.toPx())
            }

            // 4. Zebra Exposure Warning (Diagonal striped overlay emulation)
            if (showZebra) {
                val zebraColor = CinemaZebraYellow.copy(alpha = 0.35f)
                val step = 24.dp.toPx()
                var x = -height
                while (x < width + height) {
                    drawLine(
                        color = zebraColor,
                        start = Offset(x, 0f),
                        end = Offset(x + height, height),
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                    x += step
                }
            }

            // 5. Focus Peaking Indicator (Simulated edge highlight overlay)
            if (showFocusPeaking) {
                val peakingColor = CinemaPeakingGreen.copy(alpha = 0.5f)
                val cx = width / 2f
                val cy = height / 2f
                drawCircle(
                    color = peakingColor,
                    radius = 80.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }
        }
    }
}

@Composable
fun RealtimeHistogram(
    data: IntArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CinemaOverlayDark, RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val barW = w / data.size.toFloat()

            // Draw grid lines
            drawLine(Color(0x33FFFFFF), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1f)

            // Draw histogram curve
            val path = Path()
            path.moveTo(0f, h)

            for (i in data.indices) {
                val value = data[i].coerceIn(0, 100)
                val barH = (value / 100f) * h
                val x = i * barW
                val y = h - barH
                if (i == 0) {
                    path.lineTo(x, y)
                } else {
                    path.lineTo(x + barW / 2f, y)
                }
            }
            path.lineTo(w, h)
            path.close()

            drawPath(path, color = Color(0x9900E5FF))
            drawPath(path, color = Color(0xFF00E5FF), style = Stroke(width = 1.5f))
        }
    }
}

@Composable
fun AudioLevelMeter(
    db: Float,
    modifier: Modifier = Modifier
) {
    // db is between -60dB (silence) and 0dB (clipping)
    val normalized = ((db + 60f) / 60f).coerceIn(0f, 1f)
    val isClipping = db > -3f

    Box(
        modifier = modifier
            .background(CinemaOverlayDark, RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val activeWidth = w * normalized

            // Color gradient: Green -> Yellow -> Red
            val barColor = when {
                isClipping -> CinemaRedRecord
                normalized > 0.75f -> CinemaZebraYellow
                else -> CinemaGreenActive
            }

            drawRect(
                color = Color(0x33FFFFFF),
                size = size
            )
            drawRect(
                color = barColor,
                size = androidx.compose.ui.geometry.Size(activeWidth, h)
            )

            // -18dB and -6dB reference marks
            val mark18 = w * ((-18f + 60f) / 60f)
            val mark6 = w * ((-6f + 60f) / 60f)
            drawLine(Color.White, Offset(mark18, 0f), Offset(mark18, h), 1f)
            drawLine(Color.White, Offset(mark6, 0f), Offset(mark6, h), 1f)
        }
    }
}
