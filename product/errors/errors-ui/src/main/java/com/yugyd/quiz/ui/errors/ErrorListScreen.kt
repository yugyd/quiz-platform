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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yugyd.quiz.domain.api.model.tasks.TaskModel
import com.yugyd.quiz.domain.api.payload.AiClientPayload
import com.yugyd.quiz.ui.errors.ErrorListView.Action
import com.yugyd.quiz.ui.errors.ErrorListView.State.NavigationState
import com.yugyd.quiz.ui.favorites.FavoriteIcon
import com.yugyd.quiz.uikit.LoadingContent
import com.yugyd.quiz.uikit.WarningContent
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.component.SimpleToolbar
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.R as UiKitR

@Composable
internal fun ErrorListRoute(
    viewModel: ErrorListViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToAi: (AiClientPayload) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ErrorListScreen(
        uiState = state,
        snackbarHostState = snackbarHostState,
        onBackPressed = {
            viewModel.onAction(Action.OnBackClicked)
        },
        onItemClicked = {
            viewModel.onAction(Action.OnErrorClicked(it))
        },
        onFavoriteClicked = {
            viewModel.onAction(Action.OnFavoriteClicked(it))
        },
        onErrorDismissState = {
            viewModel.onAction(Action.OnSnackbarDismissed)
        },
        onBack = onBack,
        onNavigateToBrowser = onNavigateToBrowser,
        onNavigateToAi = onNavigateToAi,
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
    )
}

@Composable
internal fun ErrorListScreen(
    uiState: ErrorListView.State,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit,
    onItemClicked: (TaskModel) -> Unit,
    onFavoriteClicked: (TaskModel) -> Unit,
    onErrorDismissState: () -> Unit,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToAi: (AiClientPayload) -> Unit,
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
            title = stringResource(id = R.string.errors_title_error_list),
            onBackPressed = onBackPressed,
        )

        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.isWarning -> {
                WarningContent()
            }

            else -> {
                ErrorListContent(
                    items = uiState.items,
                    onItemClicked = onItemClicked,
                    onFavoriteClicked = onFavoriteClicked,
                )
            }
        }
    }

    NavigationHandler(
        navigationState = uiState.navigationState,
        onBack = onBack,
        onNavigateToBrowser = onNavigateToBrowser,
        onNavigateToAi = onNavigateToAi,
        onNavigationHandled = onNavigationHandled,
    )
}

@Composable
internal fun ErrorListContent(
    items: List<TaskModel>,
    onItemClicked: (TaskModel) -> Unit,
    onFavoriteClicked: (TaskModel) -> Unit,
) {
    LazyColumn {
        items(
            items = items, key = TaskModel::id
        ) {
            ErrorItem(
                model = it,
                onItemClicked = onItemClicked,
                onFavoriteClicked = onFavoriteClicked,
            )
        }
    }
}

@Composable
internal fun ErrorItem(
    model: TaskModel,
    onFavoriteClicked: (TaskModel) -> Unit,
    onItemClicked: (TaskModel) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    onItemClicked(model)
                }
                .fillMaxWidth()
                .padding(all = 16.dp),
        ) {
            Column(
                modifier = Modifier.weight(weight = 1F),
            ) {
                Text(
                    text = stringResource(id = R.string.errors_title_quest),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = model.quest,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.errors_title_true_answer),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = model.trueAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (model.isFavoriteEnabled) {
                Spacer(modifier = Modifier.width(width = 8.dp))

                FavoriteIcon(
                    isFavorite = model.isFavorite,
                    onFavoriteClicked = {
                        onFavoriteClicked(model)
                    },
                )
            }
        }
    }

    Divider()
}

@Composable
internal fun NavigationHandler(
    navigationState: NavigationState?,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToAi: (AiClientPayload) -> Unit,
    onNavigationHandled: () -> Unit,
) {
    LaunchedEffect(key1 = navigationState) {
        when (navigationState) {
            NavigationState.Back -> onBack()
            is NavigationState.NavigateToExternalBrowser -> onNavigateToBrowser(navigationState.url)
            is NavigationState.NavigateToAiScreen -> {
                val payload = AiClientPayload(
                    prompt = navigationState.prompt,
                    fallbackWebLink = navigationState.fallbackWebLink,
                    title = navigationState.title,
                )
                onNavigateToAi(payload)
            }

            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

@ThemePreviews
@Composable
private fun ContentPreview(
    @PreviewParameter(ErrorListPreviewParameterProvider::class) items: List<TaskModel>,
) {
    QuizApplicationTheme {
        QuizBackground {
            ErrorListContent(
                items = items,
                onItemClicked = {},
                onFavoriteClicked = {},
            )
        }
    }
}
