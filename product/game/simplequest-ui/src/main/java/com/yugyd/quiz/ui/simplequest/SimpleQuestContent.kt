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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.yugyd.quiz.ui.commonquest.QuestComponent
import com.yugyd.quiz.ui.game.api.model.HighlightUiModel
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme
import com.yugyd.quiz.uikit.theme.app_color_negative
import com.yugyd.quiz.uikit.theme.app_color_positive

@Composable
fun SimpleQuestContent(
    quest: SimpleQuestUiModel,
    onAnswerClicked: (String) -> Unit,
) {
    Column {
        QuestComponent(quest = quest.questModel)

        AnswerButtons(
            highlight = quest.highlight,
            answers = quest.answers,
            onAnswerClicked = onAnswerClicked,
            answerButtonIsEnabled = quest.answerButtonIsEnabled,
        )
    }
}

@Composable
internal fun AnswerButtons(
    highlight: HighlightUiModel,
    answers: List<String>,
    answerButtonIsEnabled: Boolean,
    onAnswerClicked: (String) -> Unit,
) {
    answers.forEachIndexed { buttonIndex, answer ->
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
internal fun AnswerButton(
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
        modifier = Modifier.fillMaxWidth(),
        colors = buttonColors,
        shape = RectangleShape,
        contentPadding = PaddingValues(
            start = horizontalMargin,
            top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding(),
            end = horizontalMargin,
            bottom = ButtonDefaults.TextButtonContentPadding.calculateBottomPadding(),
        ),
    ) {
        Text(
            text = answer,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
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
