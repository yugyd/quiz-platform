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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.Action
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.NavigationState
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.SnackbarMessage
import com.yugyd.quiz.ui.aiconnectiondetails.AiConnectionDetailsView.State.ToolbarTitle
import com.yugyd.quiz.uikit.LoadingContent
import com.yugyd.quiz.uikit.WarningContent
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.component.SimpleToolbar
import com.yugyd.quiz.uikit.component.TwoLineWithActionsElevatedCard
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.R as UiKitR

@Composable
internal fun AiConnectionDetailsRoute(
    viewModel: AiConnectionDetailsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigateToBrowser: (String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AiConnectionDetailsScreen(
        uiState = state,
        snackbarHostState = snackbarHostState,
        onKeyInstructionClicked = {
            viewModel.onAction(Action.OnKeyInstructionClicked)
        },
        onSaveClicked = {
            viewModel.onAction(Action.OnSaveClicked)
        },
        onDeleteClicked = {
            viewModel.onAction(Action.OnDeleteClicked)
        },
        onNameChanged = {
            viewModel.onAction(Action.OnNameChanged(it))
        },
        onProviderSelected = {
            viewModel.onAction(Action.OnProviderSelected(it))
        },
        onApiKeyChanged = {
            viewModel.onAction(Action.OnApiKeyChanged(it))
        },
        onCloudProjectFolderChanged = {
            viewModel.onAction(Action.OnCloudProjectFolderChanged(it))
        },
        onApiModelChanged = {
            viewModel.onAction(Action.OnApiModelChanged(it))
        },
        onBackPressed = {
            viewModel.onAction(Action.OnBackPressed)
        },
        onErrorDismissState = {
            viewModel.onAction(Action.OnSnackbarDismissed)
        },
        onBack = onBack,
        onNavigateToBrowser = onNavigateToBrowser,
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
    )
}

@Composable
internal fun AiConnectionDetailsScreen(
    uiState: State,
    snackbarHostState: SnackbarHostState,
    onKeyInstructionClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onNameChanged: (String) -> Unit,
    onProviderSelected: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onCloudProjectFolderChanged: (String) -> Unit,
    onApiModelChanged: (String) -> Unit,
    onErrorDismissState: () -> Unit,
    onBackPressed: () -> Unit,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigationHandled: () -> Unit,
) {
    val errorMessage = stringResource(id = UiKitR.string.ds_error_base)
    val errorFieldsMessage = stringResource(id = UiKitR.string.ds_error_fields)
    LaunchedEffect(key1 = uiState.showErrorMessage) {
        when (uiState.showErrorMessage) {
            SnackbarMessage.ERROR -> {
                snackbarHostState.showSnackbar(message = errorMessage)
            }

            SnackbarMessage.FILL_FIELDS -> {
                snackbarHostState.showSnackbar(message = errorFieldsMessage)
            }

            null -> Unit
        }

        uiState.showErrorMessage?.let { onErrorDismissState() }
    }

    Column {
        val toolbar = remember(uiState.toolbarTitle) {
            when (uiState.toolbarTitle) {
                ToolbarTitle.ADD -> R.string.ai_connection_details_title_add
                ToolbarTitle.EDIT -> R.string.ai_connection_details_title_edit
                null -> null
            }
        }

        SimpleToolbar(
            title = toolbar?.let { stringResource(it) } ?: "",
            onBackPressed = onBackPressed,
            rightIcon = if (uiState.isDeleteVisible) {
                Icons.Default.Delete
            } else {
                null
            },
            onRightIconClicked = onDeleteClicked,
        )

        when {
            uiState.isLoading -> {
                LoadingContent()
            }

            uiState.isWarning -> {
                WarningContent()
            }

            else -> {
                AiConnectionDetailsContent(
                    name = uiState.name,
                    provider = uiState.provider,
                    allProviders = uiState.allProviders,
                    isProviderEnabled = uiState.isProviderEnabled,
                    apiKey = uiState.apiKey,
                    isApiKeyValid = uiState.isApiKeyValid,
                    cloudProjectFolder = uiState.cloudProjectFolder,
                    isCloudProjectFolderVisible = uiState.isCloudProjectFolderVisible,
                    apiModel = uiState.apiModel,
                    isSaveButtonEnabled = uiState.isSaveButtonEnabled,
                    onKeyInstructionClicked = onKeyInstructionClicked,
                    onSaveClicked = onSaveClicked,
                    onNameChanged = onNameChanged,
                    onProviderSelected = onProviderSelected,
                    onApiKeyChanged = onApiKeyChanged,
                    onCloudProjectFolderChanged = onCloudProjectFolderChanged,
                    onApiModelChanged = onApiModelChanged,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiConnectionDetailsContent(
    name: String,
    provider: String,
    allProviders: List<String>,
    isProviderEnabled: Boolean,
    apiKey: String,
    isApiKeyValid: Boolean?,
    cloudProjectFolder: String,
    isCloudProjectFolderVisible: Boolean,
    apiModel: String,
    isSaveButtonEnabled: Boolean,
    onKeyInstructionClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onNameChanged: (String) -> Unit,
    onProviderSelected: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onCloudProjectFolderChanged: (String) -> Unit,
    onApiModelChanged: (String) -> Unit,
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
        TwoLineWithActionsElevatedCard(
            title = stringResource(id = R.string.ai_connection_details_title),
            subtitle = stringResource(id = R.string.ai_connection_details_description),
            confirm = stringResource(id = R.string.ai_connection_details_open),
            onConfirmClicked = onKeyInstructionClicked,
        )

        Spacer(
            modifier = Modifier.height(height = 16.dp),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChanged,
            label = {
                Text(
                    text = stringResource(id = R.string.ai_connection_details_name),
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        onNameChanged("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null,
                    )
                }
            },
        )

        Spacer(
            modifier = Modifier.height(height = 16.dp),
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = provider,
                onValueChange = {},
                label = {
                    Text(
                        text = stringResource(id = R.string.ai_connection_details_provider),
                    )
                },
                readOnly = true,
                singleLine = true,
                enabled = isProviderEnabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                allProviders.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            onProviderSelected(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(height = 16.dp),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKey,
            onValueChange = onApiKeyChanged,
            label = {
                Text(
                    text = stringResource(id = R.string.ai_connection_details_api_key),
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (isApiKeyValid != true) {
                            onApiKeyChanged("")
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isApiKeyValid == true) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.Clear
                        },
                        contentDescription = null,
                    )
                }
            },
        )

        if (isCloudProjectFolderVisible) {
            Spacer(
                modifier = Modifier.height(height = 16.dp),
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = cloudProjectFolder,
                onValueChange = onCloudProjectFolderChanged,
                label = {
                    Text(
                        text = stringResource(id = R.string.ai_connection_details_project_folder),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (isApiKeyValid != true) {
                                onCloudProjectFolderChanged("")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isApiKeyValid == true) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.Clear
                            },
                            contentDescription = null,
                        )
                    }
                },
            )
        }

        Spacer(
            modifier = Modifier.height(height = 16.dp),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiModel,
            onValueChange = onApiModelChanged,
            label = {
                Text(
                    text = stringResource(id = R.string.ai_connection_details_api_model),
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        onApiModelChanged("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null,
                    )
                }
            },
        )

        Spacer(
            modifier = Modifier.height(height = 16.dp),
        )

        ContinueButton(
            isSaveButtonEnabled = isSaveButtonEnabled,
            onSaveClicked = onSaveClicked,
        )
    }
}

@Composable
private fun ColumnScope.ContinueButton(
    isSaveButtonEnabled: Boolean,
    onSaveClicked: () -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(end = 16.dp)
            .align(Alignment.End),
        onClick = onSaveClicked,
        enabled = isSaveButtonEnabled,
    ) {
        Text(
            text = stringResource(id = R.string.ai_connection_details_action_save),
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
            NavigationState.Back -> {
                onBack()
            }

            is NavigationState.NavigateToExternalBrowser -> {
                onNavigateToBrowser(navigationState.link)
            }

            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

@ThemePreviews
@Composable
private fun ContentPreview() {
    QuizApplicationTheme {
        QuizBackground {
            AiConnectionDetailsContent(
                name = "Name",
                provider = "Provider",
                allProviders = listOf("Provider"),
                isProviderEnabled = true,
                apiKey = "ApiKey",
                isApiKeyValid = true,
                cloudProjectFolder = "CloudProjectFolder",
                isCloudProjectFolderVisible = true,
                apiModel = "ApiModel",
                isSaveButtonEnabled = true,
                onKeyInstructionClicked = {},
                onSaveClicked = {},
                onNameChanged = {},
                onProviderSelected = {},
                onApiKeyChanged = {},
                onCloudProjectFolderChanged = {},
                onApiModelChanged = {},
            )
        }
    }
}
