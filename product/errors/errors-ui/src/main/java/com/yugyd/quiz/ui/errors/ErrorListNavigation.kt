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

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yugyd.quiz.domain.api.model.Mode
import com.yugyd.quiz.domain.api.payload.AiClientPayload
import com.yugyd.quiz.domain.api.payload.ErrorListPayload
import com.yugyd.quiz.navigation.IntListDecoder
import com.yugyd.quiz.navigation.hideBottomBarArgument

private const val MODE_ARG = "mode"
private const val THEME_ID_ARG = "themeId"
private const val ERROR_IDS_ARG = "errorQuestIds"
private const val ERROR_LIST_ROUTE = "error_list/"

internal class ErrorListArgs(
    val errorListPayload: ErrorListPayload,
) {

    constructor(savedStateHandle: SavedStateHandle) : this(
        errorListPayload = ErrorListPayload(
            mode = Mode.fromId(
                id = checkNotNull(savedStateHandle[MODE_ARG])
            ),
            themeId = savedStateHandle[THEME_ID_ARG],
            errorQuestIds = IntListDecoder.decode(
                value = checkNotNull(savedStateHandle[ERROR_IDS_ARG])
            ),
        )
    )
}

fun NavController.navigateToErrorList(payload: ErrorListPayload) {
    val idsArgument = IntListDecoder.encode(payload.errorQuestIds)
    val route = buildString {
        append(ERROR_LIST_ROUTE)
        append("${payload.mode.id}&")
        append("${payload.themeId}&")
        append("$idsArgument")
    }
    navigate(route)
}

fun NavGraphBuilder.errorListScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToAi: (AiClientPayload) -> Unit,
) {
    val route = buildString {
        append(ERROR_LIST_ROUTE)
        append("{$MODE_ARG}&")
        append("{$THEME_ID_ARG}&")
        append("{$ERROR_IDS_ARG}")
    }

    composable(
        route = route,
        arguments = listOf(
            navArgument(MODE_ARG) { type = NavType.IntType },
            navArgument(THEME_ID_ARG) {
                type = NavType.IntType
                defaultValue = 0
            },
            navArgument(ERROR_IDS_ARG) { type = NavType.StringType },
            hideBottomBarArgument,
        ),
    ) {
        ErrorListRoute(
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onNavigateToBrowser = onNavigateToBrowser,
            onNavigateToAi = onNavigateToAi,
        )
    }
}
