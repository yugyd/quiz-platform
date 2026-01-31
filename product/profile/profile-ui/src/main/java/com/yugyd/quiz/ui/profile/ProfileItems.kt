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

package com.yugyd.quiz.ui.profile

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.yugyd.quiz.ui.profile.model.HeaderProfileUiModel
import com.yugyd.quiz.ui.profile.model.OpenSourceProfileUiModel
import com.yugyd.quiz.ui.profile.model.ProfileUiModel
import com.yugyd.quiz.ui.profile.model.SectionProfileUiModel
import com.yugyd.quiz.ui.profile.model.SelectItemProfileUiModel
import com.yugyd.quiz.ui.profile.model.SocialItemProfileUiModel
import com.yugyd.quiz.ui.profile.model.SwitchItemProfileUiModel
import com.yugyd.quiz.ui.profile.model.ValueItemProfileUiModel
import com.yugyd.quiz.uikit.common.ThemePreviews
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme

@Composable
internal fun ProfileItem(
    model: ProfileUiModel,
    onItemClicked: (ProfileUiModel) -> Unit,
    onItemChecked: (SwitchItemProfileUiModel, Boolean) -> Unit,
    onRatePlatformClicked: () -> Unit,
    onReportBugPlatformClicked: () -> Unit,
) {
    when (model) {
        is HeaderProfileUiModel -> {
            HeaderProfileItem(model)
        }

        is SectionProfileUiModel -> {
            SectionProfileItem(model.title)
        }

        is SelectItemProfileUiModel -> {
            SelectProfileItem(
                model = model,
                onItemClicked = onItemClicked
            )
        }

        is ValueItemProfileUiModel -> {
            ValueProfileItem(
                model = model,
                onItemClicked = onItemClicked
            )
        }

        is SwitchItemProfileUiModel -> {
            SwitchProfileItem(
                model = model,
                onItemChecked = onItemChecked,
            )
        }

        is SocialItemProfileUiModel -> {
            TelegramProfileItem(
                model = model,
                onItemClicked = onItemClicked,
            )
        }

        is OpenSourceProfileUiModel -> {
            OpenSourceProfileItem(
                onRatePlatformClicked = onRatePlatformClicked,
                onReportBugPlatformClicked = onReportBugPlatformClicked,
            )
        }
    }
}

@Composable
internal fun HeaderProfileItem(model: HeaderProfileUiModel) {
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val drawable = AppCompatResources.getDrawable(
                LocalContext.current,
                model.appIcon
            )

            Image(
                painter = rememberDrawablePainter(drawable = drawable),
                contentDescription = null,
                modifier = Modifier.size(size = 96.dp),
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            Text(
                text = model.appName,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            Text(
                text = model.versionTitle,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
internal fun OpenSourceProfileItem(
    onRatePlatformClicked: () -> Unit,
    onReportBugPlatformClicked: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.profile_content_banner_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.profile_content_banner_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = onReportBugPlatformClicked,
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_content_banner_report_error),
                    )
                }

                Spacer(modifier = Modifier.width(width = 8.dp))

                Button(
                    onClick = onRatePlatformClicked,
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_content_banner_rate),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SectionProfileItem(sectionTitle: String) {
    Column {
        Divider()
        ListItem(
            headlineContent = {
                Text(
                    text = sectionTitle,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        )
    }
}

@Composable
internal fun SelectProfileItem(
    model: SelectItemProfileUiModel,
    onItemClicked: (ProfileUiModel) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = model.title)
        },
        modifier = Modifier.clickable {
            onItemClicked(model)
        }
    )
}

@Composable
internal fun ValueProfileItem(
    model: ValueItemProfileUiModel,
    onItemClicked: (ProfileUiModel) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = model.title)
        },
        modifier = Modifier.clickable {
            onItemClicked(model)
        },
        trailingContent = {
            Text(
                text = model.value,
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
internal fun SwitchProfileItem(
    model: SwitchItemProfileUiModel,
    onItemChecked: (SwitchItemProfileUiModel, Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = model.title)
        },
        trailingContent = {
            Switch(
                checked = model.isChecked,
                onCheckedChange = {
                    onItemChecked(model, it)
                }
            )
        }
    )
}

@Composable
internal fun TelegramProfileItem(
    model: SocialItemProfileUiModel,
    onItemClicked: (ProfileUiModel) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = model.title)
        },
        modifier = Modifier.clickable {
            onItemClicked(model)
        },
        supportingContent = {
            Text(text = model.message)
        },
        leadingContent = {
            Image(
                painter = painterResource(id = model.icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        },
    )
}

@ThemePreviews
@Composable
private fun SectionProfileItemPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) items: List<ProfileUiModel>,
) {
    QuizApplicationTheme {
        Surface {
            SectionProfileItem(
                sectionTitle = items.filterIsInstance<SectionProfileUiModel>().first().title
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SelectProfileItemPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) items: List<ProfileUiModel>,
) {
    QuizApplicationTheme {
        Surface {
            SelectProfileItem(
                model = items.filterIsInstance<SelectItemProfileUiModel>().first(),
                onItemClicked = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ValueProfileItemPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) items: List<ProfileUiModel>,
) {
    QuizApplicationTheme {
        Surface {
            ValueProfileItem(
                model = items.filterIsInstance<ValueItemProfileUiModel>().first(),
                onItemClicked = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SwitchProfileItemPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) items: List<ProfileUiModel>,
) {
    QuizApplicationTheme {
        Surface {
            SwitchProfileItem(
                model = items.filterIsInstance<SwitchItemProfileUiModel>().first(),
                onItemChecked = { _, _ -> },
            )
        }
    }
}

@ThemePreviews
@Composable
private fun HeaderProfileItemPreview(
    @PreviewParameter(ProfilePreviewParameterProvider::class) items: List<ProfileUiModel>,
) {
    QuizApplicationTheme {
        Surface {
            HeaderProfileItem(
                model = items.filterIsInstance<HeaderProfileUiModel>().first(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun OpenSourceProfileItemPreview() {
    QuizApplicationTheme {
        Surface {
            OpenSourceProfileItem(
                onRatePlatformClicked = {},
                onReportBugPlatformClicked = {},
            )
        }
    }
}
