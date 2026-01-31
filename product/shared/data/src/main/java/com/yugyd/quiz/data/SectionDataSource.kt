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

package com.yugyd.quiz.data

import com.yugyd.quiz.data.database.content.dao.QuestDao
import com.yugyd.quiz.data.database.user.dao.SectionDao
import com.yugyd.quiz.data.database.user.dao.UserResetDao
import com.yugyd.quiz.data.model.mappers.SectionEntityMapper
import com.yugyd.quiz.domain.api.repository.SectionRange
import com.yugyd.quiz.domain.api.repository.SectionSource
import javax.inject.Inject

internal class SectionDataSource @Inject constructor(
    private val questDao: QuestDao,
    private val sectionDao: SectionDao,
    private val resetDao: UserResetDao,
    private val sectionEntityMapper: SectionEntityMapper
) : SectionSource {

    override suspend fun getAll(): List<Int> = sectionDao.getAllIds()

    override suspend fun getSectionCountByTheme(themeId: Int): SectionRange {
        return SectionRange(
            startSectionId = questDao.getMinSectionByTheme(themeId),
            maxSectionId = questDao.getSectionCountByTheme(themeId),
        )
    }

    override suspend fun updateSectionIds(questIds: List<Int>) = sectionDao
        .insertAll(sectionEntityMapper.mapSectionToEntity(questIds))

    override suspend fun deleteSectionIds(questIds: List<Int>) = sectionDao
        .deleteAll(sectionEntityMapper.mapSectionToEntity(questIds))

    override suspend fun getSectionTotalProgress(questIds: IntArray) = sectionDao
        .loadAllByIds(questIds)
        .size

    override suspend fun resetSections() = resetDao.resetSection()
}