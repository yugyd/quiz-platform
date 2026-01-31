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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yugyd.quiz.game.api.LeaderboardType
import com.yugyd.quiz.ui.profile.ProfileView.Action
import com.yugyd.quiz.ui.profile.ProfileView.State
import com.yugyd.quiz.ui.profile.ProfileView.State.NavigationState
import com.yugyd.quiz.ui.profile.model.ProfileUiModel
import com.yugyd.quiz.ui.profile.model.SwitchItemProfileUiModel
import com.yugyd.quiz.uikit.LoadingContent
import com.yugyd.quiz.uikit.WarningContent
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.component.RootToolbar
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.R as UiKitR

@Composable
internal fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToTelegram: (String) -> Unit,
    onNavigateToTransition: () -> Unit,
    onNavigateToExternalReportError: () -> Unit,
    onNavigateToGooglePlay: () -> Unit,
    onNavigateToOtherApps: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToProOnboarding: () -> Unit,
    onNavigateToShare: () -> Unit,
    onNavigateToContents: () -> Unit,
    onNavigateToExternalPlatformRate: () -> Unit,
    onNavigateToExternalPlatformReportError: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToRating: (LeaderboardType) -> Unit,
    onNavigateToAiSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = state,
        onItemClicked = {
            viewModel.onAction(Action.OnProfileClicked(it))
        },
        onItemChecked = { model, isChecked ->
            viewModel.onAction(Action.OnProfileItemChecked(model, isChecked))
        },
        onRatePlatformClicked = {
            viewModel.onAction(Action.OnRatePlatformClicked)
        },
        onReportBugPlatformClicked = {
            viewModel.onAction(Action.OnReportBugPlatformClicked)
        },
        onNavigateToTelegram = onNavigateToTelegram,
        onNavigateToTransition = onNavigateToTransition,
        onNavigateToExternalReportError = onNavigateToExternalReportError,
        onNavigateToGooglePlay = onNavigateToGooglePlay,
        onNavigateToOtherApps = onNavigateToOtherApps,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToProOnboarding = onNavigateToProOnboarding,
        onNavigateToShare = onNavigateToShare,
        onNavigateToContents = onNavigateToContents,
        onTelegramHandled = {
            viewModel.onAction(Action.OnTelegramHandled)
        },
        onNavigateToExternalPlatformRate = onNavigateToExternalPlatformRate,
        onNavigateToExternalPlatformReportError = onNavigateToExternalPlatformReportError,
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
        onNavigateToTasks = onNavigateToTasks,
        onNavigateToRating = onNavigateToRating,
        onNavigateToAiSettings = onNavigateToAiSettings,
    )
}

@Composable
internal fun ProfileScreen(
    uiState: State,
    onItemClicked: (ProfileUiModel) -> Unit,
    onItemChecked: (SwitchItemProfileUiModel, Boolean) -> Unit,
    onRatePlatformClicked: () -> Unit,
    onReportBugPlatformClicked: () -> Unit,
    onNavigateToTelegram: (String) -> Unit,
    onNavigateToTransition: () -> Unit,
    onNavigateToExternalReportError: () -> Unit,
    onNavigateToGooglePlay: () -> Unit,
    onNavigateToOtherApps: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToProOnboarding: () -> Unit,
    onNavigateToShare: () -> Unit,
    onNavigateToContents: () -> Unit,
    onNavigateToExternalPlatformRate: () -> Unit,
    onNavigateToExternalPlatformReportError: () -> Unit,
    onTelegramHandled: () -> Unit,
    onNavigationHandled: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToRating: (LeaderboardType) -> Unit,
    onNavigateToAiSettings: () -> Unit,
) {
    LaunchedEffect(key1 = uiState.showTelegram) {
        if (uiState.showTelegram) {
            onNavigateToTelegram(uiState.telegramLink)

            onTelegramHandled()
        }
    }

    Column {
        RootToolbar(
            title = stringResource(id = UiKitR.string.ds_navbar_title_profile),
        )

        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.isWarning -> {
                WarningContent()
            }

            else -> {
                ProfileContent(
                    items = uiState.items,
                    onItemClicked = onItemClicked,
                    onItemChecked = onItemChecked,
                    onRatePlatformClicked = onRatePlatformClicked,
                    onReportBugPlatformClicked = onReportBugPlatformClicked,
                )
            }
        }
    }

    NavigationHandler(
        navigationState = uiState.navigationState,
        onNavigateToTransition = onNavigateToTransition,
        onNavigateToExternalReportError = onNavigateToExternalReportError,
        onNavigateToGooglePlay = onNavigateToGooglePlay,
        onNavigateToOtherApps = onNavigateToOtherApps,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToProOnboarding = onNavigateToProOnboarding,
        onNavigateToShare = onNavigateToShare,
        onNavigateToContents = onNavigateToContents,
        onNavigateToExternalPlatformRate = onNavigateToExternalPlatformRate,
        onNavigateToExternalPlatformReportError = onNavigateToExternalPlatformReportError,
        onNavigateToTasks = onNavigateToTasks,
        onNavigateToRating = onNavigateToRating,
        onNavigateToAiSettings = onNavigateToAiSettings,
        onNavigationHandled = onNavigationHandled,
    )
}

