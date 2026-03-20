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

import com.android.axion.axionfx.AxionFxController
import com.android.axion.axionfx.data.EffectRepository

class EffectInteractor(private val repo: EffectRepository) {

    fun setMasterEnabled(enabled: Boolean) {
        AxionFxController.setMasterEnabled(enabled)
        repo.putBoolean(EffectKeys.MASTER_ENABLED, enabled)
    }

    fun setOutputGain(value: Int) {
        AxionFxController.setOutputGain(value)
        repo.putInt(EffectKeys.OUTPUT_GAIN, value)
    }

    fun setEqEnabled(enabled: Boolean) {
        AxionFxController.setEqEnabled(enabled)
        repo.putBoolean(EffectKeys.EQ_ENABLED, enabled)
    }

    fun setEqBandLevel(band: Int, level: Int) {
        AxionFxController.setEqBandLevel(band, level)
        repo.putInt("${EffectKeys.EQ_BAND_PREFIX}$band", level)
    }

    fun setFirEqEnabled(enabled: Boolean) {
        AxionFxController.setFirEqEnabled(enabled)
        repo.putBoolean(EffectKeys.FIR_EQ_ENABLED, enabled)
    }

    fun setFirEqBandGain(band: Int, tenthsDb: Int) {
        AxionFxController.setFirEqBandGain(band, tenthsDb)
        repo.putInt("${EffectKeys.FIR_EQ_BAND_PREFIX}$band", tenthsDb)
    }

    fun setBassEnabled(enabled: Boolean) {
        AxionFxController.setBassEnabled(enabled)
        repo.putBoolean(EffectKeys.BASS_ENABLED, enabled)
    }

    fun setBassMode(mode: Int) {
        AxionFxController.setBassMode(mode)
        repo.putInt(EffectKeys.BASS_MODE, mode)
    }

    fun setBassGain(gain: Int) {
        AxionFxController.setBassGain(gain)
        repo.putInt(EffectKeys.BASS_GAIN, gain)
    }

    fun setWidenerEnabled(enabled: Boolean) {
        AxionFxController.setWidenerEnabled(enabled)
        repo.putBoolean(EffectKeys.WIDENER_ENABLED, enabled)
    }

    fun setWidenerWidth(width: Int) {
        AxionFxController.setWidenerWidth(width)
        repo.putInt(EffectKeys.WIDENER_WIDTH, width)
    }

    fun setCrossfeedEnabled(enabled: Boolean) {
        AxionFxController.setCrossfeedEnabled(enabled)
        repo.putBoolean(EffectKeys.CROSSFEED_ENABLED, enabled)
    }

    fun setCrossfeedLevel(level: Int) {
        AxionFxController.setCrossfeedLevel(level)
        repo.putInt(EffectKeys.CROSSFEED_LEVEL, level)
    }

    fun setSurroundEnabled(enabled: Boolean) {
        AxionFxController.setSurroundEnabled(enabled)
        repo.putBoolean(EffectKeys.SURROUND_ENABLED, enabled)
    }

    fun setSurroundDelay(delay: Int) {
        AxionFxController.setParameter(0xB01, delay)
        repo.putInt(EffectKeys.SURROUND_DELAY, delay)
    }

    fun setSurroundWidth(width: Int) {
        AxionFxController.setParameter(0xB02, width)
        repo.putInt(EffectKeys.SURROUND_WIDTH, width)
    }

    fun setOutputPan(pan: Int) {
        AxionFxController.setParameter(0x102, pan)
        repo.putInt(EffectKeys.OUTPUT_PAN, pan)
    }

    fun setLimiterThreshold(threshold: Int) {
        AxionFxController.setParameter(0x501, threshold)
        repo.putInt(EffectKeys.LIMITER_THRESHOLD, threshold)
    }

    fun setSpatialEnabled(enabled: Boolean) {
        AxionFxController.setSpatialEnabled(enabled)
        repo.putBoolean(EffectKeys.SPATIAL_ENABLED, enabled)
    }

    fun setSpatialWidth(width: Int) {
        AxionFxController.setSpatialWidth(width)
        repo.putInt(EffectKeys.SPATIAL_WIDTH, width)
    }

    fun setSpatialBlend(blend: Int) {
        AxionFxController.setSpatialBlend(blend)
        repo.putInt(EffectKeys.SPATIAL_BLEND, blend)
    }

    fun setCompressorEnabled(enabled: Boolean) {
        AxionFxController.setCompressorEnabled(enabled)
        repo.putBoolean(EffectKeys.COMPRESSOR_ENABLED, enabled)
    }

    fun setAgcEnabled(enabled: Boolean) {
        AxionFxController.setAgcEnabled(enabled)
        repo.putBoolean(EffectKeys.AGC_ENABLED, enabled)
    }

