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

package com.yugyd.quiz.data.database.user.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yugyd.quiz.data.model.SectionEntity

@Dao
interface SectionDao {

    @Query("SELECT * FROM section")
    suspend fun getAll(): List<SectionEntity>

    @Query("SELECT _id FROM section")
    suspend fun getAllIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<SectionEntity>)

    @Delete
    suspend fun deleteAll(sections: List<SectionEntity>): Int

    @Query("SELECT * FROM section WHERE _id IN (:questIds)")
    suspend fun loadAllByIds(questIds: IntArray): List<SectionEntity>

    @Query("SELECT COUNT(_id) FROM section")
    suspend fun getCount(): Int
}
