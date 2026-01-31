/*
 * Copyright 2024 Roman Likhachev
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

package com.yugyd.quiz.domain.aitasks

import com.yugyd.quiz.domain.game.api.model.Quest
import com.yugyd.quiz.domain.questai.model.AiTaskModel
import kotlinx.coroutines.flow.Flow

interface AiTasksInteractor {
    suspend fun getQuestIds(aiThemeId: Int): List<Int>
    suspend fun getQuest(aiThemeId: Int, aiQuestId: Int): Quest
    suspend fun getQuests(aiThemeId: Int): List<Quest>
    suspend fun fetchAiTasks(aiThemeId: Int)
    fun subscribeToAiTasks(aiThemeId: Int): Flow<List<AiTaskModel>>
    fun generateAiPromptForErrorTask(quest: String, trueAnswer: String): String
}
