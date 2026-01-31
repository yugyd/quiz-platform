/*
 *    Copyright 2024 Roman Likhachev
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

package com.yugyd.quiz.ui.aiconnectiondetails

import androidx.lifecycle.SavedStateHandle
import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderTypeModel
import com.yugyd.quiz.ai.connection.api.model.NewAiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.UpdateAiConnectionModel
import com.yugyd.quiz.commonui.base.BaseViewModel
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.core.result
import com.yugyd.quiz.domain.aiconnection.AiConnectionInteractor
import com.yugyd.quiz.domain.aiconnection.model.AiInstructionConfig
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.Action
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.AiConnectionDetailsDomainState
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.NavigationState
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.SnackbarMessage
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.ToolbarTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AiConnectionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiConnectionInteractor: AiConnectionInteractor,
    logger: Logger,
    dispatchersProvider: DispatchersProvider,
) :
    BaseViewModel<State, Action>(
        logger = logger,
        dispatchersProvider = dispatchersProvider,
        initialState = State(
            domainState = AiConnectionDetailsDomainState(
                payload = AiConnectionDetailsArgs(savedStateHandle).aiConnectionDetailsPayload,
            ),
        ),
    ) {

    init {
        val toolbarTitle = if (screenState.domainState.payload.aiConnectionId != null) {
            ToolbarTitle.EDIT
        } else {
            ToolbarTitle.ADD
        }
        screenState = screenState.copy(
            toolbarTitle = toolbarTitle,
            isDeleteVisible = isEdit(),
        )

        loadData()
    }

    override fun handleAction(action: Action) {
        when (action) {
            is Action.OnApiKeyChanged -> {
                screenState = screenState.copy(apiKey = action.apiKey)
            }

            is Action.OnApiModelChanged -> {
                screenState = screenState.copy(apiModel = action.apiModel)
            }

            is Action.OnCloudProjectFolderChanged -> {
                screenState = screenState.copy(cloudProjectFolder = action.cloudProjectFolder)
            }

            Action.OnKeyInstructionClicked -> {
                onKeyInstructionClicked()
            }

            is Action.OnNameChanged -> {
                screenState = screenState.copy(name = action.name)
            }

            is Action.OnProviderSelected -> {
                changeProvider(action.provider)
            }

            Action.OnSaveClicked -> {
                saveAiConnection()
            }

            Action.OnBackPressed -> {
                screenState = screenState.copy(navigationState = NavigationState.Back)
            }

            Action.OnNavigationHandled -> {
                screenState = screenState.copy(navigationState = null)
            }

            Action.OnSnackbarDismissed -> {
                screenState = screenState.copy(showErrorMessage = null)
            }

            Action.OnDeleteClicked -> {
                deleteAiConnection()
            }
        }
    }

    private fun loadData() {
        vmScopeErrorHandled.launch {
            val domainState = screenState.domainState.copy(aiConnectionModel = null)
            screenState = screenState.copy(
                isLoading = true,
                isWarning = false,
                domainState = domainState,
            )

            result {
                val aiProviders = aiConnectionInteractor.getAvailableAiProviders()

                val apiKeyInstructionUrl = result {
                    aiConnectionInteractor.getAiInstructionConfigs()
                }
                    .getOrElse {
                        processError(it)

                        null
                    }

                val aiConnection = if (isEdit()) {
                    aiConnectionInteractor.getAiConnection(
                        aiConnectionId = requireNotNull(screenState.domainState.payload.aiConnectionId),
                    )
                } else {
                    null
                }

                LoadDataResult(
                    providers = aiProviders,
                    apiKeyAiInstructionConfigs = apiKeyInstructionUrl,
                    aiConnection = aiConnection,
                )
            }
                .onFailure {
                    processDataError(it)
                }
                .onSuccess { aiConnectionModel ->
                    processData(aiConnectionModel)
                }
        }
    }

    private fun processData(dataResult: LoadDataResult) {
        val domainState = screenState.domainState.copy(
            aiConnectionModel = dataResult.aiConnection,
            apiKeyAiInstructionConfigs = dataResult.apiKeyAiInstructionConfigs,
        )

        if (isEdit() && dataResult.aiConnection != null) {
            screenState = screenState.copy(
                isLoading = false,
                isWarning = false,
                domainState = domainState,
                name = dataResult.aiConnection.name,
                allProviders = dataResult.providers.map { it.name },
                apiKey = dataResult.aiConnection.apiKey,
                apiModel = dataResult.aiConnection.apiModel.orEmpty(),
                cloudProjectFolder = dataResult.aiConnection.apiCloudFolder.orEmpty(),
                isCloudProjectFolderVisible = dataResult.aiConnection.apiProvider.isNeedFolder,
            )
            val provider = dataResult.providers.first {
                dataResult.aiConnection.apiProvider == it.type
            }.name
            changeProvider(provider)
        } else {
            screenState = screenState.copy(
                isLoading = false,
                isWarning = false,
                domainState = domainState,
                allProviders = dataResult.providers.map { it.name },
            )
            changeProvider(dataResult.providers.first().name)
        }
    }

    private fun processDataError(error: Throwable) {
        val domainState = screenState.domainState.copy(
            aiConnectionModel = null,
            apiKeyAiInstructionConfigs = null,
        )
        screenState = screenState.copy(
            isLoading = false,
            isWarning = true,
            domainState = domainState,
            showErrorMessage = SnackbarMessage.ERROR,
        )
        processError(error)
    }

    private fun isEdit(): Boolean {
        return screenState.domainState.payload.aiConnectionId != null
    }

    private fun deleteAiConnection() {
        vmScopeErrorHandled.launch {
            result {
                aiConnectionInteractor.deleteAiConnection(
                    id = requireNotNull(screenState.domainState.payload.aiConnectionId),
                )
            }
                .onFailure {
                    processError(it)
                    screenState = screenState.copy(showErrorMessage = SnackbarMessage.ERROR)
                }
                .onSuccess {
                    screenState = screenState.copy(navigationState = NavigationState.Back)
                }
        }
    }

    private fun onKeyInstructionClicked() {
        val apiKeyAiInstructionConfigs = screenState.domainState.apiKeyAiInstructionConfigs
        val aiInstructionUrl = apiKeyAiInstructionConfigs?.firstOrNull {
            it.id == AiConnectionProviderTypeModel.fromQualifier(screenState.provider)
        }?.url

        if (aiInstructionUrl != null) {
            screenState = screenState.copy(
                navigationState = NavigationState.NavigateToExternalBrowser(aiInstructionUrl),
            )
        }
    }

    private fun saveAiConnection() {
        vmScopeErrorHandled.launch {
            if (
                screenState.name.isBlank() ||
                screenState.provider.isBlank() ||
                screenState.apiKey.isBlank() ||
                checkApiCloudFolderIfNeed()
            ) {
                screenState = screenState.copy(showErrorMessage = SnackbarMessage.FILL_FIELDS)
                return@launch
            }


            result {
                if (isEdit()) {
                    val updateAiConnection = UpdateAiConnectionModel(
                        id = requireNotNull(screenState.domainState.payload.aiConnectionId),
                        name = screenState.name,
                        apiKey = screenState.apiKey,
                        apiCloudFolder = screenState.cloudProjectFolder,
                        apiModel = screenState.apiModel.ifBlank { null },
                    )
                    aiConnectionInteractor.updateAiConnection(updateAiConnection)
                } else {
                    val addAiConnection = NewAiConnectionModel(
                        name = screenState.name,
                        apiProvider = AiConnectionProviderTypeModel.fromQualifier(screenState.provider),
                        apiKey = screenState.apiKey,
                        apiCloudFolder = screenState.cloudProjectFolder,
                        apiModel = screenState.apiModel.ifBlank { null },
                    )
                    aiConnectionInteractor.addAiConnection(addAiConnection)
                }
            }
                .onFailure {
                    processSaveError(it)
                }
                .onSuccess {
                    processSaveData()
                }
        }
    }

    private fun checkApiCloudFolderIfNeed() = if (screenState.isCloudProjectFolderVisible) {
        screenState.cloudProjectFolder.isBlank()
    } else {
        false
    }

    private fun processSaveError(error: Throwable) {
        processError(error)
        screenState = screenState.copy(showErrorMessage = SnackbarMessage.ERROR)
    }

    private fun processSaveData() {
        screenState = screenState.copy(navigationState = NavigationState.Back)
    }

    private fun changeProvider(provider: String) {
        screenState = screenState.copy(
            provider = provider,
            isCloudProjectFolderVisible = screenState.domainState.apiKeyAiInstructionConfigs
                ?.firstOrNull { it.id == AiConnectionProviderTypeModel.fromQualifier(provider) }
                ?.id?.isNeedFolder ?: false,
        )
    }

    private data class LoadDataResult(
        val providers: List<AiConnectionProviderModel>,
        val apiKeyAiInstructionConfigs: List<AiInstructionConfig>?,
        val aiConnection: AiConnectionModel?,
    )
}
