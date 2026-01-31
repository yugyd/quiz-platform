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

package com.yugyd.quiz.game.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.result
import com.yugyd.quiz.game.api.GameAndroidClient
import com.yugyd.quiz.game.api.GameClient
import com.yugyd.quiz.game.impl.exceptions.ActivityHostException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleGameClientImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: Logger,
) : GameClient, GameAndroidClient {

    private val activityMutex = Mutex()
    private var hostActivity: WeakReference<Activity> = WeakReference(null)

    override suspend fun setHostActivity(activity: Activity) {
        activityMutex.withLock {
            hostActivity = WeakReference(activity)
        }
    }

    override fun isAvailable(): Boolean {
        return result {
            val availability = GoogleApiAvailability.getInstance()
            val result = availability.isGooglePlayServicesAvailable(context)
            result == ConnectionResult.SUCCESS
        }
            .getOrElse {
                logger.logError(TAG, it)
                false
            }
    }

    override suspend fun isAuthenticated(): Boolean {
        if (!isAvailable()) return false

        return activityMutex.withLock {
            result {
                val gamesSignInClient = getSignInClient()
                val authenticationResult = gamesSignInClient.isAuthenticated().await()

                if (authenticationResult.isAuthenticated) {
                    logger.print(TAG, "User is authenticated")
                    true
                } else {
                    logger.print(TAG, "User is not authenticated")
                    false
                }
            }
                .getOrElse {
                    logger.logError(TAG, it)
                    false
                }
        }
    }

    override suspend fun signIn(): Boolean {
        if (!isAvailable()) return false

        return activityMutex.withLock {
            result {

                val gamesSignInClient = getSignInClient()
                val authenticationResult: AuthenticationResult = gamesSignInClient.signIn().await()

                if (authenticationResult.isAuthenticated) {
                    logger.print(TAG, "User is authenticated")
                    true
                } else {
                    // show error message and retry sign-in
                    logger.print(TAG, "User is not authenticated")
                    false
                }
            }
                .getOrElse {
                    logger.logError(TAG, it)
                    false
                }
        }
    }

    override suspend fun submitTotalScore(leaderboardId: String, score: Long) {
        if (!isAvailable()) return

        activityMutex.withLock {
            result {
                val leaderboardsClient = getLeaderboardsClient()
                leaderboardsClient.submitScoreImmediate(leaderboardId, score).await()
            }
                .getOrElse {
                    logger.logError(TAG, it)
                }
        }
    }

    override suspend fun getAndSubmitScore(leaderboardId: String, score: Long) {
        if (!isAvailable()) return

        activityMutex.withLock {
            result {
                val leaderboardsClient = getLeaderboardsClient()

                val userData = leaderboardsClient.loadCurrentPlayerLeaderboardScore(
                    leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC,
                )
                    .await()
                val userScore = userData?.get()?.rawScore ?: 0L
                val newScore = userScore + score
                leaderboardsClient.submitScoreImmediate(leaderboardId, newScore).await()
            }
                .getOrElse {
                    logger.logError(TAG, it)
                }
        }
    }

    override suspend fun getLeaderboardIntent(leaderboardId: String): Intent? {
        if (!isAvailable()) return null

        return activityMutex.withLock {
            result {
                val leaderboardsClient = getLeaderboardsClient()
                leaderboardsClient.getLeaderboardIntent(leaderboardId).await()
            }
                .getOrElse {
                    logger.logError(TAG, it)
                    null
                }
        }
    }

    private fun getSignInClient(): GamesSignInClient {
        val activity = hostActivity.get()
            ?: throw ActivityHostException("getSignInClient")
        return PlayGames.getGamesSignInClient(activity)
    }

    private fun getLeaderboardsClient(): LeaderboardsClient {
        val activity = hostActivity.get()
            ?: throw ActivityHostException("getLeaderboardsClient")
        return PlayGames.getLeaderboardsClient(activity)
    }

    companion object {
        private const val TAG = "GoogleGameClientImpl"
    }
}
