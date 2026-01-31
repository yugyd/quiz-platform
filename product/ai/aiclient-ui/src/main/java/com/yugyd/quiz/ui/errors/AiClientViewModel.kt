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

package com.yugyd.quiz.ui.errors

import androidx.lifecycle.SavedStateHandle
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderTypeModel
import com.yugyd.quiz.commonui.base.BaseViewModel
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.core.runCatch
import com.yugyd.quiz.domain.aiconnection.AiConnectionInteractor
import com.yugyd.quiz.domain.aidirectconnection.AiClientInteractor
import com.yugyd.quiz.domain.aidirectconnection.model.AiProvider
import com.yugyd.quiz.domain.aidirectconnection.model.AiRequestConfig
import com.yugyd.quiz.ui.errors.AiClientView.Action
import com.yugyd.quiz.ui.errors.AiClientView.State
import com.yugyd.quiz.ui.errors.AiClientView.State.NavigationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AiClientViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val aiConnectionInteractor: AiConnectionInteractor,
    private val aiClientInteractor: AiClientInteractor,
    logger: Logger,
    dispatchersProvider: DispatchersProvider,
) :
    BaseViewModel<State, Action>(
        logger = logger,
        dispatchersProvider = dispatchersProvider,
        initialState = State(
            payload = AiClientArgs(savedStateHandle).aiClientPayload,
        )
    ) {

    init {
        loadData()
    }

    override fun handleAction(action: Action) {
        when (action) {
            Action.OnBackClicked -> onBackClicked()

            Action.OnSnackbarDismissed -> {
                screenState = screenState.copy(showErrorMessage = false)
            }

            Action.OnNavigationHandled -> {
                screenState = screenState.copy(navigationState = null)
            }

            Action.OnOpenWebClicked -> {
                screenState = screenState.copy(
                    navigationState = NavigationState.NavigateToWeb(
                        fallbackWebLink = screenState.payload.fallbackWebLink,
                    )
                )
            }
        }
    }

    private fun onBackClicked() {
        screenState = screenState.copy(navigationState = NavigationState.Back)
    }

    private fun loadData() {
        vmScopeErrorHandled.launch {
            screenState = screenState.copy(
                isLoading = true,
                isWarning = false,
                mdData = "",
                title = screenState.payload.title,
            )

            runCatch(
                block = {
                    if (aiConnectionInteractor.isActiveAiConnection()) {
                        val current = aiConnectionInteractor.getCurrentAiConnection()
                            ?: throw IllegalStateException("No active AI connection")

                        val config = AiRequestConfig(
                            provider = when (current.apiProvider) {
                                AiConnectionProviderTypeModel.YANDEX -> AiProvider.YANDEX
                                AiConnectionProviderTypeModel.CHAT_GPT -> AiProvider.OPENAI
                                AiConnectionProviderTypeModel.NONE -> {
                                    throw IllegalStateException("No active AI connection")
                                }
                            },
                            apiKey = current.apiKey,
                            apiFolder = current.apiCloudFolder,
                            model = null,
                        )

                        val mdResult = aiClientInteractor.translate(
                            prompt = screenState.payload.prompt,
                            config = config,
                        )

                        processData(mdResult)
                    } else {
                        throw IllegalStateException("No active AI connection")
                    }
                },
                catch = ::processDataError,
            )
        }
    }

    private fun processData(mdData: String) {
        screenState = screenState.copy(
            isLoading = false,
            isWarning = false,
            mdData = mdData,
        )
    }

    private fun processDataError(error: Throwable) {
        screenState = screenState.copy(
            isLoading = false,
            isWarning = true,
            mdData = "",
            showErrorMessage = true,
        )
        processError(error)
    }
}
