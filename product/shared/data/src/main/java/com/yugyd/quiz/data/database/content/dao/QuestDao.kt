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

package com.yugyd.quiz.data.database.content.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yugyd.quiz.data.model.quest.QuestComplexitySubsetEntity
import com.yugyd.quiz.data.model.quest.QuestEntity

@Dao
interface QuestDao {

    @Query("SELECT * FROM quest")
    suspend fun getAll(): List<QuestEntity>

    @Query("SELECT _id FROM quest")
    suspend fun getIds(): List<Int>

    @Query("SELECT _id FROM quest WHERE category = :themeId")
    suspend fun getIdsByTheme(themeId: Int): List<Int>

    @Query("SELECT _id, complexity FROM quest WHERE category = :themeId")
    suspend fun getComplexityQuestByTheme(themeId: Int): List<QuestComplexitySubsetEntity>

    @Query("SELECT _id FROM quest WHERE category = :themeId AND section = :sectionId")
    suspend fun getIdsBySection(themeId: Int, sectionId: Int): List<Int>

    @Query("SELECT _id, complexity FROM quest WHERE category = :themeId AND section = :sectionId")
    suspend fun getComplexityQuestBySection(
        themeId: Int,
        sectionId: Int
    ): List<QuestComplexitySubsetEntity>

    @Query("SELECT * FROM quest WHERE _id = :id")
    suspend fun getQuestById(id: Int): QuestEntity

    @Query("SELECT * FROM quest WHERE _id IN (:questIds)")
    suspend fun loadAllByIds(questIds: IntArray): List<QuestEntity>

    @Query("SELECT MAX(section) FROM quest WHERE category = :themeId")
    suspend fun getSectionCountByTheme(themeId: Int): Int

    @Query("SELECT MIN(section) FROM quest WHERE category = :themeId")
    suspend fun getMinSectionByTheme(themeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quests: List<QuestEntity>)
}
