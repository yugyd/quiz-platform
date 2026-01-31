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

package com.yugyd.quiz.domain.aidirectconnection

import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.domain.aidirectconnection.model.AiRequestConfig
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AiClientInteractorImpl @Inject constructor(
    private val remoteSource: AiClientRemoteSource,
    private val dispatcherProvider: DispatchersProvider,
) : AiClientInteractor {

    override suspend fun validateAi(
        config: AiRequestConfig,
    ) = withContext(dispatcherProvider.io) {
        remoteSource.validateAi(config)
    }

    override suspend fun translate(
        config: AiRequestConfig,
        prompt: String,
    ): String = withContext(dispatcherProvider.io) {
        remoteSource.request(config, prompt)
    }
}
