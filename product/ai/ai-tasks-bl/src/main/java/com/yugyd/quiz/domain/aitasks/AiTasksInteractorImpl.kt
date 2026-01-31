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

import com.yugyd.quiz.core.GlobalConfig
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.domain.aitasks.data.AiTasksModel
import com.yugyd.quiz.domain.game.api.model.Quest
import com.yugyd.quiz.domain.game.api.model.QuestType
import com.yugyd.quiz.domain.questai.AiQuestInteractor
import com.yugyd.quiz.domain.questai.model.AiTaskModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AiTasksInteractorImpl @Inject constructor(
    private val aiQuestInteractor: AiQuestInteractor,
    private val aiTasksInMemorySource: AiTasksInMemorySource,
    private val dispatcherProvider: DispatchersProvider,
) : AiTasksInteractor {

    override suspend fun getQuestIds(aiThemeId: Int) = withContext(dispatcherProvider.io) {
        getAiTasks(aiThemeId = aiThemeId)
            .firstOrNull()
            ?.map(AiTaskModel::id)
            .orEmpty()
    }

    override suspend fun getQuests(aiThemeId: Int): List<Quest> =
        withContext(dispatcherProvider.io) {
            getAiTasks(aiThemeId = aiThemeId)
                .firstOrNull()
                .orEmpty()
                .map { it.toQuest() }
        }

    override suspend fun getQuest(aiThemeId: Int, aiQuestId: Int) =
        withContext(dispatcherProvider.io) {
            val aiTasks =
                getAiTasks(aiThemeId = aiThemeId).firstOrNull() ?: error("Ai tasks is empty")
            val aiTask = aiTasks.first {
                it.id == aiQuestId
            }
            aiTask.toQuest()
        }

    override suspend fun fetchAiTasks(aiThemeId: Int) = withContext(dispatcherProvider.io) {
        val aiTasks = aiQuestInteractor.getTasks(themeId = aiThemeId)
        val aiTasksModel = AiTasksModel(
            aiThemeId = aiThemeId,
            aiTasks = aiTasks,
        )
        aiTasksInMemorySource.updateCachedAiTasks(aiTasks = aiTasksModel)
    }

    override fun subscribeToAiTasks(aiThemeId: Int): Flow<List<AiTaskModel>> {
        return getAiTasks(aiThemeId = aiThemeId)
    }

    override fun generateAiPromptForErrorTask(quest: String, trueAnswer: String): String {
        return "Explain why the answer '$trueAnswer' is correct for the question: $quest." +
                "Translate the explanation into ${GlobalConfig.LOCALE_CODE}." +
                "There should always be a question and a complete correct answer at the beginning." +
                "Use markdown formatting."
    }

    private fun getAiTasks(aiThemeId: Int) = aiTasksInMemorySource
        .subscribeToCachedAiTasks()
        .map {
            if (it.aiThemeId == aiThemeId) {
                it.aiTasks
            } else {
                emptyList()
            }
        }

    private fun AiTaskModel.toQuest(): Quest = Quest(
        id = id,
        quest = quest,
        trueAnswer = trueAnswer,
        image = image,
        answer2 = answer2.orEmpty(),
        answer3 = answer3.orEmpty(),
        answer4 = answer4.orEmpty(),
        answer5 = answer5.orEmpty(),
        answer6 = answer6.orEmpty(),
        answer7 = answer7.orEmpty(),
        answer8 = answer8.orEmpty(),
        complexity = complexity,
        category = category,
        section = section,
        type = QuestType.SIMPLE,
    )
}

