/*
 * Copyright 2024 Roman Likhachev
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

import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderTypeModel
import com.yugyd.quiz.domain.aiconnection.AiConnectionInteractor
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(
    private val aiConnectionInteractor: AiConnectionInteractor,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val aiConnection = getAiConnection()
        val request = original.newBuilder()

        if (aiConnection != null) {
            request.header("X-Ai-Key", aiConnection.apiKey)
            request.header("X-Ai-Provider", aiConnection.apiProvider.qualifier)

            val apiModel = when (aiConnection.apiProvider) {
                AiConnectionProviderTypeModel.YANDEX -> aiConnection.apiModel ?: "yandexgpt"
                AiConnectionProviderTypeModel.CHAT_GPT -> aiConnection.apiModel ?: "gpt-4.1-mini"
                AiConnectionProviderTypeModel.NONE -> null
            }
            apiModel?.let {
                request.header("X-Ai-Model", it)
            }

            aiConnection.apiCloudFolder?.let { apiCloudFolder ->
                request.header("X-Ai-Folder", apiCloudFolder)
            }
        }

        return chain.proceed(request.build())
    }

    private fun getAiConnection(): AiConnectionModel? = runBlocking {
        aiConnectionInteractor.getCurrentAiConnection()
    }
}
