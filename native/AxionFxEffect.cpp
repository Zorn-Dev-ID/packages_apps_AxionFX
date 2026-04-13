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

#include <algorithm>
#include <cstddef>
#include <cstring>
#include <memory>

#include <aidl/android/hardware/audio/effect/DefaultExtension.h>
#define LOG_TAG "AHAL_AxionFxEffect"
#include <android-base/logging.h>
#include <fmq/AidlMessageQueue.h>
#include <system/audio_effects/effect_uuid.h>

#include "AxionFxEffect.h"
#include "AxionFxParams.h"

using aidl::android::hardware::audio::effect::AxionFxContext;
using aidl::android::hardware::audio::effect::AxionFxEffect;
using aidl::android::hardware::audio::effect::DefaultExtension;
using aidl::android::hardware::audio::effect::Descriptor;
using aidl::android::hardware::audio::effect::getEffectImplUuidAxionFx;
using aidl::android::hardware::audio::effect::getEffectTypeUuidAxionFx;
using aidl::android::hardware::audio::effect::IEffect;
using aidl::android::hardware::audio::effect::VendorExtension;
using aidl::android::media::audio::common::AudioUuid;

extern "C" binder_exception_t createEffect(const AudioUuid* in_impl_uuid,
                                           std::shared_ptr<IEffect>* instanceSpp) {
    if (!in_impl_uuid || *in_impl_uuid != getEffectImplUuidAxionFx()) {
        LOG(ERROR) << __func__ << " uuid not supported";
        return EX_ILLEGAL_ARGUMENT;
    }
    if (instanceSpp) {
        *instanceSpp = ndk::SharedRefBase::make<AxionFxEffect>();
        LOG(DEBUG) << __func__ << " instance " << instanceSpp->get() << " created";
        return EX_NONE;
    } else {
        LOG(ERROR) << __func__ << " invalid input parameter!";
        return EX_ILLEGAL_ARGUMENT;
    }
}

extern "C" binder_exception_t queryEffect(const AudioUuid* in_impl_uuid,
                                          Descriptor* _aidl_return) {
    if (!in_impl_uuid || *in_impl_uuid != getEffectImplUuidAxionFx()) {
        LOG(ERROR) << __func__ << " uuid not supported";
        return EX_ILLEGAL_ARGUMENT;
    }
    *_aidl_return = AxionFxEffect::kDescriptor;
    return EX_NONE;
}

