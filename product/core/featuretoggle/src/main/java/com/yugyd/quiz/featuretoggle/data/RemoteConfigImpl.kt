/*
 *    Copyright 2023 Roman Likhachev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.yugyd.quiz.featuretoggle.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.yugyd.quiz.featuretoggle.R
import com.yugyd.quiz.remoteconfig.api.RemoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

internal class RemoteConfigImpl @Inject internal constructor(
    @ApplicationContext private val context: Context,
) : RemoteConfig {

    private val remoteConfig: FirebaseRemoteConfig? by lazy {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            Firebase.remoteConfig.apply {
                setDefaultsAsync(R.xml.remote_config_defaults)
                setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = ONE_HOUR
                    }
                )
            }
        } else {
            null
        }
    }

    override suspend fun fetchStringValue(key: String): String {
        fetchConfig()
        return getStringValue(key)
    }

    override suspend fun fetchLongValue(key: String): Long {
        fetchConfig()
        return getLongValue(key)
    }


    override suspend fun fetchBooleanValue(key: String): Boolean {
        fetchConfig()
        return getBooleanValue(key)
    }

    override fun getStringValue(key: String): String {
        val rawValue = remoteConfig?.getString(key).orEmpty()
        Timber.tag(TAG).d("New config value %s", rawValue)
        return rawValue
    }

    private suspend fun fetchConfig() {
        if (remoteConfig == null) {
            return
        }

        remoteConfig!!
            .fetchAndActivate()
            .addOnSuccessListener {
                Timber.tag(TAG).d("Fetch config is successful")
            }
            .addOnFailureListener { error ->
                Timber.tag(TAG).d("Fetch config error")
                Timber.tag(TAG).e(error, "msg")
            }
            .await()
    }

    private fun getLongValue(key: String): Long {
        val rawValue = remoteConfig?.getLong(key) ?: 0
        Timber.tag(TAG).d("New config value %s", rawValue)
        return rawValue
    }

    private fun getBooleanValue(key: String): Boolean {
        val rawValue = remoteConfig?.getBoolean(key) ?: false
        Timber.tag(TAG).d("New config value $key: %s", rawValue)
        return rawValue
    }

    companion object {
        private const val TAG = "RemoteConfigImpl"
        private const val ONE_HOUR = 3600L
    }
}
