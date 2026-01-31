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

package com.yugyd.quiz.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri


fun Context.getTelegramIntent(channelLink: String): Intent {
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        "tg://resolve?domain=${channelLink}".toUri()
    )

    return if (appIntent.resolveActivity(packageManager) != null) {
        appIntent
    } else {
        val browseIntent = Intent(
            Intent.ACTION_VIEW,
            "https://t.me/${channelLink}".toUri()
        )
        browseIntent
    }
}
