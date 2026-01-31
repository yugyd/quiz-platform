/*
 * Copyright 2024 Roman Likhachev
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

package com.yugyd.quiz.ui.commonquest

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yugyd.quiz.ui.game.api.model.QuestValueUiModel

internal class LatexQuestValuePreviewParameterProvider :
    PreviewParameterProvider<QuestValueUiModel> {

    override val values: Sequence<QuestValueUiModel> = sequenceOf(
        createQuest(),
        createQuest(
            image = "https://github.com/Yugyd/quiz-platform/blob/master/app/src/main/ic_launcher-playstore.png?raw=true",
        )
    )

    private fun createQuest(image: String? = null) = QuestValueUiModel(
        questText = "\\frac{a}{b} + \\sqrt{c^2 + d^2}",
        imageUri = image,
    )
}
