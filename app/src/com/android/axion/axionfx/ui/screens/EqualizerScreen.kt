@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import com.android.axion.axionfx.ui.AxionFxViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.android.axion.axionfx.R
import com.android.axion.axionfx.domain.EffectKeys
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val KEY_EQ_ENABLED = "eq_enabled"
private const val KEY_EQ_BAND_PREFIX = "eq_band_"

private val BAND_LABELS = arrayOf(
    "31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"
)

private data class EqPreset(val nameResId: Int, val bands: IntArray)

private val PRESETS = listOf(
    EqPreset(R.string.eq_preset_flat, intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
    EqPreset(R.string.eq_preset_bass_boost, intArrayOf(500, 400, 300, 100, 0, 0, 0, 0, 0, 0)),
    EqPreset(R.string.eq_preset_treble_boost, intArrayOf(0, 0, 0, 0, 0, 0, 100, 300, 400, 500)),
    EqPreset(R.string.eq_preset_voice, intArrayOf(-200, -100, 0, 200, 400, 400, 200, 0, -100, -200)),
    EqPreset(R.string.eq_preset_v_shape, intArrayOf(400, 300, 100, 0, -200, -200, 0, 100, 300, 400)),
    EqPreset(R.string.eq_preset_rock, intArrayOf(300, 200, 0, -100, -200, 0, 200, 300, 400, 400)),
)

@Composable
fun EqualizerScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    var enabled by remember { mutableStateOf(viewModel.loadBoolean(KEY_EQ_ENABLED, false)) }
    var mode by remember { mutableIntStateOf(0) }
    val bandGains = remember {
        Array(10) { mutableFloatStateOf(viewModel.loadInt("$KEY_EQ_BAND_PREFIX$it", 0).toFloat()) }
    }

    fun applyBands() {
        for (i in 0..9) {
            val v = bandGains[i].floatValue.toInt()
            viewModel.interactor.setEqBandLevel(i, v)
        }
    }

    AxionScaffold(title = stringResource(R.string.nav_equalizer), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreferenceGroup(title = stringResource(R.string.eq_category)) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.eq_enable_title),
                        summary = stringResource(R.string.eq_enable_summary),
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            viewModel.interactor.setEqEnabled(it)
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { mode = 0 },
                    modifier = Modifier.weight(1f),
                    colors = if (mode == 0) ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) else ButtonDefaults.filledTonalButtonColors(),
                ) { Text(stringResource(R.string.eq_simple)) }
                FilledTonalButton(
                    onClick = { mode = 1 },
                    modifier = Modifier.weight(1f),
                    colors = if (mode == 1) ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) else ButtonDefaults.filledTonalButtonColors(),
                ) { Text(stringResource(R.string.eq_advanced)) }
            }

            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut()).using(SizeTransform(clip = false))
                },
                label = "eqMode",
            ) { currentMode ->
                when (currentMode) {
                    0 -> SimpleEqMode(bandGains, enabled) { applyBands() }
                    else -> AdvancedEqMode(
                        bandGains = bandGains,
                        enabled = enabled,
                        onBandChange = { band, value ->
                            bandGains[band].floatValue = value
                            viewModel.interactor.setEqBandLevel(band, value.toInt())
                        },
                        onReset = {
                            for (i in 0..9) bandGains[i].floatValue = 0f
                            applyBands()
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SimpleEqMode(
    bandGains: Array<MutableFloatState>,
    enabled: Boolean,
    onApply: () -> Unit,
) {
    var bass by remember { mutableFloatStateOf(0f) }
    var mid by remember { mutableFloatStateOf(0f) }
    var treble by remember { mutableFloatStateOf(0f) }

    fun syncFromBands() {
        bass = (bandGains[0].floatValue + bandGains[1].floatValue + bandGains[2].floatValue) / 3f / 50f
        mid = (bandGains[3].floatValue + bandGains[4].floatValue + bandGains[5].floatValue + bandGains[6].floatValue) / 4f / 50f
        treble = (bandGains[7].floatValue + bandGains[8].floatValue + bandGains[9].floatValue) / 3f / 50f
    }

    fun applyTriangle() {
        val bv = (bass * 50f).toInt().coerceIn(-600, 600)
        val mv = (mid * 50f).toInt().coerceIn(-600, 600)
        val tv = (treble * 50f).toInt().coerceIn(-600, 600)
        bandGains[0].floatValue = bv * 1.2f
        bandGains[1].floatValue = bv.toFloat()
        bandGains[2].floatValue = bv * 0.8f
        bandGains[3].floatValue = bv * 0.3f + mv * 0.7f
        bandGains[4].floatValue = mv.toFloat()
        bandGains[5].floatValue = mv.toFloat()
        bandGains[6].floatValue = mv * 0.7f + tv * 0.3f
        bandGains[7].floatValue = tv * 0.8f
        bandGains[8].floatValue = tv.toFloat()
        bandGains[9].floatValue = tv * 1.2f
        onApply()
    }

    remember { syncFromBands(); true }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularEqControl(
            bass = bass, mid = mid, treble = treble,
            enabled = enabled,
            onBassChange = { bass = it; applyTriangle() },
            onMidChange = { mid = it; applyTriangle() },
            onTrebleChange = { treble = it; applyTriangle() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .aspectRatio(1f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PRESETS.take(3).forEach { preset ->
                    FilledTonalButton(
                        onClick = {
                            for (i in 0..9) bandGains[i].floatValue = preset.bands[i].toFloat()
                            syncFromBands()
                            onApply()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                    ) { Text(stringResource(preset.nameResId), style = MaterialTheme.typography.labelMedium) }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PRESETS.drop(3).forEach { preset ->
                    FilledTonalButton(
                        onClick = {
                            for (i in 0..9) bandGains[i].floatValue = preset.bands[i].toFloat()
                            syncFromBands()
                            onApply()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                    ) { Text(stringResource(preset.nameResId), style = MaterialTheme.typography.labelMedium) }
                }
            }
        }
    }
}

@Composable
private fun CircularEqControl(
    bass: Float, mid: Float, treble: Float,
    enabled: Boolean,
    onBassChange: (Float) -> Unit,
    onMidChange: (Float) -> Unit,
    onTrebleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val outline = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceBright = MaterialTheme.colorScheme.surfaceBright
    val labelStyle = TextStyle(fontSize = 11.sp)
    val valueStyle = TextStyle(fontSize = 13.sp)

    val labelMid = stringResource(R.string.eq_label_mid)
    val labelBass = stringResource(R.string.eq_label_bass)
    val labelTreble = stringResource(R.string.eq_label_treble)

    var activeAxis by remember { mutableIntStateOf(-1) }
    val currentBassChange by rememberUpdatedState(onBassChange)
    val currentMidChange by rememberUpdatedState(onMidChange)
    val currentTrebleChange by rememberUpdatedState(onTrebleChange)

    val angles = remember { doubleArrayOf(-PI / 2, -PI / 2 + 2 * PI / 3, -PI / 2 + 4 * PI / 3) }

    fun calcValueForAxis(pos: Offset, w: Int, h: Int, axisIdx: Int): Float {
        val cx = w / 2f
        val cy = h / 2f
        val baseR = w / 2f * 0.35f
        val angle = angles[axisIdx]
        val axDx = cos(angle).toFloat()
        val axDy = sin(angle).toFloat()
        val dot = (pos.x - cx) * axDx + (pos.y - cy) * axDy
        return ((dot / baseR - 1f) / 0.8f * 10f).coerceIn(-10f, 10f)
    }

    fun findNearestAxis(pos: Offset, w: Int, h: Int): Int {
        val cx = w / 2f
        val cy = h / 2f
        val touchAngle = atan2((pos.y - cy).toDouble(), (pos.x - cx).toDouble())
        var bestIdx = 0
        var bestDiff = Double.MAX_VALUE
        for (i in 0..2) {
            val diff = atan2(sin(touchAngle - angles[i]), cos(touchAngle - angles[i]))
            val absDiff = kotlin.math.abs(diff)
            if (absDiff < bestDiff) { bestDiff = absDiff; bestIdx = i }
        }
        val dist = kotlin.math.hypot((pos.x - cx).toDouble(), (pos.y - cy).toDouble())
        return if (dist < w * 0.48f) bestIdx else -1
    }

    fun dispatchToAxis(axisIdx: Int, value: Float) {
        when (axisIdx) {
            0 -> currentMidChange(value)
            1 -> currentBassChange(value)
            2 -> currentTrebleChange(value)
        }
    }

    fun dispatchDiagonal(pos: Offset, w: Int, h: Int) {
        val cx = w / 2f
        val cy = h / 2f
        val touchAngle = atan2((pos.y - cy).toDouble(), (pos.x - cx).toDouble())
        for (i in 0..2) {
            val diff = atan2(sin(touchAngle - angles[i]), cos(touchAngle - angles[i]))
            val absDiff = kotlin.math.abs(diff)
            val weight = cos(absDiff * 1.5).toFloat().coerceAtLeast(0f)
            if (weight > 0.15f) {
                val v = calcValueForAxis(pos, w, h, i) * weight
                dispatchToAxis(i, v.coerceIn(-10f, 10f))
            }
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val axis = findNearestAxis(down.position, size.width, size.height)
                    if (axis < 0) return@awaitEachGesture
                    down.consume()

                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val touchAngle = atan2((down.position.y - cy).toDouble(), (down.position.x - cx).toDouble())
                    val nearestDiff = kotlin.math.abs(atan2(sin(touchAngle - angles[axis]), cos(touchAngle - angles[axis])))
                    val isDiagonal = nearestDiff > PI / 6

                    if (isDiagonal) {
                        activeAxis = -1
                        dispatchDiagonal(down.position, size.width, size.height)
                    } else {
                        activeAxis = axis
                        val v = calcValueForAxis(down.position, size.width, size.height, axis)
                        dispatchToAxis(axis, v)
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        change.consume()
                        if (isDiagonal) {
                            dispatchDiagonal(change.position, size.width, size.height)
                        } else {
                            val v = calcValueForAxis(change.position, size.width, size.height, axis)
                            dispatchToAxis(axis, v)
                        }
                    }
                    activeAxis = -1
                }
            }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.width / 2f * 0.9f
        val baseR = size.width / 2f * 0.35f

        drawCircle(surfaceBright, outerR, Offset(cx, cy))
        drawCircle(outline.copy(alpha = 0.15f), outerR, Offset(cx, cy), style = Stroke(1.5f))

        val values = floatArrayOf(mid, bass, treble)
        val labels = arrayOf(labelMid, labelBass, labelTreble)

        for (i in 0..2) {
            val ex = cx + outerR * 0.88f * cos(angles[i]).toFloat()
            val ey = cy + outerR * 0.88f * sin(angles[i]).toFloat()
            drawLine(outline.copy(alpha = 0.12f), Offset(cx, cy), Offset(ex, ey), strokeWidth = 1f)

            for (dot in 1..7) {
                val dr = baseR * (0.3f + dot * 0.2f)
                val dx = cx + dr * cos(angles[i]).toFloat()
                val dy = cy + dr * sin(angles[i]).toFloat()
                drawCircle(outline.copy(alpha = 0.4f), 3.5f, Offset(dx, dy))
            }
        }

        drawCircle(outline.copy(alpha = 0.06f), baseR, Offset(cx, cy), style = Stroke(0.8f))

        val segments = 72
        val wavePath = Path()
        for (s in 0..segments) {
            val t = s.toFloat() / segments
            val segAngle = t * 2.0 * PI - PI / 2

            var r = baseR
            for (i in 0..2) {
                val norm = (values[i] / 10f).coerceIn(-1f, 1f)
                val influence = baseR * norm * 0.8f
                val diff = segAngle - angles[i]
                val wrap = atan2(sin(diff), cos(diff))
                val w = cos(wrap * 0.75).toFloat().coerceAtLeast(0f)
                r += influence * w * w
            }

            val px = cx + r * cos(segAngle).toFloat()
            val py = cy + r * sin(segAngle).toFloat()
            if (s == 0) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
        }
        wavePath.close()

        drawPath(wavePath, primaryContainer.copy(alpha = 0.12f))
        drawPath(wavePath, primary.copy(alpha = 0.4f), style = Stroke(2f, cap = StrokeCap.Round))

        val points = Array(3) { i ->
            val norm = (values[i] / 10f).coerceIn(-1f, 1f)
            val r = baseR * (1f + norm * 0.8f)
            Offset(cx + r * cos(angles[i]).toFloat(), cy + r * sin(angles[i]).toFloat())
        }

        for (i in 0..2) {
            val isActive = (activeAxis == i)
            drawCircle(primary.copy(alpha = if (isActive) 0.2f else 0.1f), if (isActive) 32f else 26f, points[i])
            drawCircle(if (isActive) primary else onSurface, if (isActive) 18f else 14f, points[i])
        }

        for (i in 0..2) {
            val sign = if (values[i] >= 0) "+" else ""
            val label = textMeasurer.measure(labels[i], labelStyle)
            val value = textMeasurer.measure("${sign}${values[i].toInt()}", valueStyle)
            val labelR = outerR * 0.78f
            val lx = cx + labelR * cos(angles[i]).toFloat()
            val ly = cy + labelR * sin(angles[i]).toFloat()

            drawText(label, onSurface.copy(alpha = 0.6f), Offset(lx - label.size.width / 2f, ly - label.size.height - 2f))
            drawText(value, onSurface, Offset(lx - value.size.width / 2f, ly + 2f))
        }
    }
}

@Composable
private fun AdvancedEqMode(
    bandGains: Array<MutableFloatState>,
    enabled: Boolean,
    onBandChange: (Int, Float) -> Unit,
    onReset: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (band in 0..9) {
                    EqBandSlider(
                        label = BAND_LABELS[band],
                        value = bandGains[band].floatValue,
                        enabled = enabled,
                        onValueChange = { onBandChange(band, it) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            FilledTonalButton(onClick = onReset) {
                Icon(Icons.Rounded.Replay, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.eq_reset))
            }
        }
    }
}

@Composable
private fun EqBandSlider(
    label: String,
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.width(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "%.1f".format(value / 100f),
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = -600f..600f,
                enabled = enabled,
                modifier = Modifier
                    .width(200.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = constraints.minHeight,
                                maxWidth = constraints.maxHeight,
                            )
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                -placeable.width / 2 + placeable.height / 2,
                                placeable.width / 2 - placeable.height / 2
                            )
                        }
                    }
                    .graphicsLayer { rotationZ = -90f },
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
