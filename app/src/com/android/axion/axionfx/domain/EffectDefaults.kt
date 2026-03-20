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

object EffectDefaults {
    const val MASTER_ENABLED = true
    const val OUTPUT_GAIN = 100
    const val OUTPUT_PAN = 0
    const val MEDIA_ONLY = false

    const val EQ_ENABLED = false
    const val FIR_EQ_ENABLED = false

    const val BASS_ENABLED = false
    const val BASS_MODE = 0
    const val BASS_GAIN = 0

    const val WIDENER_ENABLED = false
    const val WIDENER_WIDTH = 100

    const val CROSSFEED_ENABLED = false
    const val CROSSFEED_LEVEL = 30

    const val SURROUND_ENABLED = false
    const val SURROUND_DELAY = 1200
    const val SURROUND_WIDTH = 60

    const val SPATIAL_ENABLED = false
    const val SPATIAL_WIDTH = 30
    const val SPATIAL_BLEND = 70

    const val COMPRESSOR_ENABLED = false
    const val AGC_ENABLED = false
    const val LIMITER_ENABLED = true
    const val LIMITER_THRESHOLD = -10

    const val REVERB_ENABLED = false
    const val REVERB_ROOM = 50
    const val REVERB_WET = 30

    const val TUBE_ENABLED = false
    const val TUBE_DRIVE = 100
    const val TUBE_MIX = 50

    const val EXCITER_ENABLED = false
    const val EXCITER_DRIVE = 50
    const val EXCITER_BLEND = 30
    const val EXCITER_FREQ = 3000

    const val MCOMP_ENABLED = false
    const val MCOMP_THRESHOLD = -200
    const val MCOMP_RATIO = 400
    const val MCOMP_MAKEUP = 0

    const val CONVOLVER_ENABLED = false
    const val CONVOLVER_MIX = 100
}
