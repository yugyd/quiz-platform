/*
 *    Copyright 2024 Roman Likhachev
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

package com.yugyd.quiz.ui.simplequest

import com.yugyd.quiz.ui.game.api.model.BaseQuestUiModel
import com.yugyd.quiz.ui.game.api.model.HighlightUiModel
import com.yugyd.quiz.ui.game.api.model.QuestUiType
import com.yugyd.quiz.ui.game.api.model.QuestValueUiModel

data class LatexSimpleQuestUiModel(
    override val questModel: QuestValueUiModel,
    val answers: List<String>,
    val answerButtonIsEnabled: Boolean,
    val selectedAnswer: String?,
    val highlight: HighlightUiModel,
    val isLatexQuest: Boolean,
    val isLatexAnswer: Boolean,
) : BaseQuestUiModel(
    questModel = questModel,
    type = QuestUiType.SIMPLE_LATEX,
)
