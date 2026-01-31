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

package com.yugyd.quiz.ui.theme.model

import android.net.Uri
import com.yugyd.quiz.commonui.utils.ProgressColorUtils
import com.yugyd.quiz.core.percent
import com.yugyd.quiz.domain.api.model.Theme
import javax.inject.Inject
import androidx.core.net.toUri

internal class ThemeUiMapper @Inject constructor(
    private val progressColorUtils: ProgressColorUtils
) {

    fun map(model: Theme) = model.run {
        val progressPercent = percent(progress, count)

        val imageUri = if (model.image != null) {
            if (model.image!!.isHttpUrl()) {
                model.image!!.toUri()
            } else {
                "file:///android_asset/${model.image}".toUri()
            }
        } else {
            null
        }
        ThemeUiModel(
            id = id,
            title = name,
            subtitle = info,
            imageUri = imageUri,
            progressPercent = progressPercent,
            progressColor = progressColorUtils.getProgressColor(progressPercent),
            record = progress
        )
    }

    private fun String.isHttpUrl(): Boolean {
        return startsWith("http://") || startsWith("https://")
    }
}
