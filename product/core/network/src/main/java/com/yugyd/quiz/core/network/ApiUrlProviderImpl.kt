/*
 * Copyright 2025 Roman Likhachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yugyd.quiz.core.network

import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.remoteconfig.api.RemoteConfig
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiUrlProviderImpl @Inject constructor(
    private val remoteConfig: RemoteConfig,
    private val logger: Logger,
) : ApiUrlProvider, ApiUrlInitializer {

    private val url: AtomicReference<String?> = AtomicReference(null)

    override fun fetchBaseUrl() {
        val fetchedUrl = remoteConfig.getStringValue(REMOTE_CONFIG_KEY).ifBlank { DEFAULT_URL }
        url.set(fetchedUrl)
        logger.print(TAG, "Base URL fetched: $fetchedUrl")
    }

    override fun getBaseUrl(): String {
        return url.get() ?: throw IllegalStateException(
            "Base URL has not been initialized. Call fetchBaseUrl() first."
        )
    }

    private companion object {
        private const val TAG = "ApiUrlProviderImpl"
        private const val DEFAULT_URL = "https://www.replaceme.com/api/"
        private const val REMOTE_CONFIG_KEY = "base_api_url"
    }
}
