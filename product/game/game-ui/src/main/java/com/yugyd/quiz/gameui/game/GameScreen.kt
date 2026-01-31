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

package com.yugyd.quiz.gameui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yugyd.quiz.ad.api.AdErrorDomainModel
import com.yugyd.quiz.commonui.utils.ProgressUtils
import com.yugyd.quiz.domain.api.model.payload.GameEndPayload
import com.yugyd.quiz.gameui.R
import com.yugyd.quiz.gameui.game.GameView.Action
import com.yugyd.quiz.gameui.game.GameView.State
import com.yugyd.quiz.gameui.game.GameView.State.BannerState
import com.yugyd.quiz.gameui.game.GameView.State.NavigationState
import com.yugyd.quiz.gameui.game.model.ConditionUiModel
import com.yugyd.quiz.gameui.game.model.ControlUiModel
import com.yugyd.quiz.gameui.game.model.GameStateUiModel
import com.yugyd.quiz.ui.enteraiquest.EnterAiQuestContent
import com.yugyd.quiz.ui.enteraiquest.EnterAiQuestUiModel
import com.yugyd.quiz.ui.enterquest.EnterQuestContent
import com.yugyd.quiz.ui.enterquest.EnterQuestUiModel
import com.yugyd.quiz.ui.game.api.model.BaseQuestUiModel
import com.yugyd.quiz.ui.game.api.model.QuestUiType
import com.yugyd.quiz.ui.hintenterquest.EnterWithHintQuestContent
import com.yugyd.quiz.ui.hintenterquest.EnterWithHintQuestUiModel
import com.yugyd.quiz.ui.selectboolquest.SelectBoolQuestContent
import com.yugyd.quiz.ui.selectboolquest.SelectBoolQuestUiModel
import com.yugyd.quiz.ui.selectmanualquest.SelectManualQuestContent
import com.yugyd.quiz.ui.selectmanualquest.SelectManualQuestUiModel
import com.yugyd.quiz.ui.selectquest.SelectQuestContent
import com.yugyd.quiz.ui.selectquest.SelectQuestUiModel
import com.yugyd.quiz.ui.simplequest.LatexSimpleQuestContent
import com.yugyd.quiz.ui.simplequest.LatexSimpleQuestUiModel
import com.yugyd.quiz.ui.simplequest.SimpleQuestContent
import com.yugyd.quiz.ui.simplequest.SimpleQuestUiModel
import com.yugyd.quiz.uikit.LoadingContent
import com.yugyd.quiz.uikit.WarningContent
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.component.SimpleToolbar
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.R as UiKitR

/**
 * TODO Implement logic with ads - https://yudyd.atlassian.net/browse/QUIZ-203
 */
@Composable
internal fun GameRoute(
    viewModel: GameViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigateToProOnboarding: () -> Unit,
    onNavigateToProgressEnd: (GameEndPayload) -> Unit,
    onNavigateToGameEnd: (GameEndPayload) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GameScreen(
        uiState = state,
        snackbarHostState = snackbarHostState,
        onBackPressed = {
            viewModel.onAction(Action.OnBackPressed)
        },
        onAnswerClicked = {
            viewModel.onAction(Action.OnAnswerClicked)
        },
        onAnswerTextChanged = {
            viewModel.onAction(Action.OnAnswerTextChanged(it))
        },
        onAnswerSelected = { answer, isSelected ->
            viewModel.onAction(
                Action.OnAnswerSelected(
                    userAnswer = answer,
                    isSelected = isSelected,
                ),
            )
        },
        onErrorDismissState = {
            viewModel.onAction(Action.OnSnackbarDismissed)
        },
        onDebugAnswerToastDismissed = {
            viewModel.onAction(Action.OnDebugAnswerToastDismissed)
        },
        onScrollToTopAnimationEnded = {
            viewModel.onAction(Action.OnScrollToTopAnimationEnded)
        },
        onPositiveRewardDialogClicked = {
            viewModel.onAction(Action.OnPositiveRewardDialogClicked)
        },
        onNegativeRewardDialogClicked = {
            viewModel.onAction(Action.OnNegativeRewardDialogClicked)
        },
        onRewardDialogDismissed = {
            viewModel.onAction(Action.OnRewardDialogDismissed)
        },
        onErrorVibrationEnded = {
            viewModel.onAction(Action.OnErrorVibrationEnded)
        },
        onProBannerClicked = {
            viewModel.onAction(Action.OnProBannerClicked)
        },
        onNavigateToProOnboarding = onNavigateToProOnboarding,
        onNavigateToProgressEnd = onNavigateToProgressEnd,
        onNavigateToGameEnd = onNavigateToGameEnd,
        onBack = onBack,
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
        onBannerAdLoaded = {
            viewModel.onAction(Action.OnBannerAdLoaded)
        },
        onBannerAdFailedToLoad = {
            viewModel.onAction(Action.OnBannerAdFailedToLoad(it))
        },
    )
}

