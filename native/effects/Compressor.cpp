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

#include "Compressor.h"

#include <algorithm>
#include <cmath>

namespace axionfx {

static constexpr float DB_FLOOR = -96.0f;

static float linearToDb(float linear) {
    if (linear < 1e-10f) return DB_FLOOR;
    return 20.0f * std::log10(linear);
}

static float dbToLinear(float dB) {
    return std::pow(10.0f, dB / 20.0f);
}

void Compressor::configure(float sampleRate) {
    mSampleRate = sampleRate;
    setAttack(10.0f);
    setRelease(100.0f);
}

void Compressor::setThreshold(float dB) {
    mThresholdDb = std::clamp(dB, -60.0f, 0.0f);
}

void Compressor::setRatio(float ratio) {
    mRatio = std::clamp(ratio, 1.0f, 20.0f);
}

void Compressor::setAttack(float ms) {
    ms = std::clamp(ms, 0.1f, 200.0f);
    mAttackCoeff = std::exp(-1.0f / (mSampleRate * ms / 1000.0f));
}

void Compressor::setRelease(float ms) {
    ms = std::clamp(ms, 1.0f, 2000.0f);
    mReleaseCoeff = std::exp(-1.0f / (mSampleRate * ms / 1000.0f));
}

void Compressor::setKnee(float dB) {
    mKneeDb = std::clamp(dB, 0.0f, 20.0f);
}

void Compressor::setMakeupGain(float dB) {
    mMakeupGainLinear = dbToLinear(std::clamp(dB, 0.0f, 30.0f));
}

float Compressor::computeGain(float inputDb) const {
    float halfKnee = mKneeDb / 2.0f;
    float outputDb;

    if (inputDb <= mThresholdDb - halfKnee) {
        outputDb = inputDb;
    } else if (inputDb >= mThresholdDb + halfKnee) {
        outputDb = mThresholdDb + (inputDb - mThresholdDb) / mRatio;
    } else {
        float x = inputDb - mThresholdDb + halfKnee;
        outputDb = inputDb + ((1.0f / mRatio) - 1.0f) * x * x / (2.0f * mKneeDb);
    }

    return outputDb - inputDb;
}

void Compressor::process(float* buffer, int frames) {
    if (!mEnabled) return;

    for (int f = 0; f < frames; f++) {
        float left = buffer[f * 2];
        float right = buffer[f * 2 + 1];

        float peak = std::max(std::fabs(left), std::fabs(right));
        float inputDb = linearToDb(peak);

        float coeff = (inputDb > mEnvelopeDb) ? mAttackCoeff : mReleaseCoeff;
        mEnvelopeDb = coeff * mEnvelopeDb + (1.0f - coeff) * inputDb;

        float gainDb = computeGain(mEnvelopeDb);
        float gainLinear = dbToLinear(gainDb) * mMakeupGainLinear;

        buffer[f * 2] = left * gainLinear;
        buffer[f * 2 + 1] = right * gainLinear;
    }
}

void Compressor::setEnabled(bool enabled) {
    mEnabled = enabled;
}

void Compressor::reset() {
    mEnvelopeDb = DB_FLOOR;
}

}  // namespace axionfx