@Composable
internal fun ProfileContent(
    items: List<ProfileUiModel>,
    onItemClicked: (ProfileUiModel) -> Unit,
    onItemChecked: (SwitchItemProfileUiModel, Boolean) -> Unit,
    onRatePlatformClicked: () -> Unit,
    onReportBugPlatformClicked: () -> Unit,
) {
    LazyColumn {
        items(
            items = items,
            key = ProfileUiModel::id
        ) {
            ProfileItem(
                model = it,
                onItemClicked = onItemClicked,
                onItemChecked = onItemChecked,
                onRatePlatformClicked = onRatePlatformClicked,
                onReportBugPlatformClicked = onReportBugPlatformClicked,
            )
        }
    }
}

@Composable
internal fun NavigationHandler(
    navigationState: NavigationState?,
    onNavigateToTransition: () -> Unit,
    onNavigateToExternalReportError: () -> Unit,
    onNavigateToGooglePlay: () -> Unit,
    onNavigateToOtherApps: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToProOnboarding: () -> Unit,
    onNavigateToShare: () -> Unit,
    onNavigateToContents: () -> Unit,
    onNavigateToExternalPlatformRate: () -> Unit,
    onNavigateToExternalPlatformReportError: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToRating: (LeaderboardType) -> Unit,
    onNavigationHandled: () -> Unit,
) {
    LaunchedEffect(key1 = navigationState) {
        when (navigationState) {
            NavigationState.NavigateToExternalReportError -> onNavigateToExternalReportError()
            NavigationState.NavigateToGooglePlay -> onNavigateToGooglePlay()
            NavigationState.NavigateToOtherApps -> onNavigateToOtherApps()
            NavigationState.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
            NavigationState.NavigateToProOnboarding -> onNavigateToProOnboarding()
            NavigationState.NavigateToShare -> onNavigateToShare()
            NavigationState.NavigateToTransition -> onNavigateToTransition()
            NavigationState.NavigateToContents -> onNavigateToContents()
            NavigationState.NavigateToExternalPlatformRate -> onNavigateToExternalPlatformRate()
            NavigationState.NavigateToExternalPlatformReportError -> onNavigateToExternalPlatformReportError()
            NavigationState.NavigateToTasks -> onNavigateToTasks()
            NavigationState.NavigateToAiSettings -> onNavigateToAiSettings()
            NavigationState.NavigateToTotalRating -> {
                onNavigateToRating(LeaderboardType.WALKTHROUGH)
            }

            NavigationState.NavigateToExperienceRating -> {
                onNavigateToRating(LeaderboardType.EXPERIENCE)
            }

            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

@ThemePreviews
@Composable
private fun ContentPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) items: List<ProfileUiModel>,
) {
    QuizApplicationTheme {
        QuizBackground {
            ProfileContent(
                items = items,
                onItemClicked = {},
                onItemChecked = { _, _ -> },
                onRatePlatformClicked = {},
                onReportBugPlatformClicked = {},
            )
        }
    }
}
