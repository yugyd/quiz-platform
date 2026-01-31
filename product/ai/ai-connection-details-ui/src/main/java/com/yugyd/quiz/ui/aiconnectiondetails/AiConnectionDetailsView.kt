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

import androidx.compose.runtime.Immutable
import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.domain.aiconnection.model.AiInstructionConfig
import com.yugyd.quiz.domain.api.payload.AiConnectionDetailsPayload

internal interface AiConnectionDetailsView {

    data class State(
        val domainState: AiConnectionDetailsDomainState = AiConnectionDetailsDomainState(),
        val toolbarTitle: ToolbarTitle? = null,
        val keyInstructionLink: String? = null,
        val name: String = "",
        val provider: String = "",
        val allProviders: List<String> = emptyList(),
        val isProviderEnabled: Boolean = true,
        val apiKey: String = "",
        val isApiKeyValid: Boolean? = null,
        val cloudProjectFolder: String = "",
        val isCloudProjectFolderVisible: Boolean = false,
        val apiModel: String = "",
        val isSaveButtonEnabled: Boolean = true,
        val isDeleteVisible: Boolean = false,
        val isWarning: Boolean = false,
        val isLoading: Boolean = false,
        val showErrorMessage: SnackbarMessage? = null,
        val navigationState: NavigationState? = null,
    ) {

        enum class SnackbarMessage {
            ERROR, FILL_FIELDS,
        }

        enum class ToolbarTitle {
            ADD, EDIT,
        }

        @Immutable
        data class AiConnectionDetailsDomainState(
            val payload: AiConnectionDetailsPayload = AiConnectionDetailsPayload(),
            val aiConnectionModel: AiConnectionModel? = null,
            val apiKeyAiInstructionConfigs: List<AiInstructionConfig>? = null,
        )

        sealed interface NavigationState {
            data object Back : NavigationState
            data class NavigateToExternalBrowser(val link: String) : NavigationState
        }
    }

    sealed interface Action {
        data object OnKeyInstructionClicked : Action
        data object OnSaveClicked : Action
        data object OnDeleteClicked : Action
        data class OnNameChanged(val name: String) : Action
        data class OnProviderSelected(val provider: String) : Action
        data class OnApiKeyChanged(val apiKey: String) : Action
        data class OnApiModelChanged(val apiModel: String) : Action
        data class OnCloudProjectFolderChanged(val cloudProjectFolder: String) : Action
        data object OnSnackbarDismissed : Action
        data object OnNavigationHandled : Action
        data object OnBackPressed : Action
    }
}
