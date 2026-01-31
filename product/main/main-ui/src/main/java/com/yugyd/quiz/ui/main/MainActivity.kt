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

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.yugyd.quiz.ad.banner.AdViewFactory
import com.yugyd.quiz.ad.banner.LocalAdViewFactory
import com.yugyd.quiz.ad.interstitial.InterstitialAdFactory
import com.yugyd.quiz.ad.interstitial.LocalInterstitialAdFactory
import com.yugyd.quiz.ad.rewarded.LocalRewardAdFactory
import com.yugyd.quiz.ad.rewarded.RewardedAdFactory
import com.yugyd.quiz.commonui.providers.LocalResIdProvider
import com.yugyd.quiz.core.ResIdProvider
import com.yugyd.quiz.featuretoggle.domain.FeatureManager
import com.yugyd.quiz.featuretoggle.domain.model.FeatureToggle
import com.yugyd.quiz.game.api.GameAndroidClient
import com.yugyd.quiz.game.api.GameClient
import com.yugyd.quiz.game.api.LeaderboardType
import com.yugyd.quiz.ui.main.MainView.Action
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class MainActivity : FragmentActivity() {

    @Inject
    lateinit var adViewFactory: AdViewFactory

    @Inject
    lateinit var rewardedAdFactory: RewardedAdFactory

    @Inject
    lateinit var interstitialAdFactory: InterstitialAdFactory

    @Inject
    lateinit var resIdProvider: ResIdProvider

    @Inject
    lateinit var gameAndroidClient: GameAndroidClient

    @Inject
    lateinit var gameClient: GameClient

    @Inject
    lateinit var featureManager: FeatureManager

    private val viewModel: MainViewModel by viewModels()

    private val leaderboardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            gameAndroidClient.setHostActivity(this@MainActivity)
        }

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                )

                onDispose {}
            }

            CompositionLocalProvider(
                LocalAdViewFactory provides adViewFactory,
                LocalRewardAdFactory provides rewardedAdFactory,
                LocalInterstitialAdFactory provides interstitialAdFactory,
                LocalResIdProvider provides resIdProvider,
            ) {
                QuizApplicationTheme(darkTheme = isSystemInDarkTheme()) {
                    MainApp(
                        viewModel = viewModel,
                        onNavigateToRating = ::navigateToLeaderboard,
                    )
                }
            }
        }

        MobileAds.initialize(this)

        if (savedInstanceState == null) {
            processPush(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        processPush(intent)
        super.onNewIntent(intent)
    }

    private fun processPush(intent: Intent?) {
        val extras = intent?.extras
            ?.toMap().orEmpty()
            .filterValues { it is String }
            .mapValues { it.value as String }
        viewModel.onAction(Action.ProcessPushData(extras))
    }

    private fun Bundle.toMap() = keySet().associateWith(::getString)

    private fun navigateToLeaderboard(leaderboardType: LeaderboardType) {
        lifecycleScope.launch {
            val res = when (leaderboardType) {
                LeaderboardType.EXPERIENCE -> resIdProvider.gameExperienceLeaderboard()
                LeaderboardType.WALKTHROUGH -> resIdProvider.gameWalkthroughLeaderboard()
            }
            val leaderboardId = getString(res)

            if (featureManager.isFeatureEnabled(FeatureToggle.GAME_SERVICES)) {
                val intent = gameClient.getLeaderboardIntent(leaderboardId = leaderboardId)

                if (intent != null) {
                    leaderboardLauncher.launch(intent)
                }
            }
        }
    }
}
