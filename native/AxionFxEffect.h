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

#pragma once

#include <aidl/android/hardware/audio/effect/BnEffect.h>
#include <fmq/AidlMessageQueue.h>
#include <memory>
#include <vector>

#include "AxionFxEngine.h"
#include "effect-impl/EffectImpl.h"

namespace aidl::android::hardware::audio::effect {

class AxionFxContext final : public EffectContext {
public:
    AxionFxContext(int statusDepth, const Parameter::Common& common);

    axionfx::AxionFxEngine& getEngine() { return mEngine; }

    RetCode setParams(const std::vector<uint8_t>& params);
    std::vector<uint8_t> getParams(const std::vector<uint8_t>& id) const;

private:
    axionfx::AxionFxEngine mEngine;
    std::vector<uint8_t> mLastParams;
};

class AxionFxEffect final : public EffectImpl {
public:
    static const std::string kEffectName;
    static const Descriptor kDescriptor;

    AxionFxEffect() = default;
    ~AxionFxEffect() override;

    ndk::ScopedAStatus getDescriptor(Descriptor* _aidl_return) override;
    ndk::ScopedAStatus setParameterSpecific(const Parameter::Specific& specific)
            REQUIRES(mImplMutex) override;
    ndk::ScopedAStatus getParameterSpecific(const Parameter::Id& id,
                                             Parameter::Specific* specific)
            REQUIRES(mImplMutex) override;

    std::shared_ptr<EffectContext> createContext(const Parameter::Common& common)
            REQUIRES(mImplMutex) override;
    RetCode releaseContext() REQUIRES(mImplMutex) override;

    std::string getEffectName() override { return kEffectName; }
    IEffect::Status effectProcessImpl(float* in, float* out, int samples)
            REQUIRES(mImplMutex) override;

private:
    std::shared_ptr<AxionFxContext> mContext GUARDED_BY(mImplMutex);
};

}  // namespace aidl::android::hardware::audio::effect
