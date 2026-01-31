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

data class SectionRange(
    val startSectionId: Int,
    val maxSectionId: Int,
)

interface SectionSource {
    suspend fun getAll(): List<Int>
    suspend fun getSectionCountByTheme(themeId: Int): SectionRange
    suspend fun updateSectionIds(questIds: List<Int>)
    suspend fun deleteSectionIds(questIds: List<Int>): Int
    suspend fun getSectionTotalProgress(questIds: IntArray): Int
    suspend fun resetSections(): Int
}
