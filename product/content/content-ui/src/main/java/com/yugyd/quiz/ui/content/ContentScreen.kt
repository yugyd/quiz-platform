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

package com.yugyd.quiz.ui.content

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yugyd.quiz.domain.api.model.Theme
import com.yugyd.quiz.domain.content.api.ContentModel
import com.yugyd.quiz.domain.content.exceptions.ContentVerificationException
import com.yugyd.quiz.domain.content.exceptions.DuplicateIdQuestsException
import com.yugyd.quiz.domain.content.exceptions.DuplicateIdThemesException
import com.yugyd.quiz.domain.content.exceptions.NotValidQuestsException
import com.yugyd.quiz.domain.content.exceptions.NotValidThemesException
import com.yugyd.quiz.domain.game.api.model.Quest
import com.yugyd.quiz.ui.content.ContentView.Action
import com.yugyd.quiz.ui.content.ContentView.State
import com.yugyd.quiz.ui.content.ContentView.State.NavigationState
import com.yugyd.quiz.ui.content.ContentView.State.SnackbarState
import com.yugyd.quiz.uikit.LoadingContent
import com.yugyd.quiz.uikit.WarningContent
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.IconWithBackground
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.component.SimpleToolbar
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme

@Composable
internal fun ContentRoute(
    viewModel: ContentViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ContentScreen(
        uiState = state,
        snackbarHostState = snackbarHostState,
        onBackPressed = {
            viewModel.onAction(Action.OnBackClicked)
        },
        onItemClicked = {
            viewModel.onAction(Action.OnItemClicked(it))
        },
        onOpenFileClicked = {
            viewModel.onAction(Action.OnOpenFileClicked)
        },
        onContentFormatClicked = {
            viewModel.onAction(Action.OnContentFormatClicked)
        },
        onSnackbarDismissed = {
            viewModel.onAction(Action.OnSnackbarDismissed)
        },
        onBack = onBack,
        onNavigateToBrowser = onNavigateToBrowser,
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
        onStartFileProviderHandled = {
            viewModel.onAction(Action.OnStartFileProviderHandled)
        },
        onDocumentResult = {
            viewModel.onAction(Action.OnDocumentResult(uri = it?.toString()))
        }
    )
}

