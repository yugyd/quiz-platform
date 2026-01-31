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

package com.yugyd.quiz.ui.main

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.yugyd.quiz.core.GlobalConfig
import com.yugyd.quiz.commonui.R as CommonUiR
import com.yugyd.quiz.uikit.R as UiKitR

internal object GlobalScreens {

    fun pro() = externalStore(
        url = "$APP_URL_STORE${GlobalConfig.PRO_APP_PACKAGE}"
    )

    fun rate() = externalStore(
        url = "$APP_URL_STORE${GlobalConfig.APPLICATION_ID}"
    )

    fun share(): Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "$APP_URL_STORE${GlobalConfig.APPLICATION_ID}")
        type = "text/plain"
    }.let {
        Intent.createChooser(it, null)
    }

    fun otherApps() = externalStore(
        url = "$DEV_URL_STORE${GlobalConfig.DEV_ID}"
    )

    fun privacyPolicy(privacyPolicyLink: String) = externalBrowser(url = privacyPolicyLink)

    private fun externalStore(url: String) = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
    }

    fun platformGitHubProject(context: Context) = externalBrowser(
        url = context.getString(R.string.main_link_quiz_platform_project),
    )

    fun platformGitHubIssues(context: Context) = externalBrowser(
        url = context.getString(R.string.main_link_quiz_platform_issues),
    )

    fun externalBrowser(url: String) = Intent(Intent.ACTION_VIEW, url.toUri())

    fun externalReportError(context: Context): Intent {
        val email = context.getString(CommonUiR.string.common_app_email)
        val appName = context.getString(UiKitR.string.app_name)
        val title = context.getString(CommonUiR.string.common_title_message_error)
        val body = context.getString(CommonUiR.string.common_message_describe_error)
        val chooserTitle = context.getString(CommonUiR.string.common_title_select_messenger)
        val subject = context.getString(
            CommonUiR.string.common_format_message_subject,
            appName,
            title
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.let {
            Intent.createChooser(it, chooserTitle)
        }
    }

    private const val APP_URL_STORE = "https://play.google.com/store/apps/details?id="
    private const val DEV_URL_STORE = "https://play.google.com/store/apps/dev?id="
}
