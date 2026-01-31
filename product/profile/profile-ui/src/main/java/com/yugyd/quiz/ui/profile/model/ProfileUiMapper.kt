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

package com.yugyd.quiz.ui.profile.model

import android.content.Context
import androidx.annotation.StringRes
import com.yugyd.quiz.commonui.model.content.ContentUiMapper
import com.yugyd.quiz.core.ResIdProvider
import com.yugyd.quiz.domain.api.model.Content
import com.yugyd.quiz.domain.options.model.Transition
import com.yugyd.quiz.featuretoggle.domain.model.telegram.TelegramConfig
import com.yugyd.quiz.ui.profile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class ProfileUiMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentUiMapper: ContentUiMapper,
    private val resIdProvider: ResIdProvider
) {

    private val appName by lazy(LazyThreadSafetyMode.NONE) {
        context.getString(resIdProvider.appName())
    }

    fun map(
        content: Content,
        transition: Transition,
        isSortedQuest: Boolean,
        isVibration: Boolean,
        isProFeatureEnabled: Boolean,
        isTelegramFeatureEnabled: Boolean,
        isContentFeatureEnabled: Boolean,
        telegramConfig: TelegramConfig?,
        contentTitle: String?,
        currentAiTitle: String?,
        isBasedOnPlatformApp: Boolean,
        isAiEnabled: Boolean,
        isAiFeatureEnabled: Boolean,
        isGamesServicesEnabled: Boolean,
    ) = listOfNotNull(
        header(content, isProFeatureEnabled),
        item(TypeProfile.TASKS, R.string.profile_tasks),
        section(
            type = TypeProfile.GAME_PROFILE_SECTION,
            titleRes = R.string.profile_title_game_profile,
            isSectionEnabled = isGamesServicesEnabled,
        ),
        item(
            type = TypeProfile.EXPERIENCE_RATING,
            titleRes = R.string.profile_rating,
            isItemEnabled = isGamesServicesEnabled
        ),
        item(
            type = TypeProfile.PROGRESS_RATING,
            titleRes = R.string.profile_rating_progress,
            isItemEnabled = isGamesServicesEnabled
        ),
        mapContentToValueItem(
            contentTitle = contentTitle,
            isContentEnabled = isContentFeatureEnabled,
        ),
        section(
            type = TypeProfile.AI_SECTION,
            titleRes = R.string.profile_title_section_ai,
            isSectionEnabled = isAiFeatureEnabled,
        ),
        mapAiToValueItem(
            aiName = currentAiTitle,
            isAiEnabled = isAiEnabled,
            isAiFeatureEnabled = isAiFeatureEnabled,
        ),
        section(
            type = TypeProfile.SOCIAL_SECTION,
            titleRes = R.string.profile_title_social,
            isSectionEnabled = isTelegramEnabledAndTelegramConfigNotNull(
                isTelegramFeatureEnabled = isTelegramFeatureEnabled,
                telegramConfig = telegramConfig,
            ),
        ),
        social(TypeProfile.TELEGRAM_SOCIAL, isTelegramFeatureEnabled, telegramConfig),
        section(TypeProfile.SETTINGS_SECTION, R.string.profile_title_settings),
        value(TypeProfile.TRANSITION, R.string.profile_title_show_answer, transition.value),
        switch(TypeProfile.SORT_QUEST, R.string.profile_title_sorting_quest, isSortedQuest),
        switch(TypeProfile.VIBRATION, R.string.profile_title_vibration, isVibration),
        getProItem(isProFeatureEnabled),
        getPurchaseSection(isProFeatureEnabled),
        section(TypeProfile.PLEASE_US_SECTION, R.string.profile_title_please_us),
        item(TypeProfile.RATE_APP, R.string.profile_title_rate_app),
        item(TypeProfile.SHARE, R.string.profile_title_share_app),
        item(TypeProfile.OTHER_APPS, R.string.profile_title_other_apps),
        section(TypeProfile.FEEDBACK_SECTION, R.string.profile_title_feedback),
        item(TypeProfile.REPORT_ERROR, R.string.profile_title_report_error),
        item(TypeProfile.PRIVACY_POLICY, R.string.profile_title_privacy_policy),
        getOpenSourceItem(isBasedOnPlatformApp),
    )

    fun mapContentToValueItem(
        contentTitle: String?,
        isContentEnabled: Boolean,
    ): ValueItemProfileUiModel? {
        return if (isContentEnabled) {
            value(
                TypeProfile.SELECT_CONTENT,
                R.string.profile_title_content,
                contentTitle ?: context.getString(R.string.profile_title_content_not_selected),
            )
        } else {
            null
        }
    }

    fun mapAiToValueItem(
        aiName: String?,
        isAiEnabled: Boolean,
        isAiFeatureEnabled: Boolean,
    ): ValueItemProfileUiModel? {
        return if (isAiFeatureEnabled) {
            val aiValueName = when {
                isAiEnabled && aiName != null -> aiName
                isAiEnabled -> context.getString(R.string.profile_title_ai_not_connected)
                else -> context.getString(R.string.profile_title_ai_off)
            }

            value(
                type = TypeProfile.AI_CONNECTION,
                titleRes = R.string.profile_title_ai,
                value = aiValueName,
            )
        } else {
            null
        }
    }

    private fun header(content: Content, isProFeatureEnabled: Boolean) = content.run {
        val contentUi = contentUiMapper.map(content)
        val contentTitle = context.getString(contentUi.title)
        val formattedVersionName = context.getString(R.string.profile_format_version, contentTitle)

        HeaderProfileUiModel(
            appName = appName,
            appIcon = resIdProvider.appIcon(),
            versionTitle = formattedVersionName,
            isVersionTitleVisible = isProFeatureEnabled
        )
    }

    private fun section(
        type: TypeProfile,
        @StringRes titleRes: Int,
        isSectionEnabled: Boolean = true,
    ): SectionProfileUiModel? {
        return if (isSectionEnabled) {
            SectionProfileUiModel(
                id = type.toString(),
                title = context.getString(titleRes)
            )
        } else {
            null
        }
    }

    private fun value(type: TypeProfile, @StringRes titleRes: Int, value: Double) =
        ValueItemProfileUiModel(
            id = type.toString(),
            type = type,
            title = context.getString(titleRes),
            value = context.getString(R.string.profile_format_time_second, value)
        )

    private fun value(type: TypeProfile, @StringRes titleRes: Int, value: String) =
        ValueItemProfileUiModel(
            id = type.toString(),
            type = type,
            title = context.getString(titleRes),
            value = value,
        )

    private fun switch(type: TypeProfile, @StringRes titleRes: Int, isChecked: Boolean) =
        SwitchItemProfileUiModel(
            id = type.toString(),
            type = type,
            title = context.getString(titleRes),
            isChecked = isChecked
        )

    private fun item(
        type: TypeProfile,
        @StringRes titleRes: Int,
        isItemEnabled: Boolean = true,
    ) = if (isItemEnabled) {
        SelectItemProfileUiModel(
            id = type.toString(),
            type = type,
            title = context.getString(titleRes)
        )
    } else {
        null
    }

    private fun social(
        type: TypeProfile,
        isTelegramFeatureEnabled: Boolean,
        telegramConfig: TelegramConfig?
    ): SocialItemProfileUiModel? {
        return if (
            isTelegramEnabledAndTelegramConfigNotNull(isTelegramFeatureEnabled, telegramConfig)
        ) {
            val telegramTitle = requireNotNull(telegramConfig).profileCell.title.ifBlank {
                context.getString(R.string.profile_title_telegram)
            }
            val telegramMsg = telegramConfig.profileCell.message.ifBlank {
                context.getString(R.string.profile_title_telegram_promo)
            }
            SocialItemProfileUiModel(
                id = type.toString(),
                type = type,
                title = telegramTitle,
                message = telegramMsg,
                icon = R.drawable.ic_telegram,
            )
        } else {
            null
        }
    }

    private fun isTelegramEnabledAndTelegramConfigNotNull(
        isTelegramFeatureEnabled: Boolean,
        telegramConfig: TelegramConfig?,
    ) = isTelegramFeatureEnabled && telegramConfig != null

    private fun getPurchaseSection(isProFeatureEnabled: Boolean): SelectItemProfileUiModel? {
        return if (isProFeatureEnabled) {
            item(TypeProfile.PRO, R.string.profile_title_pro_version)
        } else {
            null
        }
    }

    private fun getProItem(isProFeatureEnabled: Boolean): SectionProfileUiModel? {
        return if (isProFeatureEnabled) {
            section(TypeProfile.PURCHASES_SECTION, R.string.profile_title_purchases)
        } else {
            null
        }
    }

    private fun getOpenSourceItem(isBasedOnPlatformApp: Boolean) = if (isBasedOnPlatformApp) {
        OpenSourceProfileUiModel(
            id = TypeProfile.OPEN_SOURCE.toString(),
            type = TypeProfile.OPEN_SOURCE,
        )
    } else {
        null
    }
}
