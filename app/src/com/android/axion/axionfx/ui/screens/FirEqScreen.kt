@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import com.android.axion.axionfx.ui.AxionFxViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.android.axion.axionfx.R
import com.android.axion.axionfx.domain.EffectKeys
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold

private const val KEY_FIR_EQ_ENABLED = "fir_eq_enabled"
private const val KEY_FIR_EQ_BAND_PREFIX = "fir_eq_band_"

private val BAND_LABELS = arrayOf(
    "25", "40", "63", "100", "160", "250", "400", "630",
    "1k", "1.6k", "2.5k", "4k", "6.3k", "10k", "16k"
)

@Composable
fun FirEqScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    var enabled by remember { mutableStateOf(viewModel.loadBoolean(KEY_FIR_EQ_ENABLED, false)) }
    val bandGains = remember {
        Array(15) {
            mutableStateOf(viewModel.loadInt("$KEY_FIR_EQ_BAND_PREFIX$it", 0).toFloat())
        }
    }

    AxionScaffold(title = stringResource(R.string.fir_eq_screen_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PreferenceGroup(title = stringResource(R.string.fir_eq_category)) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.fir_eq_enable_title),
                        summary = stringResource(R.string.fir_eq_enable_summary),
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            viewModel.interactor.setFirEqEnabled(it)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(R.string.fir_eq_plus_db),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.fir_eq_minus_db),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for (band in 0 until 15) {
                            FirBandSlider(
                                label = BAND_LABELS[band],
                                value = bandGains[band].value,
                                enabled = enabled,
                                onValueChange = { newVal ->
                                    bandGains[band].value = newVal
                                    val tenthsDb = (newVal * 10).toInt()
                                    viewModel.interactor.setFirEqBandGain(band, tenthsDb)
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                FilledTonalButton(
                    onClick = {
                        for (band in 0 until 15) {
                            bandGains[band].value = 0f
                            viewModel.interactor.setFirEqBandGain(band, 0)
                        }
                    },
                ) {
                    Icon(Icons.Rounded.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.fir_eq_flat))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FirBandSlider(
    label: String,
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.width(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${value.toInt()}",
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
                valueRange = -20f..20f,
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
