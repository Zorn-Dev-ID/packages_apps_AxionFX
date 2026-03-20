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

package com.android.axion.axionfx.domain

object EffectKeys {
    const val MASTER_ENABLED = "master_enabled"
    const val OUTPUT_GAIN = "output_gain"
    const val OUTPUT_PAN = "output_pan"
    const val MEDIA_ONLY = "media_only_mode"

    const val EQ_ENABLED = "eq_enabled"
    const val EQ_BAND_PREFIX = "eq_band_"

    const val FIR_EQ_ENABLED = "fir_eq_enabled"
    const val FIR_EQ_BAND_PREFIX = "fir_eq_band_"

    const val BASS_ENABLED = "bass_enabled"
    const val BASS_MODE = "bass_mode"
    const val BASS_GAIN = "bass_gain"

    const val WIDENER_ENABLED = "widener_enabled"
    const val WIDENER_WIDTH = "widener_width"

    const val CROSSFEED_ENABLED = "crossfeed_enabled"
    const val CROSSFEED_LEVEL = "crossfeed_level"

    const val SURROUND_ENABLED = "surround_enabled"
    const val SURROUND_DELAY = "surround_delay"
    const val SURROUND_WIDTH = "surround_width"

    const val SPATIAL_ENABLED = "spatial_enabled"
    const val SPATIAL_WIDTH = "spatial_width"
    const val SPATIAL_BLEND = "spatial_blend"

    const val COMPRESSOR_ENABLED = "compressor_enabled"
    const val AGC_ENABLED = "agc_enabled"
    const val LIMITER_ENABLED = "limiter_enabled"
    const val LIMITER_THRESHOLD = "limiter_threshold"

    const val REVERB_ENABLED = "reverb_enabled"
    const val REVERB_ROOM = "reverb_room"
    const val REVERB_WET = "reverb_wet"

    const val TUBE_ENABLED = "tube_enabled"
    const val TUBE_DRIVE = "tube_drive"
    const val TUBE_MIX = "tube_mix"

    const val EXCITER_ENABLED = "exciter_enabled"
    const val EXCITER_DRIVE = "exciter_drive"
    const val EXCITER_BLEND = "exciter_blend"
    const val EXCITER_FREQ = "exciter_freq"

    const val MCOMP_ENABLED = "mcomp_enabled"
    const val MCOMP_THRESH_PREFIX = "mcomp_thresh_"
    const val MCOMP_RATIO_PREFIX = "mcomp_ratio_"
    const val MCOMP_MAKEUP_PREFIX = "mcomp_makeup_"

    const val CONVOLVER_ENABLED = "convolver_enabled"
    const val CONVOLVER_MIX = "convolver_mix"
    const val CONVOLVER_IR_PATH = "convolver_ir_path"
}
