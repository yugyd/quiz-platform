package com.yugyd.quiz.ui.simplequest.di

import com.yugyd.quiz.ui.game.api.GameViewModelDelegate
import com.yugyd.quiz.ui.simplequest.viewmodel.LatexSimpleGameViewModelDelegate
import com.yugyd.quiz.ui.simplequest.viewmodel.SimpleGameViewModelDelegate
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ViewModelComponent::class)
interface SimpleGameViewModelModule {

    @Binds
    @IntoSet
    fun bindsGameViewModelDelegate(impl: SimpleGameViewModelDelegate): GameViewModelDelegate

    @Binds
    @IntoSet
    fun bindsLatexSimpleGameViewModelDelegate(
        impl: LatexSimpleGameViewModelDelegate,
    ): GameViewModelDelegate
}
