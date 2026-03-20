@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import com.android.axion.axionfx.ui.AxionFxViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.android.axion.axionfx.R
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.android.axion.axionfx.preset.PresetManager
import com.android.axion.axionfx.service.AxionFxService
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold

@Composable
fun PresetsScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    var presetNames by remember { mutableStateOf(PresetManager.listPresets()) }

    AxionScaffold(title = stringResource(R.string.presets_screen_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    onClick = {
                        val name = "Preset ${presetNames.size + 1}"
                        PresetManager.savePreset(name, viewModel.repo.prefs)
                        presetNames = PresetManager.listPresets()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.presets_save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceGroup(title = stringResource(R.string.presets_builtin_title)) {
                PresetManager.listBuiltinPresets().forEach { name ->
                    item {
                        ClickablePreference(
                            title = name,
                            summary = stringResource(R.string.presets_tap_to_load),
                            onClick = {
                                PresetManager.loadBuiltinPreset(name, viewModel.repo.prefs)
                                AxionFxService.start(context)
                                Toast.makeText(context, context.getString(R.string.preset_loaded, name), Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (presetNames.isNotEmpty()) {
                PreferenceGroup(title = stringResource(R.string.presets_saved_title)) {
                    presetNames.forEach { name ->
                        item {
                            ClickablePreference(
                                title = name,
                                summary = stringResource(R.string.presets_tap_to_load),
                                onClick = {
                                    PresetManager.loadPreset(name, viewModel.repo.prefs)
                                    AxionFxService.start(context)
                                    Toast.makeText(context, context.getString(R.string.preset_loaded, name), Toast.LENGTH_SHORT).show()
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
