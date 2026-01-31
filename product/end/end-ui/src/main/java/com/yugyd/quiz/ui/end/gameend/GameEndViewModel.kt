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

package com.yugyd.quiz.ui.end.gameend

import androidx.lifecycle.SavedStateHandle
import com.yugyd.quiz.ad.api.AdErrorDomainModel
import com.yugyd.quiz.commonui.base.BaseViewModel
import com.yugyd.quiz.core.AdIdJvmProvider
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.core.runCatch
import com.yugyd.quiz.domain.api.model.Mode
import com.yugyd.quiz.domain.api.model.payload.GameEndPayload
import com.yugyd.quiz.domain.api.payload.ErrorListPayload
import com.yugyd.quiz.domain.questai.AiQuestInteractor
import com.yugyd.quiz.domain.theme.ThemeInteractor
import com.yugyd.quiz.featuretoggle.domain.FeatureManager
import com.yugyd.quiz.featuretoggle.domain.model.FeatureToggle
import com.yugyd.quiz.ui.end.EndArgs
import com.yugyd.quiz.ui.end.gameend.GameEndView.Action
import com.yugyd.quiz.ui.end.gameend.GameEndView.State
import com.yugyd.quiz.ui.end.gameend.GameEndView.State.InterstitialAdState
import com.yugyd.quiz.ui.end.gameend.GameEndView.State.NavigationState
import com.yugyd.quiz.ui.end.gameend.model.GameEndUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GameEndViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val themeInteractor: ThemeInteractor,
    private val gameEndUiMapper: GameEndUiMapper,
    private val featureManager: FeatureManager,
    private val logger: Logger,
    dispatchersProvider: DispatchersProvider,
    private val adIdJvmProvider: AdIdJvmProvider,
    private val aiQuestInteractor: AiQuestInteractor,
) :
    BaseViewModel<State, Action>(
        logger = logger,
        dispatchersProvider = dispatchersProvider,
        initialState = State(
            payload = EndArgs(savedStateHandle).payload,
        )
    ) {

    init {
        loadData()
    }

    override fun handleAction(action: Action) {
        when (action) {
            Action.OnBackClicked -> onBackClicked()
            Action.OnNewGameClicked -> onNewGameClicked()
            Action.OnShowErrorsClicked -> onShowErrorsClicked()
            Action.OnNavigationHandled -> {
                screenState = screenState.copy(navigationState = null)
            }

            Action.OnSnackbarDismissed -> {
                screenState = screenState.copy(showInterstitialErrorMessage = false)
            }

            // Ad
            Action.OnInterstitialAdClosed -> loadAd(screenState.isAdFeatureEnabled)

            is Action.OnInterstitialAdFailedToLoad -> onInterstitialAdFailedToLoad(action.adError)

            is Action.OnInterstitialAdFailedToShow -> onInterstitialAdFailedToLoad(action.adError)

            Action.OnInterstitialAdLoaded -> {
                screenState = screenState.copy(interstitialAdState = InterstitialAdState.IS_LOADED)
            }

            Action.OnInterstitialAdNotToLoad -> {
                screenState = screenState.copy(interstitialAdState = InterstitialAdState.NOT_LOADED)
            }
        }
    }

    private fun onBackClicked() = navigateToBack()

    private fun onNewGameClicked() = navigateToBack()

    private fun onShowErrorsClicked() {
        val payload = ErrorListPayload(
            mode = screenState.payload.mode,
            themeId = screenState.payload.themeId,
            errorQuestIds = screenState.payload.errorQuestIds,
        )
        screenState = screenState.copy(
            navigationState = NavigationState.NavigateToErrorList(payload),
        )
    }

    private fun loadData() {
        vmScopeErrorHandled.launch {
            screenState = screenState.copy(isLoading = true)

            val isAdFeatureEnabled = featureManager.isFeatureEnabled(FeatureToggle.AD) &&
                    featureManager.isFeatureEnabled(FeatureToggle.AD_INTERSTITIAL_GAME_END)

            runCatch(
                block = {
                    val themeTitle = getThemeTitle(screenState.payload)
                    val state = gameEndUiMapper.map(
                        payload = screenState.payload,
                        themeTitle = themeTitle,
                        isAdFeatureEnabled = isAdFeatureEnabled
                    )
                    processData(state)
                },
                catch = {
                    screenState = screenState.copy(isLoading = false)
                    processError(it)
                }
            )

            loadAd(isAdFeatureEnabled)
        }
    }

    private suspend fun getThemeTitle(payload: GameEndPayload) = when (payload.mode) {
        Mode.ARCADE, Mode.TRAIN -> {
            payload.themeId?.let { themeId ->
                themeInteractor.getTheme(themeId).name
            }.orEmpty()
        }

        Mode.AI_TASKS -> {
            // Bad
            payload.themeId?.let { themeId ->
                aiQuestInteractor.getThemeDetail(themeId).name
            }.orEmpty()
        }

        Mode.ERROR, Mode.FAVORITE, Mode.NONE -> ""
    }


    private fun processData(state: State) {
        screenState = state
    }

    private fun loadAd(isAdFeatureEnabled: Boolean) {
        if (isAdFeatureEnabled) {
            screenState = screenState.copy(
                interstitialAdState = InterstitialAdState.LOAD_AD,
                interstitialAdUnitId = adIdJvmProvider.gameEndInterstitialAdId(),
            )
        }
    }

    private fun navigateToBack() {
        val adState = if (isAdEnabledAndLoaded()) {
            InterstitialAdState.SHOW_AD
        } else {
            screenState.interstitialAdState
        }

        screenState = screenState.copy(
            navigationState = NavigationState.Back,
            interstitialAdState = adState,
        )
    }

    private fun isAdEnabledAndLoaded() = isAdEnabled() &&
            screenState.interstitialAdState == InterstitialAdState.IS_LOADED

    private fun isAdEnabled() = screenState.isAdFeatureEnabled &&
            !screenState.payload.isRewardedSuccess &&
            !screenState.payload.isBlockedInterstitial

    private fun onInterstitialAdFailedToLoad(adError: AdErrorDomainModel? = null) {
        adError?.let {
            logger.print(TAG, "Reward ad is failed: $it")
        }
        screenState = screenState.copy(interstitialAdState = InterstitialAdState.NOT_LOADED)
    }

    companion object {
        private const val TAG = "GameEndViewModel"
    }
}
