/*
 * Copyright 2025 Roman Likhachev
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

package com.yugyd.quiz.data.di

import android.content.Context
import com.yugyd.quiz.core.GlobalConfig
import com.yugyd.quiz.core.Logger
import javax.inject.Inject

class ContentDatabaseNameProvider @Inject constructor(
    private val logger: Logger,
) {

    fun getName(appContext: Context): String {
        val contentDbName = if (GlobalConfig.IS_BASED_ON_PLATFORM_APP) {
            val localeCode = GlobalConfig.LOCALE_CODE
            if (localeCode.isNotEmpty()) {
                val contentWithLocale = "content-encode-pro-$localeCode.db"

                if (appContext.assetExists(contentWithLocale)) {
                    contentWithLocale
                } else {
                    DEFAULT_CONTENT_DB_NAME
                }
            } else {
                DEFAULT_CONTENT_DB_NAME
            }
        } else {
            DEFAULT_CONTENT_DB_NAME
        }

        logger.print("New content db name = $contentDbName")

        return contentDbName
    }

    private fun Context.assetExists(fileName: String): Boolean {
        return assets.list("")?.contains(fileName) == true
    }

    companion object {
        private const val DEFAULT_CONTENT_DB_NAME = "content-encode-pro.db"
    }
}