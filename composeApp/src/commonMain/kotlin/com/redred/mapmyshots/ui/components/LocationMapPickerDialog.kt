package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import com.redred.mapmyshots.ui.theme.MapMyShotsShapes
import com.redred.mapmyshots.ui.theme.MapMyShotsSpacing
import com.redred.mapmyshots.ui.theme.MapMyShotsTypography
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply
import mapmyshots.composeapp.generated.resources.cancel
import mapmyshots.composeapp.generated.resources.choose_location_coordinates
import mapmyshots.composeapp.generated.resources.choose_location_hint
import mapmyshots.composeapp.generated.resources.choose_location_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

internal data class LocationMapCandidate(
    val label: String,
    val lat: Double,
    val lon: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LocationMapPickerDialog(
    initialLocation: Pair<Double, Double>,
    candidates: List<LocationMapCandidate>,
    onDismiss: () -> Unit,
    onApply: (lat: Double, lon: Double) -> Unit
) {
    var selected by remember(initialLocation) { mutableStateOf(initialLocation) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MapMyShotsShapes.metadataCard,
            color = MapMyShotsColors.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(MapMyShotsSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xl)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xs)) {
                    Text(
                        text = stringResource(Res.string.choose_location_title),
                        color = MapMyShotsColors.textPrimary,
                        fontSize = MapMyShotsTypography.sectionTitle,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.choose_location_hint),
                        color = MapMyShotsColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                InteractiveLocationMap(
                    selected = selected,
                    candidates = candidates,
                    onSelected = { selected = it }
                )

                Text(
                    text = stringResource(
                        Res.string.choose_location_coordinates,
                        selected.first,
                        selected.second
                    ),
                    color = MapMyShotsColors.textValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (candidates.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.sm)
                    ) {
                        candidates.forEach { candidate ->
                            TextButton(onClick = { selected = candidate.lat to candidate.lon }) {
                                Text(candidate.label)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(onClick = { onApply(selected.first, selected.second) }) {
                        Text(stringResource(Res.string.apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveLocationMap(
    selected: Pair<Double, Double>,
    candidates: List<LocationMapCandidate>,
    onSelected: (Pair<Double, Double>) -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(MapMyShotsShapes.hero)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFDCEFE8), Color(0xFFEAF2FA))
                )
            )
            .border(1.dp, MapMyShotsColors.divider, MapMyShotsShapes.hero)
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { 260.dp.toPx() }
        val markerSizePx = with(density) { 34.dp.toPx() }
        val marker = selected.toMapOffset(widthPx, heightPx)

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onSelected(offset.toLatLon(size.width.toFloat(), size.height.toFloat()))
                    }
                }
        ) {
            val gridColor = Color.White.copy(alpha = 0.68f)
            val landColor = Color(0xFF8ECFB8).copy(alpha = 0.78f)
            val roadColor = Color.White.copy(alpha = 0.9f)

            for (i in 1..4) {
                val x = size.width * i / 5f
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            }
            for (i in 1..3) {
                val y = size.height * i / 4f
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }

            drawOval(
                color = landColor,
                topLeft = Offset(size.width * 0.08f, size.height * 0.18f),
                size = Size(size.width * 0.34f, size.height * 0.24f)
            )
            drawOval(
                color = landColor,
                topLeft = Offset(size.width * 0.48f, size.height * 0.16f),
                size = Size(size.width * 0.38f, size.height * 0.28f)
            )
            drawOval(
                color = landColor,
                topLeft = Offset(size.width * 0.26f, size.height * 0.55f),
                size = Size(size.width * 0.48f, size.height * 0.25f)
            )

            val route = Path().apply {
                moveTo(size.width * 0.04f, size.height * 0.70f)
                cubicTo(size.width * 0.30f, size.height * 0.42f, size.width * 0.44f, size.height * 0.86f, size.width * 0.96f, size.height * 0.30f)
            }
            drawPath(route, roadColor, style = Stroke(width = 8.dp.toPx()))
            drawPath(route, Color(0xFF76A9D6).copy(alpha = 0.38f), style = Stroke(width = 2.dp.toPx()))

            candidates.forEach { candidate ->
                val point = (candidate.lat to candidate.lon).toMapOffset(size.width, size.height)
                drawCircle(
                    color = MapMyShotsColors.primary.copy(alpha = 0.18f),
                    radius = 13.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = MapMyShotsColors.primary,
                    radius = 5.dp.toPx(),
                    center = point
                )
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (marker.x - markerSizePx / 2f).roundToInt(),
                        (marker.y - markerSizePx).roundToInt()
                    )
                }
                .size(34.dp)
                .clip(CircleShape)
                .background(MapMyShotsColors.primary),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun Pair<Double, Double>.toMapOffset(width: Float, height: Float): Offset {
    val x = (((second + 180.0) / 360.0).coerceIn(0.0, 1.0) * width).toFloat()
    val y = (((90.0 - first) / 180.0).coerceIn(0.0, 1.0) * height).toFloat()
    return Offset(x, y)
}

private fun Offset.toLatLon(width: Float, height: Float): Pair<Double, Double> {
    val lon = ((x / width).coerceIn(0f, 1f) * 360f - 180f).toDouble()
    val lat = (90f - (y / height).coerceIn(0f, 1f) * 180f).toDouble()
    return lat to lon
}
