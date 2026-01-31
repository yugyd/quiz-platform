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

package com.yugyd.quiz

import com.yugyd.quiz.core.ResIdProvider
import com.yugyd.quiz.core.TextModel
import javax.inject.Inject
import com.yugyd.quiz.uikit.R as uiKitR

internal class ResIdProviderImpl @Inject constructor() : ResIdProvider {
    override fun appIcon() = R.mipmap.ic_launcher
    override fun appRoundIcon() = R.mipmap.ic_launcher_round
    override fun pushIcon() = uiKitR.drawable.ic_message_24
    override fun appName() = R.string.app_name
    override fun msgProAdBanner() = R.string.app_msg_pro_ad_banner
    override fun appTelegramChat() = R.string.main_app_telegram_chat_name
    override fun msgProAdBannerString() = TextModel.ResTextModel(res = msgProAdBanner())
    override fun appPrivacyPolicyLink() = R.string.app_privacy_policy_link
    override fun gameExperienceLeaderboard() = R.string.id_game_experience_league_leaderboard
    override fun gameWalkthroughLeaderboard() = R.string.id_game_walkthrough_league_leaderboard
}
