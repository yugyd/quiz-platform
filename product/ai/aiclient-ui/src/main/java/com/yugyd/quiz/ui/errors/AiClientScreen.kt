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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.yugyd.quiz.ui.errors.AiClientView.Action
import com.yugyd.quiz.ui.errors.AiClientView.State
import com.yugyd.quiz.ui.errors.AiClientView.State.NavigationState
import com.yugyd.quiz.uikit.LoadingContent
import com.yugyd.quiz.uikit.WarningContent
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.component.SimpleToolbar
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.R as UiKitR

@Composable
internal fun AiClientRoute(
    viewModel: AiClientViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigateToBrowser: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AiClientScreen(
        uiState = state,
        snackbarHostState = snackbarHostState,
        onBackPressed = {
            viewModel.onAction(Action.OnBackClicked)
        },
        onErrorDismissState = {
            viewModel.onAction(Action.OnSnackbarDismissed)
        },
        onOpenWebClicked = {
            viewModel.onAction(Action.OnOpenWebClicked)
        },
        onBack = onBack,
        onNavigateToBrowser = onNavigateToBrowser,
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
    )
}

@Composable
internal fun AiClientScreen(
    uiState: State,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit,
    onOpenWebClicked: () -> Unit,
    onErrorDismissState: () -> Unit,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigationHandled: () -> Unit,
) {
    val errorMessage = stringResource(id = UiKitR.string.ds_error_base)
    LaunchedEffect(key1 = uiState.showErrorMessage) {
        if (uiState.showErrorMessage) {
            snackbarHostState.showSnackbar(message = errorMessage)

            onErrorDismissState()
        }
    }

    Column {
        SimpleToolbar(
            title = stringResource(id = R.string.ai_client_title_ai),
            onBackPressed = onBackPressed,
            rightIcon = Icons.Filled.OpenInBrowser,
            onRightIconClicked = onOpenWebClicked,
        )

        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.isWarning -> {
                WarningContent(
                    isRetryButtonEnabled = true,
                    retryTitle = stringResource(R.string.ai_client_action_open_in_web),
                    onRetryClicked = onOpenWebClicked,
                )
            }

            else -> {
                AiClientContent(
                    mdData = uiState.mdData,
                )
            }
        }
    }

    NavigationHandler(
        navigationState = uiState.navigationState,
        onBack = onBack,
        onNavigateToBrowser = onNavigateToBrowser,
        onNavigationHandled = onNavigationHandled,
    )
}

@Composable
internal fun AiClientContent(
    mdData: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp,
            )
            .verticalScroll(rememberScrollState()),
    ) {
        Markdown(
            modifier = Modifier.fillMaxWidth(),
            content = mdData,
        )
    }
}

@Composable
internal fun NavigationHandler(
    navigationState: NavigationState?,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigationHandled: () -> Unit,
) {
    LaunchedEffect(key1 = navigationState) {
        when (navigationState) {
            NavigationState.Back -> onBack()
            is NavigationState.NavigateToWeb -> onNavigateToBrowser(navigationState.fallbackWebLink)
            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

@ThemePreviews
@Composable
private fun ContentPreview(
    @PreviewParameter(AiClientPreviewParameterProvider::class) mdData: String,
) {
    QuizApplicationTheme {
        QuizBackground {
            AiClientContent(
                mdData = mdData,
            )
        }
    }
}
