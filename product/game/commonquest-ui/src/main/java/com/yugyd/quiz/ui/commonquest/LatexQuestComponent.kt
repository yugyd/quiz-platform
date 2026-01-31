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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yugyd.quiz.ui.game.api.model.QuestValueUiModel
import com.yugyd.quiz.uikit.LatexText
import com.yugyd.quiz.uikit.R
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.component.QuizBackground
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme

private const val IMAGE_RATIO_16_9 = 1.77F

@Composable
fun LatexQuestComponent(quest: QuestValueUiModel) {
    Column {
        if (!quest.imageUri.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .aspectRatio(IMAGE_RATIO_16_9)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(size = 16.dp)
                    ),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(quest.imageUri)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.ic_no_photography_24),
                    error = painterResource(id = R.drawable.ic_no_photography_24),
                )
            }

            Spacer(
                modifier = Modifier.height(height = 16.dp),
            )
        }

        LatexText(
            text = quest.questText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        HorizontalDivider(Modifier.padding(vertical = 16.dp))
    }
}

@ThemePreviews
@Composable
private fun LatexQuestComponentPreview(
    @PreviewParameter(QuestValuePreviewParameterProvider::class) item: QuestValueUiModel,
) {
    QuizApplicationTheme {
        QuizBackground {
            LatexQuestComponent(quest = item)
        }
    }
}
