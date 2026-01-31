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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.yugyd.quiz.game.api.LeaderboardType
import com.yugyd.quiz.navigation.topLevelNavOptions

private const val PROFILE_ROUTE = "profile"

fun NavController.navigateToProfile() {
    navigate(PROFILE_ROUTE, topLevelNavOptions())
}

fun NavGraphBuilder.profileScreen(
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
    composable(route = PROFILE_ROUTE) {
        ProfileRoute(
            onNavigateToTelegram = onNavigateToTelegram,
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
        )
    }
}
