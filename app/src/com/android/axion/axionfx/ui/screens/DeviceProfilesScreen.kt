/*
 * Copyright 2025-2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.axion.axionfx.R
import com.android.axion.axionfx.device.DeviceCategory
import com.android.axion.axionfx.device.DeviceProfile
import com.android.axion.axionfx.device.DeviceProfileManager
import com.android.axion.axionfx.preset.PresetManager
import com.android.axion.axionfx.service.AxionFxService
import com.android.axion.axionfx.ui.AxionFxViewModel
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold
import kotlinx.coroutines.launch

@Composable
fun DeviceProfilesScreen(viewModel: AxionFxViewModel, onBackClick: () -> Unit) {
    BackHandler(onBack = onBackClick)

    val context = LocalContext.current
    val prefs = viewModel.repo.prefs

    var profiles by remember { mutableStateOf(DeviceProfileManager.listProfiles(prefs)) }
    var bindingsVersion by remember { mutableIntStateOf(0) }
    var pickerFor by remember { mutableStateOf<DeviceProfile?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var profileToRename by remember { mutableStateOf<DeviceProfile.User?>(null) }
    var profileToDelete by remember { mutableStateOf<DeviceProfile.User?>(null) }

    val pagerState = rememberPagerState(pageCount = { profiles.size + 1 })
    val scope = rememberCoroutineScope()

    val builtinNames = remember { PresetManager.listBuiltinPresets() }
    val userPresetNames = remember { PresetManager.listPresets(context) }

    fun refreshProfiles() {
        profiles = DeviceProfileManager.listProfiles(prefs)
        bindingsVersion++
    }

    pickerFor?.let { profile ->
        PresetPickerDialog(
            profile = profile,
            current = DeviceProfileManager.getBinding(prefs, profile),
            builtinNames = builtinNames,
            userPresetNames = userPresetNames,
            onDismiss = { pickerFor = null },
            onSelect = { token ->
                DeviceProfileManager.setBinding(prefs, profile, token)
                refreshProfiles()
                pickerFor = null
            },
        )
    }

    if (showAddDialog) {
        AddProfileDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                val created = DeviceProfileManager.addUserProfile(prefs, name)
                if (created != null) {
                    refreshProfiles()
                    showAddDialog = false
                    scope.launch { pagerState.animateScrollToPage(profiles.indexOf(created).coerceAtLeast(0)) }
                } else {
                    Toast.makeText(context, context.getString(R.string.device_profiles_add_invalid), Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    profileToRename?.let { profile ->
        RenameProfileDialog(
            initial = profile.name,
            onDismiss = { profileToRename = null },
            onConfirm = { newName ->
                val ok = DeviceProfileManager.renameUserProfile(prefs, profile.name, newName)
                if (ok) {
                    refreshProfiles()
                    profileToRename = null
                } else {
                    Toast.makeText(context, context.getString(R.string.device_profiles_add_invalid), Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text(stringResource(R.string.device_profiles_delete_title)) },
            text = { Text(stringResource(R.string.device_profiles_delete_message, profile.name)) },
            confirmButton = {
                TextButton(onClick = {
                    DeviceProfileManager.removeUserProfile(prefs, profile.name)
                    refreshProfiles()
                    profileToDelete = null
                }) {
                    Text(
                        stringResource(R.string.presets_confirm_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text(stringResource(R.string.presets_confirm_cancel))
                }
            },
        )
    }

    val autoSwitchEnabled by AxionFxService.autoSwitchEnabledFlow.collectAsState()
    LaunchedEffect(Unit) { AxionFxService.primeFromContext(context) }

    AxionScaffold(title = stringResource(R.string.device_profiles_title), onBackClick = onBackClick) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            PreferenceGroup(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.device_profiles_auto_switch_title),
                        summary = stringResource(R.string.device_profiles_auto_switch_summary),
                        checked = autoSwitchEnabled,
                        onCheckedChange = { enabled ->
                            val svc = AxionFxService.instance
                            if (svc != null) {
                                svc.setAutoSwitchEnabled(enabled)
                            } else {
                                prefs.edit().putBoolean(AxionFxService.KEY_AUTO_SWITCH, enabled).apply()
                                AxionFxService.primeFromContext(context)
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.device_profiles_swipe_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                if (page == profiles.size) {
                    AddProfileCard(onClick = { showAddDialog = true })
                } else {
                    val profile = profiles[page]
                    val token = remember(bindingsVersion, profile.id) {
                        DeviceProfileManager.getBinding(prefs, profile)
                    }
                    ProfileCard(
                        profile = profile,
                        boundName = DeviceProfileManager.displayName(token),
                        showApply = !autoSwitchEnabled,
                        onPick = { pickerFor = profile },
                        onApply = {
                            val svc = AxionFxService.instance
                            val applied = svc?.applyProfile(profile)
                                ?: DeviceProfileManager.applyBinding(context, prefs, profile)
                            if (applied) {
                                val name = DeviceProfileManager.displayName(token) ?: ""
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.preset_loaded, name),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.device_profiles_none_bound),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        onRename = (profile as? DeviceProfile.User)?.let { { profileToRename = it } },
                        onDelete = (profile as? DeviceProfile.User)?.let { { profileToDelete = it } },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PageIndicator(
                pageCount = profiles.size + 1,
                currentPage = pagerState.currentPage,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileCard(
    profile: DeviceProfile,
    boundName: String?,
    showApply: Boolean,
    onPick: () -> Unit,
    onApply: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    val title = when (profile) {
        is DeviceProfile.Fixed -> stringResource(categoryTitleRes(profile.category))
        is DeviceProfile.User -> profile.name
    }
    val summary = boundName ?: stringResource(R.string.device_profiles_unbound)
    val icon = when (profile) {
        is DeviceProfile.Fixed -> categoryIcon(profile.category)
        is DeviceProfile.User -> Icons.Rounded.Tune
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                Box(modifier = Modifier.weight(1f))
                if (onRename != null || onDelete != null) {
                    ProfileCardMenu(onRename = onRename, onDelete = onDelete)
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp),
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = if (boundName != null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onPick,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.device_profiles_set_preset))
                }
                if (showApply) {
                    FilledTonalButton(
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        enabled = boundName != null,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(stringResource(R.string.device_profiles_apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCardMenu(onRename: (() -> Unit)?, onDelete: (() -> Unit)?) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (onRename != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.presets_rename)) },
                    onClick = { expanded = false; onRename() },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                )
            }
            if (onDelete != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.presets_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = { expanded = false; onDelete() },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun AddProfileCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(40.dp),
                )
            }
            Text(
                text = stringResource(R.string.device_profiles_add_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.device_profiles_add_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FilledTonalButton(onClick = onClick) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text(stringResource(R.string.device_profiles_add_button))
            }
        }
    }
}

@Composable
private fun AddProfileDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.device_profiles_add_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.device_profiles_add_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank(),
            ) {
                Text(stringResource(R.string.device_profiles_add_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.presets_confirm_cancel)) }
        },
    )
}

@Composable
private fun RenameProfileDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.presets_rename_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.presets_rename_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank() && text != initial,
            ) {
                Text(stringResource(R.string.presets_confirm_rename))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.presets_confirm_cancel)) }
        },
    )
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val color by animateColorAsState(
                targetValue = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                label = "dot",
            )
            Box(
                modifier = Modifier
                    .size(if (active) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun PresetPickerDialog(
    profile: DeviceProfile,
    current: String?,
    builtinNames: List<String>,
    userPresetNames: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    val title = when (profile) {
        is DeviceProfile.Fixed -> stringResource(categoryTitleRes(profile.category))
        is DeviceProfile.User -> profile.name
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.device_profiles_pick_title, title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                PickerRow(
                    label = stringResource(R.string.device_profiles_unbound),
                    selected = current.isNullOrEmpty(),
                    onClick = { onSelect(null) },
                )
                if (builtinNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.presets_builtin_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    builtinNames.forEach { name ->
                        val token = DeviceProfileManager.builtinToken(name)
                        PickerRow(
                            label = name,
                            selected = current == token,
                            onClick = { onSelect(token) },
                        )
                    }
                }
                if (userPresetNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.presets_saved_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    userPresetNames.forEach { name ->
                        val token = DeviceProfileManager.userToken(name)
                        PickerRow(
                            label = name,
                            selected = current == token,
                            onClick = { onSelect(token) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.presets_confirm_cancel))
            }
        },
    )
}

@Composable
private fun PickerRow(label: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
    )
}

internal fun categoryTitleRes(category: DeviceCategory): Int = when (category) {
    DeviceCategory.SPEAKER -> R.string.device_cat_speaker
    DeviceCategory.WIRED -> R.string.device_cat_wired
    DeviceCategory.BLUETOOTH -> R.string.device_cat_bluetooth
    DeviceCategory.USB -> R.string.device_cat_usb
    DeviceCategory.OTHER -> R.string.device_cat_other
}

internal fun categoryIcon(category: DeviceCategory): ImageVector = when (category) {
    DeviceCategory.SPEAKER -> Icons.Rounded.VolumeUp
    DeviceCategory.WIRED -> Icons.Rounded.Headphones
    DeviceCategory.BLUETOOTH -> Icons.Rounded.Bluetooth
    DeviceCategory.USB -> Icons.Rounded.Usb
    DeviceCategory.OTHER -> Icons.Rounded.Speaker
}
