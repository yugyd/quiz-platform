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

package com.yugyd.quiz.uikit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme

@Composable
fun WarningContent(
    modifier: Modifier = Modifier,
    retryTitle: String? = null,
    isRetryButtonEnabled: Boolean = false,
    onRetryClicked: (() -> Unit)? = null,
) {
    WarningContent(
        icon = R.drawable.ic_cloud_off_24,
        title = stringResource(id = R.string.ds_empty_state_title),
        message = stringResource(id = R.string.ds_empty_state_description),
        modifier = modifier,
        retryTitle = retryTitle,
        isRetryButtonEnabled = isRetryButtonEnabled,
        onRetryClicked = onRetryClicked,
    )
}

@Composable
fun WarningContent(
    @DrawableRes icon: Int,
    message: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.ds_empty_state_title),
    retryTitle: String? = null,
    isRetryButtonEnabled: Boolean = false,
    onRetryClicked: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.then(
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.size(64.dp),
            painter = painterResource(id = icon),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (isRetryButtonEnabled && onRetryClicked != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetryClicked,
                enabled = isRetryButtonEnabled,
            ) {
                Text(
                    text = retryTitle ?: stringResource(id = R.string.ds_empty_state_retry),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun WarningContentPreview() {
    QuizApplicationTheme {
        Surface {
            WarningContent()
        }
    }
}

@ThemePreviews
@Composable
fun WarningWithButtonContentPreview() {
    QuizApplicationTheme {
        Surface {
            WarningContent(
                isRetryButtonEnabled = true,
                onRetryClicked = {},
            )
        }
    }
}
