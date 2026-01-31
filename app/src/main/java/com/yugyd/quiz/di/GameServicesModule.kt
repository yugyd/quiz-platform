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

package com.yugyd.quiz.di

import com.yugyd.quiz.game.api.GameAndroidClient
import com.yugyd.quiz.game.api.GameClient
import com.yugyd.quiz.game.api.GameServicesInitializer
import com.yugyd.quiz.game.impl.GameServicesInitializerImpl
import com.yugyd.quiz.game.impl.GoogleGameClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GameServicesModule {

    @Binds
    internal abstract fun bindGameServicesInitializer(
        impl: GameServicesInitializerImpl,
    ): GameServicesInitializer

    @Binds
    internal abstract fun bindGameClient(impl: GoogleGameClientImpl): GameClient

    @Binds
    internal abstract fun bindGameAndroidClient(impl: GoogleGameClientImpl): GameAndroidClient
}
