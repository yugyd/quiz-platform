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

package com.yugyd.quiz.uikit

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.rememberWebViewState
import com.yugyd.quiz.uikit.theme.QuizApplicationTheme

class NoTouchWebView(context: Context) : WebView(context) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun performClick(): Boolean {
        return false
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LatexText(
    text: String,
    modifier: Modifier = Modifier,
) {
    var webView: WebView? by remember { mutableStateOf(null) }

    val isDark = isSystemInDarkTheme()
    val state = if (isDark) {
        rememberWebViewState("file:///android_asset/latex_render.html")
    } else {
        rememberWebViewState("file:///android_asset/latex_render_light.html")
    }

    if (state.loadingState is LoadingState.Finished) {
        val formatted = text.quoteForJs()
        webView?.loadUrl("javascript:addBody('$formatted')")

        webView?.isFocusableInTouchMode = false
        webView?.isClickable = false
        webView?.isFocusable = false
    }

    com.google.accompanist.web.WebView(
        state = state,
        captureBackPresses = false,
        modifier = modifier.then(Modifier.fillMaxWidth()),
        onCreated = {
            try {
                it.settings.javaScriptEnabled = true
            } catch (expected: Throwable) {
                expected.printStackTrace()
            }
            webView = it
            it.isClickable = false
            it.isFocusable = false
            it.isFocusableInTouchMode = false
            it.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
            it.setBackgroundColor(0)
        },
        factory = {
            NoTouchWebView(it)
        }
    )
}

private fun String.quoteForJs(): String {
    return this
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}

@Preview
@Composable
private fun LatexTextPreview() {
    QuizApplicationTheme {
        LatexText(
            text = "Решите уравнение \\\\sin(x) \\\\cdot \\\\cos(y) \\\\cdot \\\\sin(x \\\\cdot y, где a и b — натуральные числа.)",
        )
    }
}
