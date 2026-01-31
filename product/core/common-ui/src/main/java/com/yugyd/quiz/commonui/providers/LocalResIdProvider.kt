package com.yugyd.quiz.commonui.providers

import androidx.compose.runtime.compositionLocalOf
import com.yugyd.quiz.core.ResIdProvider
import com.yugyd.quiz.core.TextModel

val LocalResIdProvider = compositionLocalOf<ResIdProvider> {
    DefaultResIdProvider()
}

private class DefaultResIdProvider : ResIdProvider {
    override fun appIcon(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun appRoundIcon(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun pushIcon(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun appName(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun msgProAdBanner(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun appTelegramChat(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun appPrivacyPolicyLink(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun msgProAdBannerString(): TextModel {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun gameExperienceLeaderboard(): Int {
        throw IllegalStateException("Res not created in default factory")
    }

    override fun gameWalkthroughLeaderboard(): Int {
        throw IllegalStateException("Res not created in default factory")
    }
}
