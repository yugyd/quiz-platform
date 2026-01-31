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

package com.yugyd.quiz.domain.aiconnection

import com.yugyd.quiz.ai.connection.api.AiConnectionClient
import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderTypeModel
import com.yugyd.quiz.ai.connection.api.model.NewAiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.UpdateAiConnectionModel
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.domain.aiconnection.model.AiTermCache
import com.yugyd.quiz.featuretoggle.domain.FeatureManager
import com.yugyd.quiz.featuretoggle.domain.model.FeatureToggle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AiConnectionInteractorImpl @Inject constructor(
    private val aiConnectionClient: AiConnectionClient,
    private val dispatcherProvider: DispatchersProvider,
    private val aiRemoteConfigSource: AiRemoteConfigSource,
    private val aiSettingsLocalSource: AiSettingsLocalSource,
    private val featureManager: FeatureManager,
) : AiConnectionInteractor {

    override suspend fun isActiveAiConnection() = withContext(dispatcherProvider.io) {
        featureManager.isFeatureEnabled(FeatureToggle.AI) &&
                aiSettingsLocalSource.subscribeToAiEnabled().first() &&
                aiConnectionClient.isActiveAiConnection()
    }

    override suspend fun subscribeCurrentAiConnection(): Flow<AiConnectionModel?> {
        return aiConnectionClient
            .subscribeToCurrentAiConnection()
            .flowOn(dispatcherProvider.io)
    }

    override suspend fun getCurrentAiConnection() = withContext(dispatcherProvider.io) {
        aiConnectionClient.subscribeToCurrentAiConnection().firstOrNull()
    }

    override suspend fun getAiConnection(
        aiConnectionId: String,
    ) = withContext(dispatcherProvider.io) {
        aiConnectionClient
            .getAllAiConnections()
            .firstOrNull { it.id == aiConnectionId }
    }

    override suspend fun activeAiConnection(id: String) = withContext(dispatcherProvider.io) {
        aiConnectionClient.activeAiConnection(id)
    }

    override suspend fun addAiConnection(
        model: NewAiConnectionModel,
    ) = withContext(dispatcherProvider.io) {
        aiConnectionClient.addAiConnection(model)
    }

    override suspend fun updateAiConnection(
        model: UpdateAiConnectionModel,
    ) = withContext(dispatcherProvider.io) {
        aiConnectionClient.updateAiConnection(model)
    }

    override suspend fun deleteAiConnection(id: String) = withContext(dispatcherProvider.io) {
        aiConnectionClient.deleteAiConnection(id)
    }

    override suspend fun getAllAiConnections() = withContext(dispatcherProvider.io) {
        aiConnectionClient.getAllAiConnections()
    }

    override suspend fun getAvailableAiProviders() = withContext(dispatcherProvider.io) {
        aiConnectionClient.getAvailableAiProviders().filterNot {
            it.type == AiConnectionProviderTypeModel.NONE
        }
    }

    override fun subscribeAiTermCache(): Flow<AiTermCache> {
        return aiSettingsLocalSource
            .subscribeToAiTermCache()
            .flowOn(dispatcherProvider.io)
    }

    override suspend fun addAiTermCache(model: AiTermCache) = withContext(dispatcherProvider.io) {
        aiSettingsLocalSource.addAiTermCache(model)
    }

    override suspend fun getAiTermCaches(): List<AiTermCache> {
        return AiTermCache.entries.toList()
    }

    override suspend fun getAiInstructionConfigs() = withContext(dispatcherProvider.io) {
        aiRemoteConfigSource.getAiInstructionConfigs()
    }

    override fun subscribeToDirectConnection(): Flow<Boolean> {
        return aiSettingsLocalSource
            .subscribeToDirectConnection()
            .flowOn(dispatcherProvider.io)
    }

    override suspend fun setDirectConnection(
        isDirectConnection: Boolean,
    ) = withContext(dispatcherProvider.io) {
        aiSettingsLocalSource.setDirectConnection(isDirectConnection)
    }

    override fun subscribeToAiEnabled(): Flow<Boolean> {
        return aiSettingsLocalSource
            .subscribeToAiEnabled()
            .flowOn(dispatcherProvider.io)
    }

    override suspend fun setAiEnabled(
        isAiEnabled: Boolean,
    ) = withContext(dispatcherProvider.io) {
        aiSettingsLocalSource.setAiEnabled(isAiEnabled)
    }

    override suspend fun getAiPrivacyUrl(): String? = withContext(dispatcherProvider.io) {
        aiRemoteConfigSource.getAiPrivacyUrl()
    }
}
