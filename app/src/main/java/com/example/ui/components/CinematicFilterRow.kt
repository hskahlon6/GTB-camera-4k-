package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CinematicFilter
import com.example.ui.theme.CinemaCyan4K
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaGoldAccent
import com.example.ui.theme.CinemaGreenActive
import com.example.ui.theme.CinemaOverlayDark
import com.example.ui.theme.CinemaPanelBorder
import com.example.ui.theme.CinemaRedRecord
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceLight
import com.example.ui.theme.CinemaWhite
import com.example.ui.theme.CinemaWhiteMuted

@Composable
fun CinematicFilterRow(
    activeFilter: CinematicFilter,
    onSelectFilter: (CinematicFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CinemaOverlayDark.copy(alpha = 0.85f))
            .padding(vertical = 6.dp)
    ) {
        // Label Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = CinemaGreenActive,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CINEMATIC 3D LUT COLOR GRADING",
                    color = CinemaWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = activeFilter.title.uppercase(),
                color = CinemaGreenActive,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Row of Filter Buttons with mini color swatch gradient preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CinematicFilter.values().forEach { filter ->
                val isSelected = activeFilter == filter
                val swatchColors = getFilterSwatchColors(filter)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CinemaSurfaceLight else CinemaSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CinemaGreenActive else CinemaPanelBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectFilter(filter) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("filter_button_${filter.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Color swatch preview disk
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(swatchColors))
                                .border(1.dp, if (isSelected) CinemaGreenActive else Color(0x66FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CinemaWhite)
                                )
                            }
                        }

                        // Filter Name
                        Text(
                            text = filter.title,
                            color = if (isSelected) CinemaGreenActive else CinemaWhiteMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun getFilterSwatchColors(filter: CinematicFilter): List<Color> {
    return when (filter) {
        CinematicFilter.NATURAL -> listOf(Color(0xFF888888), Color(0xFFDDDDDD))
        CinematicFilter.TEAL_AND_ORANGE -> listOf(Color(0xFF00A896), Color(0xFFFF8C42))
        CinematicFilter.HOLLYWOOD_FILM -> listOf(Color(0xFF1E272C), Color(0xFFFFD166))
        CinematicFilter.WARM_FILM -> listOf(Color(0xFFE76F51), Color(0xFFF4A261))
        CinematicFilter.COOL_FILM -> listOf(Color(0xFF2A9D8F), Color(0xFF457B9D))
        CinematicFilter.VINTAGE -> listOf(Color(0xFFBC6C25), Color(0xFFDDA15E))
        CinematicFilter.BLACK_AND_WHITE -> listOf(Color(0xFF111111), Color(0xFFFFFFFF))
        CinematicFilter.DOCUMENTARY -> listOf(Color(0xFF4A4E69), Color(0xFF9A8C98))
        CinematicFilter.MOODY -> listOf(Color(0xFF0D1B2A), Color(0xFF415A77))
        CinematicFilter.PORTRAIT -> listOf(Color(0xFFFFB5A7), Color(0xFFFCD5CE))
    }
}