    fun setLimiterEnabled(enabled: Boolean) {
        AxionFxController.setLimiterEnabled(enabled)
        repo.putBoolean(EffectKeys.LIMITER_ENABLED, enabled)
    }

    fun setReverbEnabled(enabled: Boolean) {
        AxionFxController.setReverbEnabled(enabled)
        repo.putBoolean(EffectKeys.REVERB_ENABLED, enabled)
    }

    fun setReverbRoomSize(size: Int) {
        AxionFxController.setReverbRoomSize(size)
        repo.putInt(EffectKeys.REVERB_ROOM, size)
    }

    fun setReverbWet(wet: Int) {
        AxionFxController.setReverbWet(wet)
        repo.putInt(EffectKeys.REVERB_WET, wet)
    }

    fun setTubeEnabled(enabled: Boolean) {
        AxionFxController.setTubeEnabled(enabled)
        repo.putBoolean(EffectKeys.TUBE_ENABLED, enabled)
    }

    fun setTubeDrive(drive: Int) {
        AxionFxController.setTubeDrive(drive)
        repo.putInt(EffectKeys.TUBE_DRIVE, drive)
    }

    fun setTubeMix(mix: Int) {
        AxionFxController.setTubeMix(mix)
        repo.putInt(EffectKeys.TUBE_MIX, mix)
    }

    fun setExciterEnabled(enabled: Boolean) {
        AxionFxController.setExciterEnabled(enabled)
        repo.putBoolean(EffectKeys.EXCITER_ENABLED, enabled)
    }

    fun setExciterDrive(drive: Int) {
        AxionFxController.setExciterDrive(drive)
        repo.putInt(EffectKeys.EXCITER_DRIVE, drive)
    }

    fun setExciterBlend(blend: Int) {
        AxionFxController.setExciterBlend(blend)
        repo.putInt(EffectKeys.EXCITER_BLEND, blend)
    }

    fun setExciterFreq(freq: Int) {
        AxionFxController.setExciterFreq(freq)
        repo.putInt(EffectKeys.EXCITER_FREQ, freq)
    }

    fun setMCompEnabled(enabled: Boolean) {
        AxionFxController.setMCompEnabled(enabled)
        repo.putBoolean(EffectKeys.MCOMP_ENABLED, enabled)
    }

    fun setMCompBandThreshold(band: Int, value: Int) {
        AxionFxController.setMCompBandThreshold(band, value)
        repo.putInt("${EffectKeys.MCOMP_THRESH_PREFIX}$band", value)
    }

    fun setMCompBandRatio(band: Int, value: Int) {
        AxionFxController.setMCompBandRatio(band, value)
        repo.putInt("${EffectKeys.MCOMP_RATIO_PREFIX}$band", value)
    }

    fun setMCompBandMakeup(band: Int, value: Int) {
        AxionFxController.setMCompBandMakeup(band, value)
        repo.putInt("${EffectKeys.MCOMP_MAKEUP_PREFIX}$band", value)
    }

    fun setConvolverEnabled(enabled: Boolean) {
        AxionFxController.setConvolverEnabled(enabled)
        repo.putBoolean(EffectKeys.CONVOLVER_ENABLED, enabled)
    }

    fun setConvolverMix(mix: Int) {
        AxionFxController.setParameter(0xC01, mix)
        repo.putInt(EffectKeys.CONVOLVER_MIX, mix)
    }

    fun resetAll() {
        repo.clear()
        setMasterEnabled(EffectDefaults.MASTER_ENABLED)
        setOutputGain(EffectDefaults.OUTPUT_GAIN)
        setBassEnabled(EffectDefaults.BASS_ENABLED)
        setWidenerEnabled(EffectDefaults.WIDENER_ENABLED)
        setReverbEnabled(EffectDefaults.REVERB_ENABLED)
        setCompressorEnabled(EffectDefaults.COMPRESSOR_ENABLED)
        setTubeEnabled(EffectDefaults.TUBE_ENABLED)
        setAgcEnabled(EffectDefaults.AGC_ENABLED)
        setCrossfeedEnabled(EffectDefaults.CROSSFEED_ENABLED)
        setSurroundEnabled(EffectDefaults.SURROUND_ENABLED)
        setSpatialEnabled(EffectDefaults.SPATIAL_ENABLED)
        setLimiterEnabled(EffectDefaults.LIMITER_ENABLED)
        setEqEnabled(EffectDefaults.EQ_ENABLED)
        setFirEqEnabled(EffectDefaults.FIR_EQ_ENABLED)
        setMCompEnabled(EffectDefaults.MCOMP_ENABLED)
        setExciterEnabled(EffectDefaults.EXCITER_ENABLED)
        setConvolverEnabled(EffectDefaults.CONVOLVER_ENABLED)
    }
}