@Composable
internal fun GameScreen(
    uiState: State,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit,
    onAnswerClicked: () -> Unit,
    onAnswerTextChanged: (String) -> Unit,
    onAnswerSelected: (String, Boolean) -> Unit,
    onErrorDismissState: () -> Unit,
    onDebugAnswerToastDismissed: () -> Unit,
    onScrollToTopAnimationEnded: () -> Unit,
    onPositiveRewardDialogClicked: () -> Unit,
    onNegativeRewardDialogClicked: () -> Unit,
    onRewardDialogDismissed: () -> Unit,
    onErrorVibrationEnded: () -> Unit,
    onProBannerClicked: () -> Unit,
    onNavigateToProOnboarding: () -> Unit,
    onNavigateToProgressEnd: (GameEndPayload) -> Unit,
    onNavigateToGameEnd: (GameEndPayload) -> Unit,
    onBack: () -> Unit,
    onNavigationHandled: () -> Unit,
    onBannerAdLoaded: () -> Unit,
    onBannerAdFailedToLoad: (AdErrorDomainModel) -> Unit,
) {
    val errorMessage = stringResource(id = UiKitR.string.ds_error_base)
    LaunchedEffect(key1 = uiState.showErrorMessage) {
        if (uiState.showErrorMessage) {
            snackbarHostState.showSnackbar(message = errorMessage)

            onErrorDismissState()
        }
    }

    val rewardedAdNotLoadMessage = stringResource(id = R.string.game_error_reward_not_loaded)
    LaunchedEffect(key1 = uiState.showRewardedAdNotLoadMessage) {
        if (uiState.showRewardedAdNotLoadMessage) {
            snackbarHostState.showSnackbar(message = rewardedAdNotLoadMessage)

            onErrorDismissState()
        }
    }

    LaunchedEffect(uiState.debugTrueAnswer) {
        if (uiState.debugTrueAnswer != null) {
            snackbarHostState.showSnackbar(message = uiState.debugTrueAnswer)

            onDebugAnswerToastDismissed()
        }
    }

    if (uiState.showRewardedDialog) {
        RewardedDialog(
            onPositiveRewardDialogClicked = onPositiveRewardDialogClicked,
            onNegativeRewardDialogClicked = onNegativeRewardDialogClicked,
            onRewardDialogDismissState = onRewardDialogDismissed,
        )
    }

    if (uiState.startErrorVibration) {
        // TODO Implement vibration - https://yudyd.atlassian.net/browse/QUIZ-205
        onErrorVibrationEnded()
    }

    Column {
        val control = uiState.control
        GameToolbar(
            title = control.countTitle,
            onBackPressed = onBackPressed,
            isConditionIconVisible = control.conditionUiModel == ConditionUiModel.LIFE,
            conditionIcon = control.conditionIcon,
            conditionIconColor = uiState.control.conditionTintColor,
        )

        when {
            uiState.isLoading -> {
                LoadingContent(modifier = Modifier.weight(weight = 1F))
            }

            uiState.isWarning -> {
                WarningContent(modifier = Modifier.weight(weight = 1F))
            }

            uiState.quest != null -> {
                GameContent(
                    modifier = Modifier.weight(weight = 1F),
                    control = control,
                    quest = uiState.quest,
                    manualAnswer = uiState.manualAnswer,
                    scrollToTop = uiState.scrollToTopAnimation,
                    onAnswerClicked = onAnswerClicked,
                    onAnswerTextChanged = onAnswerTextChanged,
                    onAnswerSelected = onAnswerSelected,
                    onScrollToTopAnimationEnded = onScrollToTopAnimationEnded,
                )
            }
        }

        BannerAd(
            bannerState = uiState.bannerState,
            onProBannerClicked = onProBannerClicked,
            onBannerAdLoaded = onBannerAdLoaded,
            onBannerAdFailedToLoad = onBannerAdFailedToLoad,
        )
    }

    NavigationHandler(
        navigationState = uiState.navigationState,
        onNavigateToProOnboarding = onNavigateToProOnboarding,
        onNavigateToProgressEnd = onNavigateToProgressEnd,
        onNavigateToGameEnd = onNavigateToGameEnd,
        onBack = onBack,
        onNavigationHandled = onNavigationHandled,
    )
}

