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

package com.yugyd.quiz.domain.aidirectconnection

import com.yugyd.quiz.domain.aidirectconnection.data.AiClientRemoteDataSource
import com.yugyd.quiz.domain.aidirectconnection.data.mapper.AiClientMapper
import com.yugyd.quiz.domain.aidirectconnection.data.mapper.AiClientMapperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module(
    includes = [
        AiClientDataModule::class,
    ]
)
@InstallIn(SingletonComponent::class)
abstract class AiQuestBlModule {

    @Binds
    internal abstract fun bindAiClientRemoteSource(impl: AiClientRemoteDataSource): AiClientRemoteSource

    @Binds
    internal abstract fun bindAiClientMapper(impl: AiClientMapperImpl): AiClientMapper

    @Binds
    internal abstract fun bindAiClientInteractor(impl: AiClientInteractorImpl): AiClientInteractor
}

