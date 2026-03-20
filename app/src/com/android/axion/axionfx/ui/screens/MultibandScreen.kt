@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import com.android.axion.axionfx.ui.AxionFxViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.android.axion.axionfx.R
import com.android.axion.axionfx.domain.EffectKeys
import com.android.axion.axionfx.ui.components.EffectSlider
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold

private const val KEY_MCOMP_ENABLED = "mcomp_enabled"

private val BAND_NAME_RES_IDS = intArrayOf(
    R.string.mcomp_band_low,
    R.string.mcomp_band_low_mid,
    R.string.mcomp_band_high_mid,
    R.string.mcomp_band_high,
)
private val BAND_RANGE_RES_IDS = intArrayOf(
    R.string.mcomp_range_low,
    R.string.mcomp_range_low_mid,
    R.string.mcomp_range_high_mid,
    R.string.mcomp_range_high,
)

@Composable
fun MultibandScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    var enabled by remember { mutableStateOf(viewModel.loadBoolean(KEY_MCOMP_ENABLED, false)) }

    val thresholds = remember {
        Array(4) { mutableFloatStateOf(viewModel.loadInt("mcomp_thresh_$it", -200).toFloat()) }
    }
    val ratios = remember {
        Array(4) { mutableFloatStateOf(viewModel.loadInt("mcomp_ratio_$it", 400).toFloat()) }
    }
    val makeups = remember {
        Array(4) { mutableFloatStateOf(viewModel.loadInt("mcomp_makeup_$it", 0).toFloat()) }
    }

    AxionScaffold(title = stringResource(R.string.mcomp_screen_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PreferenceGroup(title = stringResource(R.string.mcomp_category)) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.mcomp_enable_title),
                        summary = stringResource(R.string.mcomp_enable_summary),
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            viewModel.interactor.setMCompEnabled(it)
                        },
                    )
                }
            }

            for (band in 0 until 4) {
                Spacer(modifier = Modifier.height(16.dp))

                PreferenceGroup(title = "${stringResource(BAND_NAME_RES_IDS[band])} (${stringResource(BAND_RANGE_RES_IDS[band])})") {
                    item {
                        EffectSlider(
                            title = stringResource(R.string.mcomp_threshold_title),
                            summary = stringResource(R.string.mcomp_threshold_summary),
                            value = thresholds[band].floatValue,
                            valueRange = -600f..0f,
                            unit = "dB/10",
                            enabled = enabled,
                            onValueChange = {
                                thresholds[band].floatValue = it
                                viewModel.interactor.setMCompBandThreshold(band, it.toInt())
                            },
                        )
                    }
                    item {
                        EffectSlider(
                            title = stringResource(R.string.mcomp_ratio_title),
                            summary = stringResource(R.string.mcomp_ratio_summary),
                            value = ratios[band].floatValue,
                            valueRange = 100f..2000f,
                            unit = "/100",
                            enabled = enabled,
                            onValueChange = {
                                ratios[band].floatValue = it
                                viewModel.interactor.setMCompBandRatio(band, it.toInt())
                            },
                        )
                    }
                    item {
                        EffectSlider(
                            title = stringResource(R.string.mcomp_makeup_title),
                            summary = stringResource(R.string.mcomp_makeup_summary),
                            value = makeups[band].floatValue,
                            valueRange = -200f..200f,
                            unit = "dB/10",
                            enabled = enabled,
                            onValueChange = {
                                makeups[band].floatValue = it
                                viewModel.interactor.setMCompBandMakeup(band, it.toInt())
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
