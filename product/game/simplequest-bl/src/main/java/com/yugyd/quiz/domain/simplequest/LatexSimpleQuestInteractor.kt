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

package com.yugyd.quiz.domain.simplequest

import com.yugyd.quiz.domain.game.api.BaseQuestDomainModel
import com.yugyd.quiz.domain.game.api.QuestInteractor
import com.yugyd.quiz.domain.game.api.TrueAnswerResultModel
import com.yugyd.quiz.domain.game.api.model.HighlightModel
import com.yugyd.quiz.domain.game.api.model.Quest
import com.yugyd.quiz.domain.game.api.model.QuestType
import javax.inject.Inject

class LatexSimpleQuestInteractor @Inject constructor(
    private val abQuestParser: IAbQuestParser,
) : QuestInteractor {

    override fun isTypeHandled(type: QuestType): Boolean {
        return type == QuestType.SIMPLE_LATEX
    }

    override fun isQuestHandled(quest: BaseQuestDomainModel): Boolean {
        return quest is LatexSimpleQuestModel
    }

    override suspend fun isTrueAnswer(
        quest: BaseQuestDomainModel,
        selectedUserAnswers: Set<String>,
        enteredUserAnswer: String
    ): TrueAnswerResultModel {
        val isValid = selectedUserAnswers == (quest as LatexSimpleQuestModel).trueAnswers
        return TrueAnswerResultModel(isValid = isValid)
    }

    override fun getQuestModel(quest: Quest): BaseQuestDomainModel {
        val answers = listOf(
            quest.answer2,
            quest.answer3,
            quest.answer4,
            quest.answer5,
            quest.answer6,
            quest.answer7,
            quest.answer8,
        )
            .filter { it.isNotEmpty() }
            .shuffled()
            .take(3)
            .plus(quest.trueAnswer)
            .shuffled()
        val simpleQuest = LatexSimpleQuestModel(
            id = quest.id,
            quest = quest.quest,
            trueAnswers = setOf(quest.trueAnswer),
            answers = answers,
        )
        return abQuestParser.parse(simpleQuest)
    }

    override fun getHighlightModel(
        quest: BaseQuestDomainModel,
        selectedUserAnswers: Set<String>,
        isSuccess: Boolean
    ): HighlightModel {
        val highlightModel = if (isSuccess) {
            HighlightModel.State.TRUE
        } else {
            HighlightModel.State.FALSE
        }

        val trueAnswerIndexes = (quest as LatexSimpleQuestModel).trueAnswers
            .map(quest.answers::indexOf)
            .toSet()

        return HighlightModel(
            state = highlightModel,
            trueAnswerIndexes = trueAnswerIndexes,
            selectedAnswerIndex = selectedUserAnswers.map(quest.answers::indexOf).toSet(),
        )
    }
}