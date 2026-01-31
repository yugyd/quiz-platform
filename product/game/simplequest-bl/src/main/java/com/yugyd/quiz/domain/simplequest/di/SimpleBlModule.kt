package com.yugyd.quiz.domain.simplequest.di

import com.yugyd.quiz.domain.game.api.QuestInteractor
import com.yugyd.quiz.domain.simplequest.AbQuestParser
import com.yugyd.quiz.domain.simplequest.IAbQuestParser
import com.yugyd.quiz.domain.simplequest.LatexSimpleQuestInteractor
import com.yugyd.quiz.domain.simplequest.SimpleQuestInteractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ViewModelComponent::class)
interface SimpleBlModule {

    @Binds
    fun bindsIAbQuestParser(impl: AbQuestParser): IAbQuestParser

    @Binds
    @IntoSet
    fun bindsQuestInteractor(impl: SimpleQuestInteractor): QuestInteractor

    @Binds
    @IntoSet
    fun bindsLatexSimpleQuestInteractor(impl: LatexSimpleQuestInteractor): QuestInteractor
}
