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

package com.yugyd.quiz.ui.errors

import androidx.lifecycle.SavedStateHandle
import com.yugyd.quiz.commonui.base.BaseViewModel
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.core.runCatch
import com.yugyd.quiz.domain.aiconnection.AiConnectionInteractor
import com.yugyd.quiz.domain.aitasks.AiTasksInteractor
import com.yugyd.quiz.domain.api.model.Mode
import com.yugyd.quiz.domain.api.model.tasks.TaskModel
import com.yugyd.quiz.domain.errors.ErrorInteractor
import com.yugyd.quiz.domain.favorites.FavoritesInteractor
import com.yugyd.quiz.featuretoggle.domain.FeatureManager
import com.yugyd.quiz.featuretoggle.domain.model.FeatureToggle
import com.yugyd.quiz.ui.errors.ErrorListView.Action
import com.yugyd.quiz.ui.errors.ErrorListView.State
import com.yugyd.quiz.ui.errors.ErrorListView.State.NavigationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ErrorListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val errorInteractor: ErrorInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val aiTasksInteractor: AiTasksInteractor,
    private val aiConnectionInteractor: AiConnectionInteractor,
    private val featureManager: FeatureManager,
    logger: Logger,
    dispatchersProvider: DispatchersProvider,
) :
    BaseViewModel<State, Action>(
        logger = logger,
        dispatchersProvider = dispatchersProvider,
        initialState = State(
            payload = ErrorListArgs(savedStateHandle).errorListPayload,
        )
    ) {

    init {
        loadData()
    }

    override fun handleAction(action: Action) {
        when (action) {
            Action.OnBackClicked -> onBackClicked()
            is Action.OnErrorClicked -> onErrorClicked(action.item)
            Action.OnSnackbarDismissed -> {
                screenState = screenState.copy(showErrorMessage = false)
            }

            Action.OnNavigationHandled -> {
                screenState = screenState.copy(navigationState = null)
            }

            is Action.OnFavoriteClicked -> onFavoriteClicked(action.item)
        }
    }

    private fun onBackClicked() {
        screenState = screenState.copy(navigationState = NavigationState.Back)
    }

    private fun onErrorClicked(item: TaskModel) {
        vmScopeErrorHandled.launch {
            if (
                featureManager.isFeatureEnabled(FeatureToggle.AI_ASSISTANT) &&
                aiConnectionInteractor.isActiveAiConnection()
            ) {
                val prompt = aiTasksInteractor.generateAiPromptForErrorTask(
                    quest = item.quest,
                    trueAnswer = item.trueAnswer,
                )

                screenState = screenState.copy(
                    navigationState = NavigationState.NavigateToAiScreen(
                        prompt = prompt,
                        fallbackWebLink = item.queryLink,
                        title = item.quest,
                    )
                )
            } else {
                screenState = screenState.copy(
                    navigationState = NavigationState.NavigateToExternalBrowser(item.queryLink),
                )
            }
        }
    }

    private fun loadData() {
        vmScopeErrorHandled.launch {
            screenState = screenState.copy(
                isLoading = true,
                isWarning = false,
                items = emptyList(),
            )

            runCatch(
                block = {
                    val errorQuestIds = screenState.payload.errorQuestIds
                    val themeId = screenState.payload.themeId
                    val taskModels = when (screenState.payload.mode) {
                        Mode.ARCADE, Mode.TRAIN, Mode.ERROR, Mode.FAVORITE -> {
                            val errors = errorInteractor.getErrorsModels(errorQuestIds)
                            val favorites = favoritesInteractor.getFavorites(errorQuestIds)
                            errors.map { task ->
                                if (favorites.contains(task.id)) {
                                    task.copy(isFavorite = true)
                                } else {
                                    task
                                }
                            }
                        }

                        Mode.AI_TASKS -> {
                            if (themeId != null) {
                                errorInteractor
                                    .getErrorModelsFromAiTasks(
                                        themeId = themeId,
                                        errors = errorQuestIds,
                                    )
                                    .map { it.copy(isFavoriteEnabled = false) }
                            } else {
                                emptyList()
                            }
                        }

                        Mode.NONE -> error("Mode not valid")
                    }


                    processData(taskModels)
                },
                catch = ::processDataError,
            )
        }
    }

    private fun processData(models: List<TaskModel>) {
        screenState = screenState.copy(
            isLoading = false,
            isWarning = false,
            items = models,
        )
    }

    private fun processDataError(error: Throwable) {
        screenState = screenState.copy(
            isLoading = false,
            isWarning = true,
            items = emptyList(),
            showErrorMessage = true,
        )
        processError(error)
    }

    private fun onFavoriteClicked(item: TaskModel) {
        vmScopeErrorHandled.launch {
            runCatch(
                block = {
                    screenState = screenState.copy(
                        items = screenState.items.map { task ->
                            if (task.id == item.id) {
                                task.copy(isFavorite = task.isFavorite.not())
                            } else {
                                task
                            }
                        }
                    )

                    val currentTask = screenState.items.first { it.id == item.id }
                    favoritesInteractor.updateTask(
                        id = currentTask.id,
                        isFavorite = currentTask.isFavorite,
                    )
                },
                catch = {
                    processError(it)
                    screenState = screenState.copy(showErrorMessage = true)
                }
            )
        }
    }
}
