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

sealed class DeviceProfile {
    abstract val id: String
    abstract val prefKey: String
    abstract val isUser: Boolean

    data class Fixed(val category: DeviceCategory) : DeviceProfile() {
        override val id: String = "fixed:${category.name}"
        override val prefKey: String = category.prefKey
        override val isUser: Boolean = false
    }

    data class User(val name: String) : DeviceProfile() {
        override val id: String = "user:$name"
        override val prefKey: String = "device_profile_user__$name"
        override val isUser: Boolean = true
    }
}
