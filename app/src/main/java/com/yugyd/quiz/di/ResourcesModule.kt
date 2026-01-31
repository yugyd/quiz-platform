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

package com.yugyd.quiz.di

import com.yugyd.quiz.ContentProviderImpl
import com.yugyd.quiz.GameServicesIdProviderImpl
import com.yugyd.quiz.GoogleAdIdProviderImpl
import com.yugyd.quiz.ResIdProviderImpl
import com.yugyd.quiz.YandexAdProviderImpl
import com.yugyd.quiz.core.AdIdJvmProvider
import com.yugyd.quiz.core.AdIdProvider
import com.yugyd.quiz.core.AdProviderType
import com.yugyd.quiz.core.ContentProvider
import com.yugyd.quiz.core.GameServicesIdProvider
import com.yugyd.quiz.core.GlobalConfig
import com.yugyd.quiz.core.ResIdJvmProvider
import com.yugyd.quiz.core.ResIdProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourcesModule {

    @Binds
    internal abstract fun bindResIdProvider(impl: ResIdProviderImpl): ResIdProvider

    @Binds
    internal abstract fun bindResIdJvmProvider(impl: ResIdProvider): ResIdJvmProvider

    @Binds
    internal abstract fun bindContentProvider(impl: ContentProviderImpl): ContentProvider

    @Binds
    internal abstract fun bindGameServicesIdProvider(
        impl: GameServicesIdProviderImpl,
    ): GameServicesIdProvider

    companion object {

        @Provides
        internal fun bindAdIdProvider(
            yandexImpl: YandexAdProviderImpl,
            googleImpl: GoogleAdIdProviderImpl,
        ): AdIdProvider {
            return when (GlobalConfig.AD_PROVIDER) {
                AdProviderType.YANDEX -> yandexImpl
                AdProviderType.GOOGLE -> googleImpl
            }
        }

        @Provides
        internal fun bindAdIdJvmProvider(
            yandexImpl: YandexAdProviderImpl,
            googleImpl: GoogleAdIdProviderImpl,
        ): AdIdJvmProvider {
            return when (GlobalConfig.AD_PROVIDER) {
                AdProviderType.YANDEX -> yandexImpl
                AdProviderType.GOOGLE -> googleImpl
            }
        }
    }
}
