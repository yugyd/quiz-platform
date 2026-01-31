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

package com.yugyd.quiz.ai.connection.api.internal.data.mapper

import com.yugyd.quiz.ai.connection.api.internal.data.model.AiConnectionEntity
import com.yugyd.quiz.ai.connection.api.model.AiConnectionModel
import com.yugyd.quiz.ai.connection.api.model.AiConnectionProviderTypeModel
import javax.inject.Inject

class AiConnectionMapper @Inject internal constructor() {

    fun map(entity: AiConnectionEntity): AiConnectionModel {
        return AiConnectionModel(
            id = entity.id,
            name = entity.name,
            apiKey = entity.apiKey,
            apiCloudFolder = entity.apiCloudFolder,
            apiProvider = AiConnectionProviderTypeModel.fromQualifier(entity.apiProvider),
            isValid = entity.isValid,
            isActive = entity.isActive,
            apiModel = entity.apiModel,
        )
    }

    fun map(model: AiConnectionModel): AiConnectionEntity {
        return AiConnectionEntity(
            id = model.id,
            name = model.name,
            apiKey = model.apiKey,
            apiCloudFolder = model.apiCloudFolder,
            apiProvider = model.apiProvider.qualifier,
            isValid = model.isValid,
            isActive = model.isActive,
            apiModel = model.apiModel,
        )
    }
}
