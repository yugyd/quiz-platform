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

package com.yugyd.quiz.ai.connection.api

import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderTypeModel
import com.yugyd.quiz.ai.connection.api.model.NewAiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.UpdateAiConnectionModel
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Singleton
internal class AiConnectionClientImpl @Inject constructor(
    private val aiConnectionLocalSource: AiConnectionLocalSource,
    private val logger: Logger,
) : AiConnectionClient {

    private val connectionsMutex = Mutex()

    override suspend fun isActiveAiConnection(): Boolean {
        connectionsMutex.withLock {
            return aiConnectionLocalSource
                .getConnections()
                .firstOrNull(AiConnectionModel::isActive) != null
        }
    }

    override fun subscribeToCurrentAiConnection(): Flow<AiConnectionModel?> {
        return aiConnectionLocalSource
            .subscribeToConnections()
            .map {
                it.firstOrNull(AiConnectionModel::isActive)
            }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun addAiConnection(model: NewAiConnectionModel): Boolean {
        connectionsMutex.withLock {
            val currentConnections = aiConnectionLocalSource
                .getConnections()
                .map { it.copy(isActive = false) }

            val newId = model.apiKey + model.apiProvider
            val base64NewId = Base64.encodeToByteArray(source = newId.toByteArray()).toString()
            val newConnection = AiConnectionModel(
                id = base64NewId,
                name = model.name,
                isActive = true,
                apiProvider = model.apiProvider,
                apiKey = model.apiKey,
                apiCloudFolder = model.apiCloudFolder,
                isValid = null,
                apiModel = model.apiModel,
            )

            val newConnections = currentConnections.plus(newConnection)

            return result {
                aiConnectionLocalSource.setConnections(newConnections)
                true
            }
                .getOrElse {
                    logger.logError(it)
                    false
                }
        }
    }

    override suspend fun activeAiConnection(id: String): Boolean {
        connectionsMutex.withLock {
            val currentConnections = aiConnectionLocalSource.getConnections()

            val newConnections = currentConnections.map {
                if (it.id == id) {
                    it.copy(isActive = true)
                } else {
                    it.copy(isActive = false)
                }
            }

            return result {
                aiConnectionLocalSource.setConnections(newConnections)
                true
            }
                .getOrElse {
                    logger.logError(it)
                    false
                }
        }
    }

    override suspend fun updateAiConnection(model: UpdateAiConnectionModel): Boolean {
        connectionsMutex.withLock {
            val currentConnections = aiConnectionLocalSource.getConnections()

            val currentConnection = currentConnections.firstOrNull { it.id == model.id }

            if (currentConnection == null) {
                return false
            } else {
                val newCurrentConnection = currentConnection.copy(
                    name = model.name,
                    apiKey = model.apiKey,
                    apiCloudFolder = model.apiCloudFolder,
                    apiModel = model.apiModel,
                )
                val newConnections = currentConnections.map {
                    if (it.id == model.id) {
                        newCurrentConnection
                    } else {
                        it
                    }
                }

                return result {
                    aiConnectionLocalSource.setConnections(newConnections)
                    true
                }
                    .getOrElse {
                        logger.logError(it)
                        false
                    }
            }
        }
    }

    override suspend fun deleteAiConnection(id: String): Boolean {
        connectionsMutex.withLock {
            val currentConnections = aiConnectionLocalSource.getConnections()

            val newConnections = currentConnections.filter { it.id != id }

            return result {
                aiConnectionLocalSource.setConnections(newConnections)
                true
            }
                .getOrElse {
                    logger.logError(it)
                    false
                }
        }
    }

    override suspend fun getAllAiConnections(): List<AiConnectionModel> {
        connectionsMutex.withLock {
            return aiConnectionLocalSource.getConnections()
        }
    }

    override suspend fun getAvailableAiProviders(): List<AiConnectionProviderModel> {
        connectionsMutex.withLock {
            return AiConnectionProviderTypeModel.entries.map {
                AiConnectionProviderModel(
                    name = it.qualifier,
                    type = it,
                )
            }
        }
    }
}
