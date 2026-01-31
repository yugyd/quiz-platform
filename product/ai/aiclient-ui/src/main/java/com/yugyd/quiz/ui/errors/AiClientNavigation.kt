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

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yugyd.quiz.domain.api.payload.AiClientPayload
import com.yugyd.quiz.navigation.hideBottomBarArgument

private const val PROMPT_ARG = "prompt"
private const val LINK_ARG = "link"
private const val TITLE_ARG = "title"
private const val AI_CLIENT_ROUTE = "ai_client/"

internal class AiClientArgs(
    val aiClientPayload: AiClientPayload,
) {

    constructor(savedStateHandle: SavedStateHandle) : this(
        aiClientPayload = AiClientPayload(
            prompt = checkNotNull(
                savedStateHandle.get<String>(PROMPT_ARG)?.let(Uri::decode),
            ),
            fallbackWebLink = checkNotNull(
                savedStateHandle.get<String>(LINK_ARG)?.let(Uri::decode),
            ),
            title = checkNotNull(
                savedStateHandle.get<String>(TITLE_ARG)?.let(Uri::decode),
            ),
        ),
    )
}

fun NavController.navigateToAiClient(payload: AiClientPayload) {
    val encodedPrompt = Uri.encode(payload.prompt)
    val encodedLink = Uri.encode(payload.fallbackWebLink)
    val encodedTitle = Uri.encode(payload.title)
    val route = buildString {
        append(AI_CLIENT_ROUTE)
        append(encodedPrompt)
        append("&")
        append(encodedLink)
        append("&")
        append(encodedTitle)
    }
    navigate(route)
}

fun NavGraphBuilder.aiClientScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToBrowser: (String) -> Unit,
    onBack: () -> Unit,
) {
    val route = buildString {
        append(AI_CLIENT_ROUTE)
        append("{$PROMPT_ARG}")
        append("&")
        append("{$LINK_ARG}")
        append("&")
        append("{$TITLE_ARG}")
    }

    composable(
        route = route,
        arguments = listOf(
            navArgument(PROMPT_ARG) { type = NavType.StringType },
            navArgument(LINK_ARG) { type = NavType.StringType },
            navArgument(TITLE_ARG) { type = NavType.StringType },
            hideBottomBarArgument,
        ),
    ) {
        AiClientRoute(
            snackbarHostState = snackbarHostState,
            onNavigateToBrowser = onNavigateToBrowser,
            onBack = onBack,
        )
    }
}
