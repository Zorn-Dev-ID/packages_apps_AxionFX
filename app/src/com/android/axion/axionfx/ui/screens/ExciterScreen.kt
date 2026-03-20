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

private const val KEY_EXCITER_ENABLED = "exciter_enabled"
private const val KEY_EXCITER_DRIVE = "exciter_drive"
private const val KEY_EXCITER_BLEND = "exciter_blend"
private const val KEY_EXCITER_FREQ = "exciter_freq"

@Composable
fun ExciterScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    var enabled by remember { mutableStateOf(viewModel.loadBoolean(KEY_EXCITER_ENABLED, false)) }
    var drive by remember { mutableFloatStateOf(viewModel.loadInt(KEY_EXCITER_DRIVE, 50).toFloat()) }
    var blend by remember { mutableFloatStateOf(viewModel.loadInt(KEY_EXCITER_BLEND, 30).toFloat()) }
    var freq by remember { mutableFloatStateOf(viewModel.loadInt(KEY_EXCITER_FREQ, 3000).toFloat()) }

    AxionScaffold(title = stringResource(R.string.exciter_screen_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PreferenceGroup(title = stringResource(R.string.exciter_category)) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.exciter_enable_title),
                        summary = stringResource(R.string.exciter_enable_summary),
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            viewModel.interactor.setExciterEnabled(it)
                        },
                    )
                }
                item {
                    EffectSlider(
                        title = stringResource(R.string.exciter_drive_title),
                        summary = stringResource(R.string.exciter_drive_summary),
                        value = drive,
                        valueRange = 0f..100f,
                        unit = "%",
                        enabled = enabled,
                        onValueChange = {
                            drive = it
                            viewModel.interactor.setExciterDrive(it.toInt())
                        },
                    )
                }
                item {
                    EffectSlider(
                        title = stringResource(R.string.exciter_blend_title),
                        summary = stringResource(R.string.exciter_blend_summary),
                        value = blend,
                        valueRange = 0f..100f,
                        unit = "%",
                        enabled = enabled,
                        onValueChange = {
                            blend = it
                            viewModel.interactor.setExciterBlend(it.toInt())
                        },
                    )
                }
                item {
                    EffectSlider(
                        title = stringResource(R.string.exciter_freq_title),
                        summary = stringResource(R.string.exciter_freq_summary),
                        value = freq,
                        valueRange = 500f..10000f,
                        unit = "Hz",
                        enabled = enabled,
                        onValueChange = {
                            freq = it
                            viewModel.interactor.setExciterFreq(it.toInt())
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
