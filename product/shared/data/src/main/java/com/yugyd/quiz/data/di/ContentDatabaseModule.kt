package com.yugyd.quiz.data.di

import android.content.Context
import androidx.room.Room
import com.yugyd.quiz.core.GlobalConfig
import com.yugyd.quiz.data.database.content.ContentDatabase
import com.yugyd.quiz.data.database.content.dao.ContentResetDao
import com.yugyd.quiz.data.database.content.dao.QuestDao
import com.yugyd.quiz.data.database.content.dao.ThemeDao
import com.yugyd.quiz.data.database.content.migrations.MIGRATION_5_6
import com.yugyd.quiz.data.database.content.migrations.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentDatabaseModule {

    @Singleton
    @Provides
    internal fun provideContentDatabase(
        @ApplicationContext appContext: Context,
        contentDbNameProvider: ContentDatabaseNameProvider,
    ): ContentDatabase {
        val contentDbName = contentDbNameProvider.getName(appContext)

        val room = Room
            .databaseBuilder(
                appContext,
                ContentDatabase::class.java,
                contentDbName,
            )
            .let {
                if (GlobalConfig.IS_BASED_ON_PLATFORM_APP) {
                    it.createFromAsset(contentDbName)
                } else {
                    it
                        .addMigrations(
                            MIGRATION_5_6,
                            MIGRATION_6_7,
                        )
                }
            }
            .fallbackToDestructiveMigration()
            .build()
        return room
    }

    @Provides
    internal fun provideThemeDao(db: ContentDatabase): ThemeDao = db.themeDao()

    @Provides
    internal fun provideQuestDao(db: ContentDatabase): QuestDao = db.questDao()

    @Provides
    internal fun provideContentResetDao(db: ContentDatabase): ContentResetDao = db.resetDao()
}