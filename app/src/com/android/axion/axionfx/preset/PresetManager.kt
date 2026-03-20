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

package com.android.axion.axionfx.preset

import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.io.File

object PresetManager {

    private const val TAG = "AxionFxPreset"
    private val PRESETS_DIR = File("/data/system/axionfx/presets")

    private val PERSISTED_KEYS = listOf(
        "master_enabled", "output_gain",
        "eq_enabled", "bass_enabled", "bass_mode", "bass_gain",
        "widener_enabled", "widener_width",
        "limiter_enabled",
        "reverb_enabled", "reverb_room", "reverb_wet",
        "compressor_enabled",
        "tube_enabled", "tube_drive", "tube_mix",
        "agc_enabled",
        "crossfeed_enabled", "crossfeed_level",
        "surround_enabled",
        "exciter_enabled", "exciter_drive", "exciter_blend", "exciter_freq",
        "mcomp_enabled",
        "fir_eq_enabled",
        "convolver_enabled", "convolver_mix",
    )

    private val BAND_KEY_PREFIXES = listOf(
        "eq_band_" to 10,
        "fir_eq_band_" to 15,
        "mcomp_thresh_" to 4,
        "mcomp_ratio_" to 4,
        "mcomp_makeup_" to 4,
    )

    fun savePreset(name: String, prefs: SharedPreferences) {
        PRESETS_DIR.mkdirs()
        val json = JSONObject()

        for (key in PERSISTED_KEYS) {
            val all = prefs.all
            if (all.containsKey(key)) {
                when (val value = all[key]) {
                    is Boolean -> json.put(key, value)
                    is Int -> json.put(key, value)
                    is Float -> json.put(key, value.toDouble())
                    is String -> json.put(key, value)
                }
            }
        }

        for ((prefix, count) in BAND_KEY_PREFIXES) {
            for (i in 0 until count) {
                val key = "$prefix$i"
                if (prefs.contains(key)) {
                    json.put(key, prefs.getInt(key, 0))
                }
            }
        }

        val file = File(PRESETS_DIR, "${sanitizeName(name)}.json")
        file.writeText(json.toString(2))
        Log.d(TAG, "Saved preset: $name -> ${file.absolutePath}")
    }

    fun loadPreset(name: String, prefs: SharedPreferences) {
        val file = File(PRESETS_DIR, "${sanitizeName(name)}.json")
        if (!file.exists()) return

        val json = JSONObject(file.readText())
        val editor = prefs.edit()

        for (key in json.keys()) {
            when (val value = json.get(key)) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putInt(key, value.toInt())
                is Double -> editor.putFloat(key, value.toFloat())
                is String -> editor.putString(key, value)
            }
        }

        editor.apply()
        Log.d(TAG, "Loaded preset: $name")
    }

    private val BUILTIN_PRESETS = mapOf(
        "Clarity" to mapOf(
            "eq_enabled" to true,
            "eq_band_6" to 150, "eq_band_7" to 200, "eq_band_8" to 250, "eq_band_9" to 150,
            "exciter_enabled" to true,
            "exciter_drive" to 40, "exciter_blend" to 25, "exciter_freq" to 4000,
            "compressor_enabled" to true,
            "limiter_enabled" to true,
            "output_gain" to 100,
        ),
        "Speaker Boost" to mapOf(
            "bass_enabled" to true, "bass_mode" to 1, "bass_gain" to 400,
            "eq_enabled" to true,
            "eq_band_0" to 300, "eq_band_1" to 200, "eq_band_8" to 150, "eq_band_9" to 100,
            "compressor_enabled" to true,
            "agc_enabled" to true,
            "limiter_enabled" to true,
            "output_gain" to 130,
        ),
        "Headphone" to mapOf(
            "crossfeed_enabled" to true, "crossfeed_level" to 25,
            "spatial_enabled" to true, "spatial_blend" to 50,
            "widener_enabled" to true, "widener_width" to 130,
            "limiter_enabled" to true,
            "output_gain" to 100,
        ),
        "Bass Heavy" to mapOf(
            "bass_enabled" to true, "bass_mode" to 2, "bass_gain" to 800,
            "eq_enabled" to true,
            "eq_band_0" to 400, "eq_band_1" to 350, "eq_band_2" to 250, "eq_band_3" to 100,
            "tube_enabled" to true, "tube_drive" to 150, "tube_mix" to 30,
            "limiter_enabled" to true,
            "output_gain" to 110,
        ),
        "Vocal" to mapOf(
            "eq_enabled" to true,
            "eq_band_0" to -200, "eq_band_1" to -100,
            "eq_band_4" to 250, "eq_band_5" to 300, "eq_band_6" to 200,
            "eq_band_8" to -100, "eq_band_9" to -200,
            "exciter_enabled" to true,
            "exciter_drive" to 30, "exciter_blend" to 20, "exciter_freq" to 3000,
            "compressor_enabled" to true,
            "limiter_enabled" to true,
            "output_gain" to 100,
        ),
    )

    fun listBuiltinPresets(): List<String> = BUILTIN_PRESETS.keys.toList()

    fun loadBuiltinPreset(name: String, prefs: SharedPreferences) {
        val preset = BUILTIN_PRESETS[name] ?: return
        val editor = prefs.edit()
        editor.clear()
        for ((key, value) in preset) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
            }
        }
        editor.putBoolean("master_enabled", true)
        editor.apply()
        Log.d(TAG, "Loaded builtin preset: $name")
    }

    fun deletePreset(name: String) {
        val file = File(PRESETS_DIR, "${sanitizeName(name)}.json")
        if (file.exists()) file.delete()
    }

    fun listPresets(): List<String> {
        if (!PRESETS_DIR.exists()) return emptyList()
        return PRESETS_DIR.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }

    fun exportPreset(name: String): String? {
        val file = File(PRESETS_DIR, "${sanitizeName(name)}.json")
        return if (file.exists()) file.readText() else null
    }

    fun importPreset(name: String, json: String) {
        PRESETS_DIR.mkdirs()
        val file = File(PRESETS_DIR, "${sanitizeName(name)}.json")
        file.writeText(json)
    }

    private fun sanitizeName(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9_\\- ]"), "").trim()
}