@Composable
private fun BannerAd(
    bannerState: BannerState?,
    onProBannerClicked: () -> Unit,
    onBannerAdLoaded: () -> Unit,
    onBannerAdFailedToLoad: (AdErrorDomainModel) -> Unit,
) {
    if (bannerState == null) {
        return
    }

    AdContainer(
        bannerState = bannerState,
        onProBannerClicked = onProBannerClicked,
        onBannerAdLoaded = onBannerAdLoaded,
        onBannerAdFailedToLoad = onBannerAdFailedToLoad,
    )
}

@Composable
internal fun GameToolbar(
    title: String,
    onBackPressed: () -> Unit,
    isConditionIconVisible: Boolean,
    conditionIcon: ImageVector? = null,
    conditionIconColor: Color? = null,
) {
    val finalRightIcon = if (isConditionIconVisible) {
        conditionIcon
    } else {
        null
    }

    SimpleToolbar(
        title = title,
        onBackPressed = onBackPressed,
        rightIcon = finalRightIcon,
        rightIconColor = conditionIconColor,
        onRightIconClicked = {},
    )
}

@Composable
internal fun GameContent(
    modifier: Modifier = Modifier,
    control: ControlUiModel,
    quest: BaseQuestUiModel,
    manualAnswer: String, scrollToTop: Boolean,
    onAnswerClicked: () -> Unit,
    onAnswerSelected: (String, Boolean) -> Unit,
    onAnswerTextChanged: (String) -> Unit,
    onScrollToTopAnimationEnded: () -> Unit,
) {
    Column(modifier = modifier) {
        val animatedProgress by animateFloatAsState(
            targetValue = ProgressUtils.toFloatPercent(control.progressPercent),
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "ProgressFloatAnimation",
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth(),
            color = control.progressColor,
        )

        val scrollState = rememberScrollState()
        LaunchedEffect(key1 = scrollToTop) {
            if (scrollToTop) {
                scrollState.animateScrollTo(value = 0)

                onScrollToTopAnimationEnded()
            }
        }

        Column(
            modifier = Modifier
                .weight(weight = 1F)
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            when (quest.type) {
                QuestUiType.SIMPLE -> {
                    val uiModel = remember(quest) {
                        quest as SimpleQuestUiModel
                    }
                    SimpleQuestContent(
                        quest = uiModel,
                        onAnswerClicked = {
                            onAnswerSelected(it, true)
                            onAnswerClicked()
                        },
                    )
                }

                QuestUiType.SIMPLE_LATEX -> {
                    val uiModel = remember(quest) {
                        quest as LatexSimpleQuestUiModel
                    }
                    LatexSimpleQuestContent(
                        quest = uiModel,
                        onAnswerClicked = {
                            onAnswerSelected(it, true)
                            onAnswerClicked()
                        },
                    )
                }

                QuestUiType.ENTER -> {
                    val uiModel = remember(quest) {
                        quest as EnterQuestUiModel
                    }
                    EnterQuestContent(
                        quest = uiModel,
                        manualAnswer = manualAnswer,
                        onAnswerHandler = onAnswerClicked,
                        onAnswerTextChanged = onAnswerTextChanged,
                    )
                }

                QuestUiType.ENTER_WITH_HINT -> {
                    val uiModel = remember(quest) {
                        quest as EnterWithHintQuestUiModel
                    }
                    EnterWithHintQuestContent(
                        quest = uiModel,
                        manualAnswer = manualAnswer,
                        onAnswerHandler = onAnswerClicked,
                        onAnswerTextChanged = onAnswerTextChanged,
                    )
                }

                QuestUiType.SELECT_MANUAL -> {
                    SelectManualQuestContent(
                        quest = quest as SelectManualQuestUiModel,
                        onAnswerClicked = onAnswerClicked,
                        onAnswerSelected = onAnswerSelected,
                    )
                }

                QuestUiType.SELECT -> {
                    SelectQuestContent(
                        quest = quest as SelectQuestUiModel,
                        onAnswerSelected = onAnswerSelected,
                    )
                }

                QuestUiType.ENTER_AI -> {
                    val uiModel = remember(quest) {
                        quest as EnterAiQuestUiModel
                    }
                    EnterAiQuestContent(
                        quest = uiModel,
                        manualAnswer = manualAnswer,
                        onAnswerHandler = onAnswerClicked,
                        onAnswerTextChanged = onAnswerTextChanged,
                    )
                }

                QuestUiType.SELECT_BOOL -> {
                    val uiModel = remember(quest) {
                        quest as SelectBoolQuestUiModel
                    }
                    SelectBoolQuestContent(
                        quest = uiModel,
                        onAnswerClicked = {
                            onAnswerSelected(it, true)
                            onAnswerClicked()
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun RewardedDialog(
    onPositiveRewardDialogClicked: () -> Unit,
    onNegativeRewardDialogClicked: () -> Unit,
    onRewardDialogDismissState: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onRewardDialogDismissState()
        },
        title = {
            Text(text = stringResource(id = R.string.game_title_add_life))
        },
        text = {
            Text(text = stringResource(id = R.string.game_msg_add_life))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPositiveRewardDialogClicked()
                    onRewardDialogDismissState()
                }
            ) {
                Text(text = stringResource(id = R.string.game_action_watch))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onNegativeRewardDialogClicked()
                    onRewardDialogDismissState()
                }
            ) {
                Text(text = stringResource(id = R.string.game_action_next))
            }
        }
    )
}

@Composable
internal fun NavigationHandler(
    navigationState: NavigationState?,
    onNavigateToProOnboarding: () -> Unit,
    onNavigateToProgressEnd: (GameEndPayload) -> Unit,
    onNavigateToGameEnd: (GameEndPayload) -> Unit,
    onBack: () -> Unit,
    onNavigationHandled: () -> Unit,
) {
    LaunchedEffect(key1 = navigationState) {
        when (navigationState) {
            NavigationState.Back -> onBack()
            is NavigationState.NavigateToGameEnd -> onNavigateToGameEnd(navigationState.payload)
            NavigationState.NavigateToPro -> onNavigateToProOnboarding()
            is NavigationState.NavigateToProgressEnd -> {
                onNavigateToProgressEnd(navigationState.payload)
            }

            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

@ThemePreviews
@Composable
private fun ContentPreview(
    @PreviewParameter(GamePreviewParameterProvider::class) gameData: List<GameStateUiModel>,
) {
    QuizApplicationTheme {
        QuizBackground {
            val gameDataItem = gameData.first { it.quest.type == QuestUiType.SELECT_BOOL }
            GameContent(
                control = gameDataItem.control,
                quest = gameDataItem.quest,
                manualAnswer = "Manual",
                scrollToTop = false,
                onAnswerClicked = {},
                onScrollToTopAnimationEnded = {},
                onAnswerTextChanged = {},
                onAnswerSelected = { _, _ -> },
            )
        }
    }
}
