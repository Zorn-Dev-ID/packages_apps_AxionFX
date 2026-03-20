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

#include "DiffSurround.h"

#include <algorithm>
#include <cmath>

namespace axionfx {

static constexpr float HP_CUTOFF_HZ = 200.0f;

void DiffSurround::configure(float sampleRate) {
    mSampleRate = sampleRate;
    int maxSamples = static_cast<int>(sampleRate * MAX_DELAY_MS / 1000.0f) + 1;
    mDelayL.setSize(maxSamples);
    mDelayR.setSize(maxSamples);
    mDelaySamples = static_cast<int>(sampleRate * mDelayMs / 1000.0f);

    float rc = 1.0f / (2.0f * M_PI * HP_CUTOFF_HZ);
    float dt = 1.0f / sampleRate;
    mHpCoeff = rc / (rc + dt);
}

void DiffSurround::setDelay(float ms) {
    mDelayMs = std::clamp(ms, 0.1f, MAX_DELAY_MS);
    mDelaySamples = static_cast<int>(mSampleRate * mDelayMs / 1000.0f);
    if (mDelaySamples < 1) mDelaySamples = 1;
}

void DiffSurround::setWidth(float width) {
    mWidth = std::clamp(width, 0.0f, 1.0f);
}

void DiffSurround::process(float* buffer, int frames) {
    if (!mEnabled) return;

    float prevHpL = mHpStateL;
    float prevHpR = mHpStateR;

    for (int f = 0; f < frames; f++) {
        float left = buffer[f * 2];
        float right = buffer[f * 2 + 1];

        float hpL = mHpCoeff * (prevHpL + left - buffer[f * 2]);
        float hpR = mHpCoeff * (prevHpR + right - buffer[f * 2 + 1]);
        prevHpL = hpL;
        prevHpR = hpR;

        mDelayL.write(hpL);
        mDelayR.write(hpR);

        float delayedL = mDelayL.read(mDelaySamples);
        float delayedR = mDelayR.read(mDelaySamples);

        int shortDelay = std::max(1, mDelaySamples / 3);
        float earlyL = mDelayL.read(shortDelay) * 0.3f;
        float earlyR = mDelayR.read(shortDelay) * 0.3f;

        buffer[f * 2] = left + (delayedR + earlyR) * mWidth;
        buffer[f * 2 + 1] = right + (delayedL + earlyL) * mWidth;
    }

    mHpStateL = prevHpL;
    mHpStateR = prevHpR;
}

void DiffSurround::setEnabled(bool enabled) {
    mEnabled = enabled;
}

void DiffSurround::reset() {
    mDelayL.reset();
    mDelayR.reset();
    mHpStateL = 0.0f;
    mHpStateR = 0.0f;
}

}  // namespace axionfx
