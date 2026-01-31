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

package com.yugyd.quiz.ui.profile

import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.commonui.base.BaseViewModel
import com.yugyd.quiz.core.ContentProvider
import com.yugyd.quiz.core.GlobalConfig
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.core.runCatch
import com.yugyd.quiz.domain.aiconnection.AiConnectionInteractor
import com.yugyd.quiz.domain.api.repository.ContentSource
import com.yugyd.quiz.domain.content.ContentInteractor
import com.yugyd.quiz.domain.content.api.ContentModel
import com.yugyd.quiz.domain.controller.TransitionController
import com.yugyd.quiz.domain.options.OptionsInteractor
import com.yugyd.quiz.featuretoggle.domain.FeatureManager
import com.yugyd.quiz.featuretoggle.domain.RemoteConfigRepository
import com.yugyd.quiz.featuretoggle.domain.model.FeatureToggle
import com.yugyd.quiz.featuretoggle.domain.model.telegram.TelegramConfig
import com.yugyd.quiz.game.api.GameClient
import com.yugyd.quiz.ui.profile.ProfileView.Action
import com.yugyd.quiz.ui.profile.ProfileView.State
import com.yugyd.quiz.ui.profile.ProfileView.State.NavigationState
import com.yugyd.quiz.ui.profile.model.ProfileUiMapper
import com.yugyd.quiz.ui.profile.model.ProfileUiModel
import com.yugyd.quiz.ui.profile.model.SwitchItemProfileUiModel
import com.yugyd.quiz.ui.profile.model.TypeProfile
import com.yugyd.quiz.ui.profile.model.ValueItemProfileUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val optionsInteractor: OptionsInteractor,
    private val transitionController: TransitionController,
    private val profileUiMapper: ProfileUiMapper,
    private val contentSource: ContentSource,
    private val featureManager: FeatureManager,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val contentProvider: ContentProvider,
    private val contentInteractor: ContentInteractor,
    private val aiConnectionInteractor: AiConnectionInteractor,
    logger: Logger,
    dispatchersProvider: DispatchersProvider,
    private val gameClient: GameClient,
) :
    BaseViewModel<State, Action>(
        logger = logger,
        dispatchersProvider = dispatchersProvider,
        initialState = State(),
    ),
    TransitionController.Listener {

    private var loadDataJob: Job? = null

    init {
        transitionController.subscribe(this)

        initData()

        loadContent()

        loadAiConnection()
    }

    private fun loadContent() {
        loadDataJob?.cancel()
        loadDataJob = vmScopeErrorHandled.launch {
            contentInteractor
                .subscribeToSelectedContent()
                .catch {
                    processError(it)
                }
                .collect {
                    processContent(it)
                }
        }
    }

    private fun processContent(newContent: ContentModel?) {
        val newItems = screenState.items.mapNotNull {
            if (
                it.type == TypeProfile.SELECT_CONTENT &&
                it is ValueItemProfileUiModel
            ) {
                profileUiMapper.mapContentToValueItem(
                    contentTitle = newContent?.name,
                    isContentEnabled = screenState.isProFeatureEnabled
                )
            } else {
                it
            }
        }
        screenState = screenState.copy(items = newItems)
    }

    private fun loadAiConnection() {
        vmScopeErrorHandled.launch {
            aiConnectionInteractor
                .subscribeCurrentAiConnection()
                .combine(aiConnectionInteractor.subscribeToAiEnabled()) { aiConnection, isAiEnabled ->
                    LoadAiConnectionResult(
                        currentAiConnection = aiConnection,
                        isAiEnabled = isAiEnabled,
                    )
                }
                .catch {
                    processError(it)
                }
                .collect {
                    processAiConnection(it)
                }
        }
    }

    private fun processAiConnection(loadAiConnectionResult: LoadAiConnectionResult) {
        val newItems = screenState.items.mapNotNull {
            if (
                it.type == TypeProfile.AI_CONNECTION &&
                it is ValueItemProfileUiModel
            ) {
                profileUiMapper.mapAiToValueItem(
                    aiName = loadAiConnectionResult.currentAiConnection?.name,
                    isAiFeatureEnabled = screenState.isAiFeatureEnabled,
                    isAiEnabled = loadAiConnectionResult.isAiEnabled,
                )
            } else {
                it
            }
        }
        screenState = screenState.copy(
            items = newItems,
        )
    }

    override fun onCleared() {
        transitionController.unsubscribe(this)
        super.onCleared()
    }

    override fun onTransitionChanged() {
        initData()
    }

    override fun handleAction(action: Action) {
        when (action) {
            is Action.OnProfileClicked -> onProfileClicked(action.item)
            is Action.OnProfileItemChecked -> onProfileItemChecked(action.item, action.isChecked)
            Action.OnNavigationHandled -> {
                screenState = screenState.copy(navigationState = null)
            }

            Action.OnTelegramHandled -> {
                screenState = screenState.copy(
                    showTelegram = false,
                    telegramLink = "",
                )
            }

            Action.OnRatePlatformClicked -> {
                screenState = screenState.copy(
                    navigationState = NavigationState.NavigateToExternalPlatformRate,
                )
            }

            Action.OnReportBugPlatformClicked -> {
                screenState = screenState.copy(
                    navigationState = NavigationState.NavigateToExternalPlatformReportError,
                )
            }
        }
    }

    private fun onProfileClicked(item: ProfileUiModel) = when (item.type) {
        TypeProfile.TRANSITION -> {
            navigateToScreen(NavigationState.NavigateToTransition)
        }

        TypeProfile.PRO -> {
            navigateToProOnboarding()
        }

        TypeProfile.RESTORE_PURCHASE -> {
            restorePurchases()
        }

        TypeProfile.RATE_APP -> {
            navigateToScreen(NavigationState.NavigateToGooglePlay)
        }

        TypeProfile.SHARE -> {
            navigateToScreen(NavigationState.NavigateToShare)
        }

        TypeProfile.OTHER_APPS -> {
            navigateToScreen(NavigationState.NavigateToOtherApps)
        }

        TypeProfile.REPORT_ERROR -> {
            navigateToScreen(NavigationState.NavigateToExternalReportError)
        }

        TypeProfile.PRIVACY_POLICY -> {
            navigateToScreen(NavigationState.NavigateToPrivacyPolicy)
        }

        TypeProfile.TELEGRAM_SOCIAL -> openTelegram()

        TypeProfile.SELECT_CONTENT -> {
            navigateToScreen(NavigationState.NavigateToContents)
        }

        TypeProfile.SORT_QUEST, TypeProfile.VIBRATION, TypeProfile.OPEN_SOURCE -> Unit

        TypeProfile.SETTINGS_SECTION,
        TypeProfile.PURCHASES_SECTION,
        TypeProfile.PLEASE_US_SECTION,
        TypeProfile.FEEDBACK_SECTION,
        TypeProfile.SOCIAL_SECTION,
        TypeProfile.AI_SECTION,
        TypeProfile.PROFILE_HEADER,
        TypeProfile.GAME_PROFILE_SECTION,
        TypeProfile.NONE -> Unit

        TypeProfile.TASKS -> {
            navigateToScreen(NavigationState.NavigateToTasks)
        }

        TypeProfile.PROGRESS_RATING -> {
            navigateToScreen(NavigationState.NavigateToTotalRating)
        }

        TypeProfile.EXPERIENCE_RATING -> {
            navigateToScreen(NavigationState.NavigateToExperienceRating)
        }

        TypeProfile.AI_CONNECTION -> {
            navigateToScreen(NavigationState.NavigateToAiSettings)
        }
    }

    private fun onProfileItemChecked(item: SwitchItemProfileUiModel, isChecked: Boolean) =
        when (item.type) {
            TypeProfile.SORT_QUEST -> {
                changeSwitch(item, isChecked) { optionsInteractor.isSortingQuest = isChecked }
            }

            TypeProfile.VIBRATION -> {
                changeSwitch(item, isChecked) { optionsInteractor.isVibration = isChecked }
            }

            else -> Unit
        }

    private fun initData() {
        screenState = screenState.copy(
            isLoading = true,
            isWarning = false,
            items = emptyList(),
        )

        vmScopeErrorHandled.launch {
            runCatch(
                block = {
                    val isAiFeatureEnabled = featureManager.isFeatureEnabled(FeatureToggle.AI)
                    val isProEnabled = featureManager.isFeatureEnabled(FeatureToggle.PRO)
                    val isTelegramEnabled = featureManager.isFeatureEnabled(FeatureToggle.TELEGRAM)
                    val isGameServicesEnabled = featureManager.isFeatureEnabled(
                        FeatureToggle.GAME_SERVICES,
                    ) && gameClient.isAvailable()

                    val telegramConfig = remoteConfigRepository.fetchTelegramConfig()
                    val contentTitle = contentInteractor.getSelectedContent()?.name
                    val isBasedOnPlatformApp = GlobalConfig.IS_BASED_ON_PLATFORM_APP
                    val aiConnection = aiConnectionInteractor
                        .subscribeCurrentAiConnection()
                        .firstOrNull()
                    val isAiEnabled = aiConnectionInteractor
                        .subscribeToAiEnabled()
                        .firstOrNull() ?: false
                    val aiConnectionResult = LoadAiConnectionResult(
                        currentAiConnection = aiConnection,
                        isAiEnabled = isAiEnabled,
                    )

                    processState(
                        isAiFeatureEnabled = isAiFeatureEnabled,
                        isProEnabled = isProEnabled,
                        isTelegramEnabled = isTelegramEnabled,
                        telegramConfig = telegramConfig,
                        contentTitle = contentTitle,
                        isBasedOnPlatformApp = isBasedOnPlatformApp,
                        aiConnectionResult = aiConnectionResult,
                        isGameServicesEnabled = isGameServicesEnabled,
                    )
                },
                catch = ::processDataError
            )
        }
    }

    private fun processState(
        isAiFeatureEnabled: Boolean,
        isProEnabled: Boolean,
        isTelegramEnabled: Boolean,
        telegramConfig: TelegramConfig?,
        contentTitle: String?,
        isBasedOnPlatformApp: Boolean,
        aiConnectionResult: LoadAiConnectionResult,
        isGameServicesEnabled: Boolean,
    ) {
        screenState = screenState.copy(
            isProFeatureEnabled = isProEnabled,
            isAiFeatureEnabled = isAiFeatureEnabled,
            items = profileUiMapper.map(
                content = contentSource.getContent(),
                transition = optionsInteractor.transition,
                isSortedQuest = optionsInteractor.isSortingQuest,
                isVibration = optionsInteractor.isVibration,
                isProFeatureEnabled = isProEnabled,
                isTelegramFeatureEnabled = isTelegramEnabled,
                telegramConfig = telegramConfig,
                contentTitle = contentTitle,
                isBasedOnPlatformApp = isBasedOnPlatformApp,
                isContentFeatureEnabled = !GlobalConfig.IS_BASED_ON_PLATFORM_APP,
                isAiFeatureEnabled = isAiFeatureEnabled,
                currentAiTitle = aiConnectionResult.currentAiConnection?.name,
                isAiEnabled = aiConnectionResult.isAiEnabled,
                isGamesServicesEnabled = isGameServicesEnabled,
            ),
            isWarning = false,
            isLoading = false
        )
    }

    private fun processDataError(error: Throwable) {
        screenState = screenState.copy(
            isLoading = false,
            isWarning = true,
            items = emptyList(),
        )
        processError(error)
    }

    private fun changeSwitch(
        item: SwitchItemProfileUiModel,
        isChecked: Boolean,
        finallyBlock: () -> Unit
    ) {
        if (item.isChecked == isChecked) {
            return
        }

        val mappedItems = screenState.items.map {
            if (it is SwitchItemProfileUiModel && it.id == item.id) {
                it.copy(isChecked = isChecked)
            } else {
                it
            }
        }
        screenState = screenState.copy(items = mappedItems)

        finallyBlock()
    }

    private fun restorePurchases() = Unit

    private fun navigateToScreen(
        navigationState: NavigationState,
    ) {
        screenState = screenState.copy(navigationState = navigationState)
    }

    private fun navigateToProOnboarding() {
        if (screenState.isProFeatureEnabled) {
            screenState = screenState.copy(
                navigationState = NavigationState.NavigateToProOnboarding
            )
        }
    }

    private fun openTelegram() {
        vmScopeErrorHandled.launch {
            val link = contentProvider.getTelegramChannel()
            screenState = screenState.copy(
                showTelegram = true,
                telegramLink = link,
            )
        }
    }

    private data class LoadAiConnectionResult(
        val currentAiConnection: AiConnectionModel?,
        val isAiEnabled: Boolean,
    )
}
