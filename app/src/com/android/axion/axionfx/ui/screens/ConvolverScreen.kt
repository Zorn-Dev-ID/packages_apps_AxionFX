@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import com.android.axion.axionfx.ui.AxionFxViewModel
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.android.axion.axionfx.R
import com.android.axion.axionfx.domain.EffectDefaults
import com.android.axion.axionfx.domain.EffectKeys
import com.android.axion.axionfx.ui.components.EffectSlider
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold
import android.provider.OpenableColumns
import java.io.File

@Composable
fun ConvolverScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    var enabled by remember { mutableStateOf(viewModel.loadBoolean(EffectKeys.CONVOLVER_ENABLED, EffectDefaults.CONVOLVER_ENABLED)) }
    var mix by remember { mutableFloatStateOf(viewModel.loadInt(EffectKeys.CONVOLVER_MIX, EffectDefaults.CONVOLVER_MIX).toFloat()) }
    val irPath = remember { mutableStateOf(viewModel.repo.getString(EffectKeys.CONVOLVER_IR_PATH, null)) }
    val irName = remember { mutableStateOf(viewModel.repo.getString(EffectKeys.CONVOLVER_IR_NAME, null)) }

    val irLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val irDir = File(context.filesDir, "convolver")
        irDir.mkdirs()
        val destFile = File(irDir, "ir.wav")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            var displayName: String? = null
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    displayName = cursor.getString(0)
                }
            }
            val path = destFile.absolutePath
            irPath.value = path
            irName.value = displayName
            viewModel.repo.putString(EffectKeys.CONVOLVER_IR_NAME, displayName)
            viewModel.interactor.loadConvolverIr(path)
        } catch (_: Exception) {}
    }

    AxionScaffold(title = stringResource(R.string.convolver_screen_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PreferenceGroup(title = stringResource(R.string.convolver_category)) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.convolver_enable_title),
                        summary = stringResource(R.string.convolver_enable_summary),
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            viewModel.interactor.setConvolverEnabled(it)
                        },
                    )
                }
                item {
                    ClickablePreference(
                        title = stringResource(R.string.convolver_load_ir),
                        summary = irName.value ?: irPath.value ?: stringResource(R.string.convolver_no_ir),
                        onClick = {
                            irLauncher.launch(arrayOf("audio/x-wav", "audio/*", "*/*"))
                        },
                        enabled = enabled,
                    )
                }
                item {
                    EffectSlider(
                        title = stringResource(R.string.convolver_mix_title),
                        summary = stringResource(R.string.convolver_mix_summary),
                        value = mix,
                        valueRange = 0f..100f,
                        unit = "%",
                        enabled = enabled,
                        onValueChange = {
                            mix = it
                            viewModel.interactor.setConvolverMix(it.toInt())
                        },
                        onReset = {
                            mix = EffectDefaults.CONVOLVER_MIX.toFloat()
                            viewModel.interactor.setConvolverMix(EffectDefaults.CONVOLVER_MIX)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.convolver_formats),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
