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

import com.yugyd.quiz.domain.api.model.tasks.TaskModel
import com.yugyd.quiz.domain.api.payload.ErrorListPayload

internal interface ErrorListView {

    data class State(
        val payload: ErrorListPayload = ErrorListPayload(),
        val items: List<TaskModel> = emptyList(),
        val isWarning: Boolean = false,
        val isLoading: Boolean = false,
        val showErrorMessage: Boolean = false,
        val navigationState: NavigationState? = null,
    ) {

        sealed interface NavigationState {
            object Back : NavigationState
            data class NavigateToExternalBrowser(val url: String) : NavigationState
            data class NavigateToAiScreen(
                val prompt: String,
                val fallbackWebLink: String,
                val title: String,
            ) : NavigationState
        }
    }

    sealed interface Action {
        object OnBackClicked : Action
        class OnErrorClicked(val item: TaskModel) : Action
        class OnFavoriteClicked(val item: TaskModel) : Action
        object OnSnackbarDismissed : Action
        object OnNavigationHandled : Action
    }
}