namespace aidl::android::hardware::audio::effect {

const std::string AxionFxEffect::kEffectName = "AxionFx";

const Descriptor AxionFxEffect::kDescriptor = {
        .common = {.id = {.type = getEffectTypeUuidAxionFx(),
                          .uuid = getEffectImplUuidAxionFx(),
                          .proxy = std::nullopt},
                   .flags = {.type = Flags::Type::INSERT,
                             .insert = Flags::Insert::LAST,
                             .volume = Flags::Volume::CTRL},
                   .name = AxionFxEffect::kEffectName,
                   .implementor = "AxionOS"}};

AxionFxContext::AxionFxContext(int statusDepth, const Parameter::Common& common)
    : EffectContext(statusDepth, common) {
    mEngine.configure(common.input.base.sampleRate);
}

RetCode AxionFxContext::setParams(const std::vector<uint8_t>& params) {
    if (params.size() < sizeof(int32_t)) {
        LOG(ERROR) << "setParams: too small " << params.size();
        return RetCode::ERROR_ILLEGAL_PARAMETER;
    }

    int32_t paramId = *reinterpret_cast<const int32_t*>(params.data());

    if (paramId == axionfx::PARAM_CONVOLVER_LOAD_IR) {
        if (params.size() > sizeof(int32_t)) {
            const char* pathData = reinterpret_cast<const char*>(params.data() + sizeof(int32_t));
            size_t pathLen = params.size() - sizeof(int32_t);
            std::string path(pathData, strnlen(pathData, pathLen));
            mEngine.loadIrFromPath(path.c_str());
        }
        mLastParams = params;
        return RetCode::SUCCESS;
    }

    if (paramId == axionfx::PARAM_CONVOLVER_LOAD_IR_DATA) {
        if (params.size() > sizeof(int32_t)) {
            const uint8_t* wavData = params.data() + sizeof(int32_t);
            size_t wavSize = params.size() - sizeof(int32_t);
            mEngine.loadIrFromData(wavData, wavSize);
        }
        mLastParams = params;
        return RetCode::SUCCESS;
    }

    if (params.size() < sizeof(axionfx::AxionFxParam)) {
        LOG(ERROR) << "setParams: too small " << params.size();
        return RetCode::ERROR_ILLEGAL_PARAMETER;
    }

    const auto* param = reinterpret_cast<const axionfx::AxionFxParam*>(params.data());
    LOG(INFO) << "setParams paramId=" << param->paramId << " value=" << param->value;
    mEngine.setParameter(param->paramId, param->value);
    mLastParams = params;
    return RetCode::SUCCESS;
}

std::vector<uint8_t> AxionFxContext::getParams(const std::vector<uint8_t>& id) const {
    if (id.size() >= sizeof(int32_t)) {
        int32_t paramId = *reinterpret_cast<const int32_t*>(id.data());
        int32_t value = mEngine.getParameter(paramId);
        axionfx::AxionFxParam result = {paramId, value};
        const uint8_t* ptr = reinterpret_cast<const uint8_t*>(&result);
        return std::vector<uint8_t>(ptr, ptr + sizeof(result));
    }
    return mLastParams;
}

AxionFxEffect::~AxionFxEffect() {
    cleanUp();
}

ndk::ScopedAStatus AxionFxEffect::getDescriptor(Descriptor* _aidl_return) {
    LOG(DEBUG) << __func__ << kDescriptor.toString();
    *_aidl_return = kDescriptor;
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus AxionFxEffect::setParameterSpecific(const Parameter::Specific& specific) {
    RETURN_IF(Parameter::Specific::vendorEffect != specific.getTag(), EX_ILLEGAL_ARGUMENT,
              "EffectNotSupported");
    RETURN_IF(!mContext, EX_NULL_POINTER, "nullContext");

    auto& vendorEffect = specific.get<Parameter::Specific::vendorEffect>();
    std::optional<DefaultExtension> defaultExt;
    RETURN_IF(STATUS_OK != vendorEffect.extension.getParcelable(&defaultExt), EX_ILLEGAL_ARGUMENT,
              "getParcelableFailed");
    RETURN_IF(!defaultExt.has_value(), EX_ILLEGAL_ARGUMENT, "parcelableNull");
    RETURN_IF(mContext->setParams(defaultExt->bytes) != RetCode::SUCCESS, EX_ILLEGAL_ARGUMENT,
              "paramNotSupported");

    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus AxionFxEffect::getParameterSpecific(const Parameter::Id& id,
                                                        Parameter::Specific* specific) {
    auto tag = id.getTag();
    RETURN_IF(Parameter::Id::vendorEffectTag != tag, EX_ILLEGAL_ARGUMENT, "wrongIdTag");
    auto extensionId = id.get<Parameter::Id::vendorEffectTag>();
    std::optional<DefaultExtension> defaultIdExt;
    RETURN_IF(STATUS_OK != extensionId.extension.getParcelable(&defaultIdExt), EX_ILLEGAL_ARGUMENT,
              "getIdParcelableFailed");
    RETURN_IF(!defaultIdExt.has_value(), EX_ILLEGAL_ARGUMENT, "parcelableIdNull");

    VendorExtension extension;
    DefaultExtension defaultExt;
    defaultExt.bytes = mContext->getParams(defaultIdExt->bytes);
    RETURN_IF(STATUS_OK != extension.extension.setParcelable(defaultExt), EX_ILLEGAL_ARGUMENT,
              "setParcelableFailed");
    specific->set<Parameter::Specific::vendorEffect>(extension);
    return ndk::ScopedAStatus::ok();
}

std::shared_ptr<EffectContext> AxionFxEffect::createContext(const Parameter::Common& common) {
    if (mContext) {
        LOG(DEBUG) << __func__ << " context already exists";
    } else {
        mContext = std::make_shared<AxionFxContext>(1, common);
    }
    return mContext;
}

RetCode AxionFxEffect::releaseContext() {
    if (mContext) {
        mContext.reset();
    }
    return RetCode::SUCCESS;
}

IEffect::Status AxionFxEffect::effectProcessImpl(float* in, float* out, int samples) {
    if (mContext) {
        mContext->getEngine().process(in, out, samples);
    } else {
        if (in != out) {
            for (int i = 0; i < samples; i++) {
                out[i] = in[i];
            }
        }
    }
    return {STATUS_OK, samples, samples};
}

}  // namespace aidl::android::hardware::audio::effect
