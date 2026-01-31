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

package com.yugyd.quiz.ui.simplequest.viewmodel

import com.yugyd.quiz.domain.game.api.BaseQuestDomainModel
import com.yugyd.quiz.domain.simplequest.LatexSimpleQuestModel
import com.yugyd.quiz.ui.game.api.GameViewModelDelegate
import com.yugyd.quiz.ui.game.api.model.BaseQuestUiModel
import com.yugyd.quiz.ui.game.api.model.HighlightUiModel
import com.yugyd.quiz.ui.game.api.model.ProcessAnswerResultModel
import com.yugyd.quiz.ui.simplequest.LatexSimpleQuestUiMapper
import com.yugyd.quiz.ui.simplequest.LatexSimpleQuestUiModel
import javax.inject.Inject

class LatexSimpleGameViewModelDelegate @Inject constructor(
    private val simpleQuestUiMapper: LatexSimpleQuestUiMapper,
) : GameViewModelDelegate {

    override fun isTypeHandled(domainQuest: BaseQuestDomainModel): Boolean {
        return domainQuest is LatexSimpleQuestModel
    }

    override fun processUserAnswer(
        domainQuest: BaseQuestDomainModel,
        quest: BaseQuestUiModel,
        userAnswer: String,
        isSelected: Boolean,
    ): ProcessAnswerResultModel {
        quest as LatexSimpleQuestUiModel

        return ProcessAnswerResultModel(
            quest = quest.copy(selectedAnswer = userAnswer),
            isLastAnswer = false,
        )
    }

    override fun getUserAnswers(
        domainQuest: BaseQuestDomainModel,
        quest: BaseQuestUiModel,
    ): Set<String> {
        quest as LatexSimpleQuestUiModel

        return setOf(quest.selectedAnswer).filterNotNull().toSet()
    }

    override fun getQuestUiModel(domainQuest: BaseQuestDomainModel): BaseQuestUiModel {
        domainQuest as LatexSimpleQuestModel

        val mapperArgs = LatexSimpleQuestUiMapper.SimpleArgs(
            answerButtonIsEnabled = true,
            highlight = HighlightUiModel.Default,
        )
        return simpleQuestUiMapper.map(
            model = domainQuest,
            args = mapperArgs,
        )
    }

    override fun updateQuestUiModel(
        domainQuest: BaseQuestDomainModel,
        quest: BaseQuestUiModel,
        highlight: HighlightUiModel,
        args: GameViewModelDelegate.GameViewModelDelegateArgs?
    ): BaseQuestUiModel {
        quest as LatexSimpleQuestUiModel

        return quest.copy(
            highlight = highlight,
            answerButtonIsEnabled = (args as SimpleGameViewModelDelegateArgs).answerButtonIsEnabled,
        )
    }

    override fun getArgs(
        domainQuest: BaseQuestDomainModel,
        userAnswer: String,
        answerButtonIsEnabled: Boolean,
    ): GameViewModelDelegate.GameViewModelDelegateArgs {
        return SimpleGameViewModelDelegateArgs(answerButtonIsEnabled = answerButtonIsEnabled)
    }

    data class SimpleGameViewModelDelegateArgs(
        val answerButtonIsEnabled: Boolean,
    ) : GameViewModelDelegate.GameViewModelDelegateArgs
}