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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.yugyd.quiz.ui.commonquest.LatexQuestComponent
import com.yugyd.quiz.ui.commonquest.QuestComponent
import com.yugyd.quiz.ui.game.api.model.HighlightUiModel
import com.yugyd.quiz.uikit.LatexText
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.theme.app_color_negative
import com.yugyd.quiz.uikit.theme.app_color_positive

@Composable
fun LatexSimpleQuestContent(
    quest: LatexSimpleQuestUiModel,
    onAnswerClicked: (String) -> Unit,
) {
    Column {
        if (quest.isLatexQuest) {
            LatexQuestComponent(quest = quest.questModel)
        } else {
            QuestComponent(quest = quest.questModel)
        }

        LatexAnswerButtons(
            highlight = quest.highlight,
            answers = quest.answers,
            onAnswerClicked = onAnswerClicked,
            answerButtonIsEnabled = quest.answerButtonIsEnabled,
            isLatex = quest.isLatexAnswer,
        )
    }
}

@Composable
internal fun LatexAnswerButtons(
    highlight: HighlightUiModel,
    answers: List<String>,
    answerButtonIsEnabled: Boolean,
    isLatex: Boolean,
    onAnswerClicked: (String) -> Unit,
) {
    answers.forEachIndexed { buttonIndex, answer ->
        if (isLatex) {
            LatexAnswerButton(
                answer = answer,
                textColor = getButtonColor(highlight, buttonIndex),
                isEnabled = answerButtonIsEnabled,
                onAnswerClicked = {
                    onAnswerClicked(answer)
                },
            )
        } else {
            AnswerButton(
                answer = answer,
                textColor = getButtonColor(highlight, buttonIndex),
                isEnabled = answerButtonIsEnabled,
                onAnswerClicked = {
                    onAnswerClicked(answer)
                },
            )
        }
    }
}

private fun getButtonColor(
    highlight: HighlightUiModel,
    buttonIndex: Int,
): Color? {
    return when (highlight) {
        HighlightUiModel.Default -> null

        is HighlightUiModel.False -> {
            when (buttonIndex) {
                highlight.trueAnswerIndexes.first() -> app_color_positive
                highlight.falseIndexes.first() -> app_color_negative
                else -> null
            }
        }

        is HighlightUiModel.True -> {
            if (buttonIndex == highlight.trueAnswerIndexes.first()) {
                app_color_positive
            } else {
                null
            }
        }
    }
}

@Composable
internal fun LatexAnswerButton(
    answer: String,
    textColor: Color?,
    isEnabled: Boolean,
    onAnswerClicked: () -> Unit,
) {
    val buttonColors = if (textColor != null) {
        ButtonDefaults.textButtonColors(contentColor = textColor)
    } else {
        ButtonDefaults.textButtonColors()
    }
    val horizontalMargin = 16.dp
    TextButton(
        onClick = {
            if (isEnabled) {
                onAnswerClicked()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        colors = buttonColors,
        shape = RoundedCornerShape(size = 4.dp),
        contentPadding = PaddingValues(
            start = horizontalMargin,
            top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding(),
            end = horizontalMargin,
            bottom = ButtonDefaults.TextButtonContentPadding.calculateBottomPadding(),
        ),
        border = if (textColor != null) {
            BorderStroke(width = 2.dp, color = textColor)
        } else {
            null
        },
    ) {
        LatexText(
            text = answer,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@ThemePreviews
@Composable
private fun ContentPreview(
    @PreviewParameter(SimpleQuestPreviewParameterProvider::class) item: SimpleQuestUiModel,
) {
    QuizApplicationTheme {
        QuizBackground {
            SimpleQuestContent(
                quest = item,
                onAnswerClicked = {},
            )
        }
    }
}