@Composable
internal fun ContentScreen(
    uiState: State,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit,
    onItemClicked: (ContentModel) -> Unit,
    onOpenFileClicked: () -> Unit,
    onContentFormatClicked: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigationHandled: () -> Unit,
    onStartFileProviderHandled: () -> Unit,
    onDocumentResult: (Uri?) -> Unit,
) {
    SnackbarMessageEffect(
        snackbarHostState = snackbarHostState,
        snackbarState = uiState.snackbarState,
        onSnackbarDismissState = onSnackbarDismissed,
    )

    FileProviderEffect(
        startFileProvider = uiState.startFileProvider,
        onStartFileProviderHandled = onStartFileProviderHandled,
        onDocumentResult = onDocumentResult,
    )

    Column {
        if (uiState.isBackEnabled) {
            SimpleToolbar(
                title = stringResource(id = R.string.content_title),
                onBackPressed = onBackPressed,
            )
        }

        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.isWarning -> {
                WarningContent()
            }

            uiState.items.isNullOrEmpty() -> {
                EmptyStateContent(
                    onOpenFileClicked = onOpenFileClicked,
                    onContentFormatClicked = onContentFormatClicked,
                )
            }

            else -> {
                ContentContent(
                    items = requireNotNull(uiState.items),
                    onItemClicked = onItemClicked,
                    onOpenFileClicked = onOpenFileClicked,
                    onContentFormatClicked = onContentFormatClicked
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
internal fun SnackbarMessageEffect(
    snackbarHostState: SnackbarHostState,
    snackbarState: SnackbarState?,
    onSnackbarDismissState: () -> Unit,
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)

    LaunchedEffect(key1 = snackbarState) {
        val msg = when (snackbarState) {
            SnackbarState.NotAddedContentIsExists -> {
                currentContext.getString(R.string.content_error_not_added_content_is_exists)
            }

            SnackbarState.UriIsNull -> {
                currentContext.getString(R.string.content_error_invalid_uri)
            }

            SnackbarState.AddIsFailed -> {
                currentContext.getString(R.string.content_error_add_is_failed)
            }

            SnackbarState.DeleteIsFailed -> {
                currentContext.getString(R.string.content_error_delete_is_failed)
            }

            SnackbarState.NotSelectAndDelete -> {
                currentContext.getString(R.string.content_error_not_select_and_delete)
            }

            SnackbarState.SelectIsFailed -> {
                currentContext.getString(R.string.content_error_select_is_failed)
            }

            is SnackbarState.VerifyError -> {
                snackbarState.error.mapToMessage(context)
            }

            SnackbarState.OneItemNotDelete -> {
                currentContext.getString(R.string.content_error_one_item_not_delete)
            }

            SnackbarState.SelectedItemNotDelete -> {
                currentContext.getString(R.string.content_error_selected_item_not_delete)
            }

            SnackbarState.ContentFormatUrlNotLoaded -> {
                currentContext.getString(R.string.content_error_content_format_url_not_loaded)
            }

            null -> null
        }

        msg?.let { snackbarHostState.showSnackbar(message = it) }
        snackbarState?.let { onSnackbarDismissState() }
    }
}

private const val TEXT_PLAIN_MIME_TYPE = "text/plain"

@Composable
internal fun FileProviderEffect(
    startFileProvider: Boolean,
    onStartFileProviderHandled: () -> Unit,
    onDocumentResult: (Uri?) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        onDocumentResult(it)
    }
    LaunchedEffect(key1 = startFileProvider) {
        if (startFileProvider) {
            launcher.launch(arrayOf(TEXT_PLAIN_MIME_TYPE))

            onStartFileProviderHandled()
        }
    }
}

@Composable
internal fun EmptyStateContent(
    onOpenFileClicked: () -> Unit,
    onContentFormatClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconWithBackground(
            modifier = Modifier.size(size = 96.dp),
            icon = R.drawable.ic_file_open_24,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.content_empty_state_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.content_empty_state_message),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        TextButton(
            onClick = onContentFormatClicked,
            shape = RectangleShape,
            contentPadding = PaddingValues(all = 0.dp),
        ) {
            Text(
                text = stringResource(id = R.string.content_empty_state_note),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onOpenFileClicked,
        ) {
            Text(text = stringResource(id = R.string.content_empty_state_button))
        }
    }
}

@Composable
internal fun ContentContent(
    items: List<ContentModel>,
    onItemClicked: (ContentModel) -> Unit,
    onOpenFileClicked: () -> Unit,
    onContentFormatClicked: () -> Unit,
) {
    LazyColumn {
        item(
            key = ContentType.NEW_FILE_HEADER.name,
            contentType = ContentType.NEW_FILE_HEADER,
        ) {
            NewFileItem(
                onOpenFileClicked = onOpenFileClicked,
                onContentFormatClicked = onContentFormatClicked,
            )

            Spacer(modifier = Modifier.height(height = 16.dp))
        }

        items(
            items = items,
            key = ContentModel::id,
            contentType = {
                ContentType.CONTENT_ITEM
            },
        ) {
            ContentItem(
                model = it,
                onItemClicked = onItemClicked,
            )
        }
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
            is NavigationState.NavigateToContentFormat -> onNavigateToBrowser(navigationState.url)
            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

private fun ContentVerificationException.mapToMessage(context: Context): String {
    return when (this) {
        is DuplicateIdQuestsException -> {
            context.getString(
                R.string.content_format_error_content_quest_duplicate_id,
                quests.map(Quest::id).joinToString(),
            )
        }

        is DuplicateIdThemesException -> {
            context.getString(
                R.string.content_format_error_content_theme_duplicate_id,
                themes.map(Theme::id).joinToString(),
            )
        }

        is NotValidQuestsException -> {
            context.getString(
                R.string.content_format_error_content_quest_not_valid,
                quests.map(Quest::id).joinToString(),
            )
        }

        is NotValidThemesException -> {
            context.getString(
                R.string.content_format_error_content_theme_not_valid,
                themes.map(Theme::id).joinToString(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ContentPreview(
    @PreviewParameter(ContentPreviewParameterProvider::class) items: List<ContentModel>,
) {
    QuizApplicationTheme {
        QuizBackground {
            ContentContent(
                items = items,
                onItemClicked = {},
                onOpenFileClicked = {},
                onContentFormatClicked = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun EmptyStatePreview() {
    QuizApplicationTheme {
        QuizBackground {
            EmptyStateContent(
                onOpenFileClicked = {},
                onContentFormatClicked = {},
            )
        }
    }
}
