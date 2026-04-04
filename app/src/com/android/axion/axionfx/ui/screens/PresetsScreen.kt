@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.android.axion.axionfx.R
import com.android.axion.axionfx.preset.PresetManager
import com.android.axion.axionfx.service.AxionFxService
import com.android.axion.axionfx.ui.AxionFxViewModel
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PresetsScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    var presetNames by remember { mutableStateOf(PresetManager.listPresets(context)) }
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<String?>(null) }
    var presetToRename by remember { mutableStateOf<String?>(null) }
    var presetToExport by remember { mutableStateOf<String?>(null) }
    var newPresetName by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                var fileName: String? = null
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }

                val name = fileName?.substringBeforeLast(".") 
                    ?: "Imported ${SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())}"

                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val json = reader.readText()
                    PresetManager.importPreset(context, name, json)
                    presetNames = PresetManager.listPresets(context)
                    Toast.makeText(context, context.getString(R.string.presets_import_success), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.presets_import_failed, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            try {
                presetToExport?.let { name ->
                    val json = PresetManager.exportPreset(context, name)
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(json?.toByteArray())
                        Toast.makeText(context, context.getString(R.string.presets_export_success), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.presets_export_failed, e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showSaveDialog) {
        var saveName by remember { mutableStateOf("My Preset ${presetNames.size + 1}") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.presets_save_title)) },
            text = {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    label = { Text(stringResource(R.string.presets_save_label)) },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    PresetManager.savePreset(context, saveName, viewModel.repo.prefs)
                    presetNames = PresetManager.listPresets(context)
                    showSaveDialog = false
                    Toast.makeText(context, context.getString(R.string.presets_save_success, saveName), Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.presets_confirm_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text(stringResource(R.string.presets_confirm_cancel)) }
            }
        )
    }

    if (presetToRename != null) {
        AlertDialog(
            onDismissRequest = { presetToRename = null },
            title = { Text(stringResource(R.string.presets_rename_title)) },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text(stringResource(R.string.presets_rename_label)) },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    PresetManager.renamePreset(context, presetToRename!!, newPresetName)
                    presetNames = PresetManager.listPresets(context)
                    presetToRename = null
                }) { Text(stringResource(R.string.presets_confirm_rename)) }
            },
            dismissButton = {
                TextButton(onClick = { presetToRename = null }) { Text(stringResource(R.string.presets_confirm_cancel)) }
            }
        )
    }

    if (presetToDelete != null) {
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text(stringResource(R.string.presets_delete_title)) },
            text = { Text(stringResource(R.string.presets_delete_message, presetToDelete!!)) },
            confirmButton = {
                TextButton(onClick = {
                    PresetManager.deletePreset(context, presetToDelete!!)
                    presetNames = PresetManager.listPresets(context)
                    presetToDelete = null
                }) { Text(stringResource(R.string.presets_confirm_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) { Text(stringResource(R.string.presets_confirm_cancel)) }
            }
        )
    }

    AxionScaffold(title = stringResource(R.string.presets_screen_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FilledTonalButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.presets_import))
                }
                
                FilledTonalButton(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.presets_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PreferenceGroup(title = stringResource(R.string.presets_builtin_title)) {
                PresetManager.listBuiltinPresets().forEach { name ->
                    item {
                        ListItem(
                            headlineContent = { Text(name) },
                            supportingContent = { Text(stringResource(R.string.presets_tap_to_load)) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    PresetManager.loadBuiltinPreset(name, viewModel.repo.prefs)
                                    AxionFxService.instance?.restoreSettings()
                                    Toast.makeText(context, context.getString(R.string.preset_loaded, name), Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (presetNames.isNotEmpty()) {
                PreferenceGroup(title = stringResource(R.string.presets_saved_title)) {
                    presetNames.forEach { name ->
                        item {
                            PresetListItem(
                                name = name,
                                onLoad = {
                                    PresetManager.loadPreset(context, name, viewModel.repo.prefs)
                                    AxionFxService.instance?.restoreSettings()
                                    Toast.makeText(context, context.getString(R.string.preset_loaded, name), Toast.LENGTH_SHORT).show()
                                },
                                onRename = {
                                    newPresetName = name
                                    presetToRename = name
                                },
                                onDelete = { presetToDelete = name },
                                onExport = {
                                    presetToExport = name
                                    exportLauncher.launch("$name.json")
                                },
                                onShare = {
                                    val file = PresetManager.getPresetFile(context, name)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "com.android.axion.axionfx.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.presets_share_subject, name))
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.presets_share)))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PresetListItem(
    name: String,
    onLoad: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(name) },
        supportingContent = { Text(stringResource(R.string.presets_tap_to_load)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        trailingContent = {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.presets_share)) },
                        onClick = { expanded = false; onShare() },
                        leadingIcon = { Icon(Icons.Rounded.Share, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.presets_export)) },
                        onClick = { expanded = false; onExport() },
                        leadingIcon = { Icon(Icons.Rounded.Storage, null) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.presets_rename)) },
                        onClick = { expanded = false; onRename() },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.presets_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = { expanded = false; onDelete() },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onLoad)
    )
}
