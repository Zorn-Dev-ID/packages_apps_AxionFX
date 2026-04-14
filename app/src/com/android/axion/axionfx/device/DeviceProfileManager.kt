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

package com.android.axion.axionfx.device

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.axion.axionfx.preset.PresetManager

object DeviceProfileManager {

    private const val TAG = "AxionFxDeviceProfile"
    private const val USER_NAMES_KEY = "device_profile_user_names"

    const val BUILTIN_PREFIX = "builtin:"
    const val USER_PREFIX = "user:"

    fun listProfiles(prefs: SharedPreferences): List<DeviceProfile> {
        val fixed = DeviceCategory.values().map { DeviceProfile.Fixed(it) }
        val user = listUserNames(prefs).map { DeviceProfile.User(it) }
        return fixed + user
    }

    fun listUserNames(prefs: SharedPreferences): List<String> {
        val set = prefs.getStringSet(USER_NAMES_KEY, emptySet()) ?: emptySet()
        return set.sorted()
    }

    fun addUserProfile(prefs: SharedPreferences, name: String): DeviceProfile.User? {
        val clean = sanitize(name) ?: return null
        val existing = prefs.getStringSet(USER_NAMES_KEY, emptySet()) ?: emptySet()
        if (existing.any { it.equals(clean, ignoreCase = true) }) return null
        if (DeviceCategory.values().any { it.name.equals(clean, ignoreCase = true) }) return null
        val updated = existing + clean
        prefs.edit().putStringSet(USER_NAMES_KEY, updated).apply()
        Log.d(TAG, "Added user profile: $clean")
        return DeviceProfile.User(clean)
    }

    fun removeUserProfile(prefs: SharedPreferences, name: String) {
        val existing = prefs.getStringSet(USER_NAMES_KEY, emptySet()) ?: emptySet()
        val updated = existing.filterNot { it == name }.toSet()
        prefs.edit()
            .putStringSet(USER_NAMES_KEY, updated)
            .remove(DeviceProfile.User(name).prefKey)
            .apply()
    }

    fun renameUserProfile(prefs: SharedPreferences, oldName: String, newName: String): Boolean {
        val clean = sanitize(newName) ?: return false
        val existing = prefs.getStringSet(USER_NAMES_KEY, emptySet()) ?: emptySet()
        if (!existing.contains(oldName)) return false
        if (existing.any { it != oldName && it.equals(clean, ignoreCase = true) }) return false
        val oldKey = DeviceProfile.User(oldName).prefKey
        val newKey = DeviceProfile.User(clean).prefKey
        val binding = prefs.getString(oldKey, null)
        val updated = existing.filterNot { it == oldName }.toSet() + clean
        val editor = prefs.edit()
            .putStringSet(USER_NAMES_KEY, updated)
            .remove(oldKey)
        if (!binding.isNullOrEmpty()) editor.putString(newKey, binding)
        editor.apply()
        return true
    }

    fun getBinding(prefs: SharedPreferences, profile: DeviceProfile): String? {
        val raw = prefs.getString(profile.prefKey, null)
        return if (raw.isNullOrEmpty()) null else raw
    }

    fun setBinding(prefs: SharedPreferences, profile: DeviceProfile, token: String?) {
        val editor = prefs.edit()
        if (token.isNullOrEmpty()) editor.remove(profile.prefKey)
        else editor.putString(profile.prefKey, token)
        editor.apply()
    }

    fun applyBinding(
        context: Context,
        prefs: SharedPreferences,
        profile: DeviceProfile,
    ): Boolean {
        val token = getBinding(prefs, profile) ?: return false
        return when {
            token.startsWith(BUILTIN_PREFIX) -> {
                val name = token.removePrefix(BUILTIN_PREFIX)
                val snapshot = captureProfileState(prefs)
                PresetManager.loadBuiltinPreset(name, prefs)
                restoreProfileState(prefs, snapshot)
                Log.d(TAG, "Applied builtin '$name' for ${profile.id}")
                true
            }
            token.startsWith(USER_PREFIX) -> {
                val name = token.removePrefix(USER_PREFIX)
                PresetManager.loadPreset(context, name, prefs)
                Log.d(TAG, "Applied user preset '$name' for ${profile.id}")
                true
            }
            else -> false
        }
    }

    fun builtinToken(name: String): String = "$BUILTIN_PREFIX$name"
    fun userToken(name: String): String = "$USER_PREFIX$name"

    fun displayName(token: String?): String? {
        if (token.isNullOrEmpty()) return null
        return when {
            token.startsWith(BUILTIN_PREFIX) -> token.removePrefix(BUILTIN_PREFIX)
            token.startsWith(USER_PREFIX) -> token.removePrefix(USER_PREFIX)
            else -> null
        }
    }

    private fun sanitize(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed.length > 32) return null
        val cleaned = trimmed.replace(Regex("[^\\p{L}\\p{N}_\\- ]"), "")
        return cleaned.ifBlank { null }
    }

    private data class ProfileState(
        val userNames: Set<String>,
        val bindings: Map<String, String?>,
    )

    private fun captureProfileState(prefs: SharedPreferences): ProfileState {
        val names = prefs.getStringSet(USER_NAMES_KEY, emptySet())?.toSet().orEmpty()
        val bindings = mutableMapOf<String, String?>()
        for (category in DeviceCategory.values()) {
            bindings[category.prefKey] = prefs.getString(category.prefKey, null)
        }
        for (name in names) {
            val key = DeviceProfile.User(name).prefKey
            bindings[key] = prefs.getString(key, null)
        }
        return ProfileState(names, bindings)
    }

    private fun restoreProfileState(prefs: SharedPreferences, state: ProfileState) {
        val editor = prefs.edit()
        editor.putStringSet(USER_NAMES_KEY, state.userNames)
        for ((key, value) in state.bindings) {
            if (value == null) editor.remove(key) else editor.putString(key, value)
        }
        editor.apply()
    }
}
