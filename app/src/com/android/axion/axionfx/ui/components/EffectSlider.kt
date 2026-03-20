package com.android.axion.axionfx.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.axion.compose.preferences.LocalPreferencePosition
import com.android.axion.compose.preferences.PreferencePosition
import com.android.axion.compose.preferences.SliderPreference

@Composable
fun EffectSlider(
    title: String,
    summary: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    unit: String = "",
    position: PreferencePosition = LocalPreferencePosition.current,
    onReset: (() -> Unit)? = null,
) {
    val displayValue = if (unit.isNotEmpty()) {
        "${value.toInt()} $unit"
    } else {
        value.toInt().toString()
    }

    SliderPreference(
        title = title,
        summary = summary,
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = {},
        valueRange = valueRange,
        displayValue = displayValue,
        modifier = modifier,
        enabled = enabled,
        position = position,
        onReset = onReset,
    )
}
