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

package com.yugyd.quiz.featuretoggle.domain.model

enum class FeatureToggle(val key: String, val isLocal: Boolean, val localValue: Boolean = false) {
    AD(key = "feature_ad", isLocal = false),
    AD_INTERSTITIAL_GAME_END(key = "feature_ad_interstitial_game_end", isLocal = false),
    AD_BANNER_GAME(key = "feature_ad_banner_game", isLocal = false),
    AD_REWARDED_THEME(key = "feature_ad_rewarded_theme", isLocal = false),
    PRO(key = "feature_pro", isLocal = false),
    CORRECT(key = "feature_correct", isLocal = false),
    TELEGRAM(key = "feature_telegram", isLocal = false),
    AI(key = "feature_ai", isLocal = true, localValue = true),
    AI_ASSISTANT(key = "feature_ai_assistant", isLocal = true, localValue = true),
    AI_COURSES(key = "feature_ai_courses", isLocal = true, localValue = true),
    AI_TASKS(key = "feature_ai_tasks", isLocal = true, localValue = true),
    GAME_SERVICES(key = "feature_game_services", isLocal = true, localValue = true),
}
