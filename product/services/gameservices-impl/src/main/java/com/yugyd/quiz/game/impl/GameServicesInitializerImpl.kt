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

import android.content.Context
import com.google.android.gms.games.PlayGamesSdk
import com.yugyd.quiz.core.Logger
import com.yugyd.quiz.core.result
import com.yugyd.quiz.game.api.GameClient
import com.yugyd.quiz.game.api.GameServicesInitializer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GameServicesInitializerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: Logger,
    private val gameClient: GameClient,
) : GameServicesInitializer {

    override fun initialize() {
        if (!gameClient.isAvailable()) return

        result {
            PlayGamesSdk.initialize(context)
            logger.print(TAG, "Initialize game services is completed")
        }
            .getOrElse {
                logger.logError(TAG, it)
            }
    }

    companion object {
        private const val TAG = "GameInitializerImpl"
    }
}
