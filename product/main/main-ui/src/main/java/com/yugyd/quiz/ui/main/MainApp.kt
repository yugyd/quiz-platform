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

package com.yugyd.quiz.ui.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yugyd.quiz.correctui.navigateToCorrect
import com.yugyd.quiz.game.api.LeaderboardType
import com.yugyd.quiz.navigation.SHOW_BOTTOM_BAR_ARG
import com.yugyd.quiz.navigation.getTelegramIntent
import com.yugyd.quiz.newversiononboarding.OnboardingBottomSheet
import com.yugyd.quiz.progressui.progress.navigateToProgress
import com.yugyd.quiz.ui.content.navigateToContent
import com.yugyd.quiz.ui.courses.navigateToCourseList
import com.yugyd.quiz.ui.main.MainView.Action
import com.yugyd.quiz.ui.main.MainView.State
import com.yugyd.quiz.ui.main.MainView.State.NavigationState
import com.yugyd.quiz.ui.profile.navigateToProfile
import com.yugyd.quiz.ui.theme.navigateToTheme
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.update.navigateToUpdate
import timber.log.Timber

@Composable
internal fun MainApp(
    viewModel: MainViewModel,
    onNavigateToRating: (LeaderboardType) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainScreen(
        uiState = state,
        onTelegramHandled = {
            viewModel.onAction(Action.OnTelegramHandled)
        },
        onNavigationHandled = {
            viewModel.onAction(Action.OnNavigationHandled)
        },
        onOnboardingClicked = {
            viewModel.onAction(Action.OnOnboardingClicked)
        },
        onOnboardingDismissState = {
            viewModel.onAction(Action.OnboardingBottomSheetDismissed)
        },
        onNavigateToRating = onNavigateToRating,
    )
}

@Composable
internal fun MainScreen(
    uiState: State,
    onTelegramHandled: () -> Unit,
    onNavigationHandled: () -> Unit,
    onOnboardingClicked: () -> Unit,
    onOnboardingDismissState: () -> Unit,
    onNavigateToRating: (LeaderboardType) -> Unit,
) {
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }
    val isBottomBarVisible = rememberSaveable { mutableStateOf(true) }
    val currentTopDestination = rememberSaveable { mutableStateOf(TopDestination.THEME) }

    PostNotificationPermission(requestPushPermission = uiState.requestPushPermission)

    val context = LocalContext.current
    LaunchedEffect(key1 = uiState.showTelegram) {
        if (uiState.showTelegram) {
            startActivityOrLogError(context, context.getTelegramIntent(uiState.telegramLink))

            onTelegramHandled()
        }
    }

    OnboardingBottomSheet(
        openBottomSheet = uiState.showOnboarding,
        payload = uiState.onboardingPayload,
        onButtonClicked = onOnboardingClicked,
        onDismissRequest = onOnboardingDismissState,
    )

    DisposableEffect(Unit) {
        val callback = NavController.OnDestinationChangedListener { _, _, arguments ->
            isBottomBarVisible.value = arguments == null ||
                    arguments.getBoolean(SHOW_BOTTOM_BAR_ARG, true)
        }

        navController.addOnDestinationChangedListener(callback)

        onDispose {
            navController.removeOnDestinationChangedListener(callback)
        }
    }

    QuizBackground {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            bottomBar = {
                if (isBottomBarVisible.value) {
                    NavigationBottomBar(
                        destinations = uiState.topDestinations,
                        currentTopDestination = currentTopDestination.value,
                        onNavigateToDestination = {
                            currentTopDestination.value = it
                            navController.navigateToTopLevelDestination(it)
                        }
                    )
                }
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                QuizNavHost(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    navigateToExternalScreen = {
                        startActivityOrLogError(context, it)
                    },
                    onNavigateToRating = onNavigateToRating,
                )
            }
        }
    }

    NavigationHandler(
        navigationState = uiState.navigationState,
        onNavigateToUpdate = {
            navController.navigateToUpdate()
        },
        onNavigateToContent = {
            navController.navigateToContent(isBackEnabled = false)
        },
        onBack = {
            navController.popBackStack()
        },
        onNavigationHandled = onNavigationHandled,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun PostNotificationPermission(requestPushPermission: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val postNotificationPermissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS,
        )

        LaunchedEffect(
            key1 = requestPushPermission,
            key2 = postNotificationPermissionState.status.isGranted
        ) {
            if (requestPushPermission && !postNotificationPermissionState.status.isGranted) {
                postNotificationPermissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
internal fun NavigationBottomBar(
    destinations: List<TopDestination>,
    currentTopDestination: TopDestination,
    onNavigateToDestination: (TopDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        destinations.forEachIndexed { _, topDestination ->
            val title = stringResource(id = topDestination.titleResId)
            NavigationBarItem(
                selected = currentTopDestination == topDestination,
                onClick = {
                    onNavigateToDestination(topDestination)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = topDestination.iconResId),
                        contentDescription = title,
                    )
                },
                label = {
                    Text(text = title)
                },
            )
        }
    }
}

@Composable
internal fun NavigationHandler(
    navigationState: NavigationState?,
    onNavigateToUpdate: () -> Unit,
    onNavigateToContent: () -> Unit,
    onBack: () -> Unit,
    onNavigationHandled: () -> Unit,
) {
    LaunchedEffect(key1 = navigationState) {
        when (navigationState) {
            NavigationState.Back -> onBack()
            NavigationState.NavigateToUpdate -> onNavigateToUpdate()
            NavigationState.NavigateToContent -> onNavigateToContent()
            null -> Unit
        }

        navigationState?.let { onNavigationHandled() }
    }
}

fun NavController.navigateToTopLevelDestination(topDestination: TopDestination) {
    when (topDestination) {
        TopDestination.THEME -> navigateToTheme()
        TopDestination.CORRECT -> navigateToCorrect()
        TopDestination.PROGRESS -> navigateToProgress()
        TopDestination.PROFILE -> navigateToProfile()
        TopDestination.COURSES -> navigateToCourseList()
    }
}

private fun startActivityOrLogError(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        Timber.e(error)
    }
}

@ThemePreviews
@Composable
private fun NavigationBarPreview() {
    QuizApplicationTheme {
        QuizBackground {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                NavigationBottomBar(
                    modifier = Modifier.fillMaxWidth(),
                    destinations = TopDestination.values().toList(),
                    currentTopDestination = TopDestination.CORRECT,
                    onNavigateToDestination = {},
                )
            }
        }
    }
}
