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

package com.yugyd.quiz.ui.simplequest

import com.yugyd.quiz.domain.simplequest.LatexSimpleQuestModel
import com.yugyd.quiz.ui.game.api.mapper.BaseQuestUiMapper
import com.yugyd.quiz.ui.game.api.mapper.UiMapperArgs
import com.yugyd.quiz.ui.game.api.model.HighlightUiModel
import com.yugyd.quiz.ui.game.api.model.QuestValueUiModel
import javax.inject.Inject

class LatexSimpleQuestUiMapper @Inject constructor() :
    BaseQuestUiMapper<LatexSimpleQuestModel, LatexSimpleQuestUiModel, LatexSimpleQuestUiMapper.SimpleArgs> {

    override fun map(model: LatexSimpleQuestModel, args: SimpleArgs): LatexSimpleQuestUiModel {
        return LatexSimpleQuestUiModel(
            questModel = QuestValueUiModel(
                questText = model.quest,
                imageUri = model.image,
            ),
            answers = model.answers,
            selectedAnswer = null,
            answerButtonIsEnabled = args.answerButtonIsEnabled,
            highlight = args.highlight,
            isLatexQuest = isLatex(model.quest),
            isLatexAnswer = model.answers.any { isLatex(it) },
        )
    }

    private val latexRegex = Regex(
        "(\\$[^$]+\\$)|(\\$\\$[^$]+\\$\\$)|(\\\\\\([^)]*\\\\\\))|(\\\\\\[[^]]*\\\\])"
    )

    fun isLatex(text: String): Boolean {
        if (text.isBlank()) return false
        return latexRegex.containsMatchIn(text)
    }

    data class SimpleArgs(
        val answerButtonIsEnabled: Boolean,
        val highlight: HighlightUiModel,
    ) : UiMapperArgs
}