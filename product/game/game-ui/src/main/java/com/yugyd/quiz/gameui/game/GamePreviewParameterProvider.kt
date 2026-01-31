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

package com.yugyd.quiz.gameui.game

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yugyd.quiz.gameui.game.model.ConditionUiModel
import com.yugyd.quiz.gameui.game.model.ControlUiModel
import com.yugyd.quiz.gameui.game.model.GameStateUiModel
import com.yugyd.quiz.ui.enteraiquest.EnterAiQuestUiModel
import com.yugyd.quiz.ui.enterquest.EnterQuestUiModel
import com.yugyd.quiz.ui.game.api.model.HighlightUiModel
import com.yugyd.quiz.ui.game.api.model.QuestUiType
import com.yugyd.quiz.ui.game.api.model.QuestValueUiModel
import com.yugyd.quiz.ui.hintenterquest.EnterWithHintQuestUiModel
import com.yugyd.quiz.ui.selectboolquest.SelectBoolQuestUiModel
import com.yugyd.quiz.ui.selectmanualquest.SelectManualQuestAnswerUiModel
import com.yugyd.quiz.ui.selectmanualquest.SelectManualQuestUiModel
import com.yugyd.quiz.ui.selectquest.SelectQuestUiModel
import com.yugyd.quiz.ui.simplequest.LatexSimpleQuestUiModel
import com.yugyd.quiz.ui.simplequest.SimpleQuestUiModel
import com.yugyd.quiz.uikit.theme.app_color_negative

internal class GamePreviewParameterProvider :
    PreviewParameterProvider<List<GameStateUiModel>> {

    override val values: Sequence<List<GameStateUiModel>>
        get() = sequenceOf(
            QuestUiType.entries.map {
                GameStateUiModel(
                    control = createControl(),
                    quest = createQuest(it),
                )
            }
        )

    private fun createControl() = ControlUiModel(
        id = 1,
        countTitle = "1",
        conditionIcon = Icons.Filled.Favorite,
        conditionTitle = "",
        conditionTintColor = app_color_negative,
        conditionUiModel = ConditionUiModel.LIFE,
        conditionIsVisible = true,
        progressPercent = 30,
        progressColor = app_color_negative,
        isBanner = true,
    )

    private fun createQuest(questUiType: QuestUiType) = when (questUiType) {
        QuestUiType.SIMPLE -> {
            SimpleQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                answers = listOf("One answer", "Two answer", "Three answer", "Four answer"),
                selectedAnswer = null,
                answerButtonIsEnabled = false,
                highlight = HighlightUiModel.Default,
            )
        }

        QuestUiType.SIMPLE_LATEX -> {
            LatexSimpleQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                answers = listOf("One answer", "Two answer", "Three answer", "Four answer"),
                selectedAnswer = null,
                answerButtonIsEnabled = false,
                highlight = HighlightUiModel.Default,
                isLatexQuest = true,
                isLatexAnswer = true,
            )
        }

        QuestUiType.ENTER -> {
            EnterQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                userAnswer = "Answer",
                isNumberKeyboard = true,
                isFieldEnabled = true,
                answerState = EnterQuestUiModel.AnswerState.NONE,
                trueAnswer = "True",
            )
        }

        QuestUiType.ENTER_WITH_HINT -> {
            EnterWithHintQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                userAnswer = "Answer",
                isNumberKeyboard = true,
                isFieldEnabled = true,
                answerHint = "Hint",
                answerState = EnterWithHintQuestUiModel.AnswerState.NONE,
                trueAnswer = "True",
            )
        }

        QuestUiType.SELECT_MANUAL -> {
            SelectManualQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                answers = buildList {
                    repeat(5) {
                        add(
                            SelectManualQuestAnswerUiModel(
                                text = it.toString(),
                                isSelected = it % 2 == 0,
                            )
                        )
                    }
                },
                answerButtonIsEnabled = true,
                highlight = HighlightUiModel.Default,
            )
        }

        QuestUiType.SELECT -> {
            SelectQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                answers = buildList {
                    repeat(5) {
                        add(
                            SelectManualQuestAnswerUiModel(
                                text = it.toString(),
                                isSelected = it % 2 == 0,
                            )
                        )
                    }
                },
                answerButtonIsEnabled = true,
                highlight = HighlightUiModel.Default,
            )
        }

        QuestUiType.ENTER_AI -> {
            EnterAiQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                userAnswer = "Answer",
                isFieldEnabled = true,
                answerState = EnterAiQuestUiModel.AnswerState.NONE,
                answerDescriptionFromAi = "True answer from AI",
                answerButtonIsEnabled = true,
            )
        }

        QuestUiType.SELECT_BOOL -> {
            SelectBoolQuestUiModel(
                questModel = QuestValueUiModel("Quest"),
                answers = listOf("True", "False"),
                selectedAnswer = "False",
                answerButtonIsEnabled = true,
                highlight = HighlightUiModel.Default,
            )
        }
    }
}
