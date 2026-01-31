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

package com.yugyd.quiz.domain.aidirectconnection.data

import com.yugyd.quiz.core.result
import com.yugyd.quiz.domain.aidirectconnection.AiClientRemoteSource
import com.yugyd.quiz.domain.aidirectconnection.data.dto.OpenAiChatCompletionRequest
import com.yugyd.quiz.domain.aidirectconnection.data.dto.OpenAiChatCompletionRequest.OpenAiChatMessage
import com.yugyd.quiz.domain.aidirectconnection.data.dto.YandexCompletionOptions
import com.yugyd.quiz.domain.aidirectconnection.data.dto.YandexCompletionRequest
import com.yugyd.quiz.domain.aidirectconnection.data.dto.YandexMessage
import com.yugyd.quiz.domain.aidirectconnection.exceptions.AiClientException
import com.yugyd.quiz.domain.aidirectconnection.exceptions.AiConfigurationException
import com.yugyd.quiz.domain.aidirectconnection.exceptions.AiUnauthorizedException
import com.yugyd.quiz.domain.aidirectconnection.model.AiProvider
import com.yugyd.quiz.domain.aidirectconnection.model.AiRequestConfig
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class AiClientRemoteDataSource @Inject constructor(
    private val openAIApi: OpenAiApi,
    private val yandexGptApi: YandexGptApi,
) : AiClientRemoteSource {

    override suspend fun validateAi(config: AiRequestConfig) {
        when (config.provider) {
            AiProvider.YANDEX -> validateYandex(config)
            AiProvider.OPENAI -> validateOpenAi(config)
        }
    }

    override suspend fun request(
        config: AiRequestConfig,
        prompt: String,
    ): String {
        return when (config.provider) {
            AiProvider.YANDEX -> requestYandex(config, prompt)
            AiProvider.OPENAI -> requestOpenAi(config, prompt)
        }
    }

    private suspend fun validateOpenAi(config: AiRequestConfig) {
        val model = config.model.orEmpty().ifBlank { DEFAULT_OPENAI_MODEL }

        val request = OpenAiChatCompletionRequest(
            model = model,
            messages = listOf(
                OpenAiChatMessage(
                    role = "user",
                    content = "Ping",
                )
            ),
            reasoningEffort = if (model == REASONING_MODEL) "high" else null,
        )

        processResponse(
            apiRequest = {
                openAIApi.createChatCompletion(
                    authorization = "Bearer ${config.apiKey}",
                    request = request,
                )
            },
            mapper = { body ->
                if (body.choices.isEmpty()) {
                    throw AiUnauthorizedException("Unauthorized: Invalid API key or access denied.")
                }
            },
        )
    }

    private suspend fun requestOpenAi(
        config: AiRequestConfig,
        prompt: String,
    ): String {
        val model = config.model.orEmpty().ifBlank { DEFAULT_OPENAI_MODEL }

        val request = OpenAiChatCompletionRequest(
            model = model,
            messages = listOf(
                OpenAiChatMessage(
                    role = "system",
                    content = OPENAI_ROLE_PROMPT,
                ),
                OpenAiChatMessage(
                    role = "user",
                    content = prompt,
                ),
            ),
            reasoningEffort = if (model == REASONING_MODEL) "high" else null,
        )

        return processResponse(
            apiRequest = {
                openAIApi.createChatCompletion(
                    authorization = "Bearer ${config.apiKey}",
                    request = request,
                )
            },
            mapper = { body ->
                val message = body.choices.firstOrNull()?.message
                    ?: throw AiClientException("No translation response received.")
                message.content
            },
        )
    }

    private suspend fun validateYandex(config: AiRequestConfig) {
        val folderId = config.apiFolder.orEmpty()
        if (folderId.isBlank()) {
            throw AiConfigurationException("Folder ID is required for YandexGPT")
        }

        val model = config.model.orEmpty().ifBlank { DEFAULT_YANDEX_MODEL }

        val request = YandexCompletionRequest(
            modelUri = "gpt://$folderId/$model",
            completionOptions = YandexCompletionOptions(
                stream = false,
                temperature = YANDEX_TEMPERATURE,
            ),
            messages = listOf(
                YandexMessage(
                    role = "user",
                    text = "Ping",
                )
            ),
        )

        processResponse(
            apiRequest = {
                yandexGptApi.completion(
                    authorization = "Api-Key ${config.apiKey}",
                    folderId = folderId,
                    request = request,
                )
            },
            mapper = { body ->
                val alternatives = body.result?.alternatives.orEmpty()
                if (alternatives.isEmpty()) {
                    throw AiUnauthorizedException("Unauthorized: Invalid API key or access denied.")
                }
            },
        )
    }

    private suspend fun requestYandex(
        config: AiRequestConfig,
        prompt: String,
    ): String {
        val folderId = config.apiFolder.orEmpty()
        if (folderId.isBlank()) {
            throw AiConfigurationException("Folder ID is required for YandexGPT")
        }

        val model = config.model.orEmpty().ifBlank { DEFAULT_YANDEX_MODEL }

        val prefixedPrompt = buildString {
            append(OPENAI_ROLE_PROMPT)
            append(prompt)
        }

        val request = YandexCompletionRequest(
            modelUri = "gpt://$folderId/$model",
            completionOptions = YandexCompletionOptions(
                stream = false,
                temperature = YANDEX_TEMPERATURE,
            ),
            messages = listOf(
                YandexMessage(
                    role = "user",
                    text = prefixedPrompt,
                )
            ),
        )

        return processResponse(
            apiRequest = {
                yandexGptApi.completion(
                    authorization = "Api-Key ${config.apiKey}",
                    folderId = folderId,
                    request = request,
                )
            },
            mapper = { body ->
                val alternatives = body.result?.alternatives.orEmpty()
                val text = alternatives.firstOrNull()?.message?.text
                text ?: throw AiClientException("No translation response received.")
            },
        )
    }

    private suspend fun <T, R> processResponse(
        apiRequest: suspend () -> Response<T>,
        mapper: (T) -> R,
    ): R {
        return result {
            val response = apiRequest()

            if (response.isSuccessful && response.body() != null) {
                mapper(response.body()!!)
            } else {
                when (response.code()) {
                    401, 403 -> throw AiUnauthorizedException(response.message())
                    else -> throw HttpException(response)
                }
            }
        }
            .getOrThrow()
    }

    private companion object {
        private const val DEFAULT_YANDEX_MODEL = "yandexgpt"
        private const val REASONING_MODEL = "gpt-5.1"
        private const val DEFAULT_OPENAI_MODEL = REASONING_MODEL
        private const val YANDEX_TEMPERATURE = 0.5

        private const val OPENAI_ROLE_PROMPT =
            "You are a school teacher. Explain the assignment to the student."
    }
}
