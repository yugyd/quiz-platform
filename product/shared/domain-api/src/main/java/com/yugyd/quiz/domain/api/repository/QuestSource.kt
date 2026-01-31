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

package com.yugyd.quiz.domain.api.repository

import com.yugyd.quiz.domain.game.api.model.Quest

interface QuestSource {

    suspend fun getQuest(id: Int): Quest

    suspend fun getQuests(): List<Quest>

    suspend fun getQuestIds(): List<Int>

    suspend fun getQuestIds(themeId: Int, isSort: Boolean = false): List<Int>

    suspend fun getQuestIdsBySection(
        themeId: Int,
        sectionId: Int,
        isSort: Boolean = false
    ): List<Int>

    suspend fun getQuestIdsByErrors(errorIds: IntArray): List<Quest>

    suspend fun addQuests(quests: List<Quest>)
}
