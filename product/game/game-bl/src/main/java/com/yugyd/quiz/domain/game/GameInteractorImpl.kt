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

package com.yugyd.quiz.domain.game

import com.yugyd.quiz.core.GameServicesIdProvider
import com.yugyd.quiz.core.coroutinesutils.AppScopeProvider
import com.yugyd.quiz.core.coroutinesutils.DispatchersProvider
import com.yugyd.quiz.domain.aitasks.AiTasksInteractor
import com.yugyd.quiz.domain.api.model.Mode
import com.yugyd.quiz.domain.api.model.Record
import com.yugyd.quiz.domain.api.model.game.ControlModel
import com.yugyd.quiz.domain.api.model.game.GameModel
import com.yugyd.quiz.domain.api.model.payload.GameEndPayload
import com.yugyd.quiz.domain.api.model.payload.GamePayload
import com.yugyd.quiz.domain.api.repository.ErrorSource
import com.yugyd.quiz.domain.api.repository.PreferencesSource
import com.yugyd.quiz.domain.api.repository.QuestSource
import com.yugyd.quiz.domain.api.repository.RecordSource
import com.yugyd.quiz.domain.api.repository.SectionSource
import com.yugyd.quiz.domain.api.repository.TrainSource
import com.yugyd.quiz.domain.controller.ErrorController
import com.yugyd.quiz.domain.controller.RecordController
import com.yugyd.quiz.domain.controller.SectionController
import com.yugyd.quiz.domain.favorites.FavoritesSource
import com.yugyd.quiz.domain.game.api.BaseQuestDomainModel
import com.yugyd.quiz.domain.game.api.TrueAnswerResultModel
import com.yugyd.quiz.domain.game.api.model.HighlightModel
import com.yugyd.quiz.domain.game.api.model.Quest
import com.yugyd.quiz.domain.game.exception.FinishGameException
import com.yugyd.quiz.domain.game.exception.RewardedGameException
import com.yugyd.quiz.domain.game.model.GameState
import com.yugyd.quiz.domain.game.quest.QuestInteractorHolder
import com.yugyd.quiz.domain.utils.SeparatorParser
import com.yugyd.quiz.featuretoggle.domain.FeatureManager
import com.yugyd.quiz.featuretoggle.domain.model.FeatureToggle
import com.yugyd.quiz.game.api.GameClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class GameInteractorImpl @Inject constructor(
    private val questSource: QuestSource,
    private val sectionSource: SectionSource,
    private val trainSource: TrainSource,
    private val recordSource: RecordSource,
    private val errorSource: ErrorSource,
    private val preferencesSource: PreferencesSource,
    private val interactorHolder: QuestInteractorHolder,
    private val separatorParser: SeparatorParser,
    private val recordController: RecordController,
    private val sectionController: SectionController,
    private val errorController: ErrorController,
    private val dispatcherProvider: DispatchersProvider,
    private val favoritesSource: FavoritesSource,
    private val aiTasksInteractor: AiTasksInteractor,
    private val gameClient: GameClient,
    private val gameServicesIdProvider: GameServicesIdProvider,
    private val featureManager: FeatureManager,
    private val appScopeProvider: AppScopeProvider,
) : GameInteractor {

    private lateinit var payload: GamePayload
    private val gameMode get() = payload.mode
    private val themeId get() = payload.themeId
    private val sectionId get() = payload.sectionId
    private val record get() = payload.record

    private lateinit var data: GameModel
    private val questIds get() = data.questIds
    private val sectionIds get() = data.sectionIds
    private val trainIds get() = data.trainIds
    private val errorIds get() = data.errorIds
    private val gameSessionErrors get() = data.gameSessionErrors
    private val condition get() = data.condition
    private val point get() = data.point
    private val questCount get() = data.questCount
    private val isFinished get() = data.isFinished
    private val totalTime get() = data.endTime - data.startTime
    private val isShowRewarded get() = data.isShowRewarded
    private val isHaveRewardedItem get() = data.isHaveRewardedItem

    private val endPayload
        get() = GameEndPayload(
            mode = gameMode,
            themeId = themeId,
            oldRecord = record,
            point = point,
            count = questCount,
            errorQuestIds = gameSessionErrors.toList(),
            isRewardedSuccess = isHaveRewardedItem
        )

    override suspend fun startGame(payload: GamePayload) = withContext(dispatcherProvider.io) {
        this@GameInteractorImpl.payload = payload
        data = GameModel(
            condition = getConditionValue(gameMode),
            startTime = System.currentTimeMillis()
        )

        val questIds = getQuestIds(
            mode = payload.mode,
            theme = payload.themeId,
            section = payload.sectionId,
            isSort = preferencesSource.isSortingQuest
        )

        data = data.copy(
            questIds = questIds,
            questCount = questIds.size
        )

        continueGame()
    }

    override suspend fun continueGame() = withContext(dispatcherProvider.io) {
        isNext(questIds)

        if (condition > 0) {
            data = data.copy(steep = data.steep.inc())
            val nextQuest = nextQuest()
            val nextControl = nextControl()
            GameState(
                quest = nextQuest,
                control = nextControl,
            )
        } else {
            getFinishError()
        }
    }

    override suspend fun resultAnswer(
        quest: BaseQuestDomainModel,
        selectedUserAnswers: Set<String>,
        userAnswer: String,
    ) = withContext(dispatcherProvider.io) {
        val trueAnswerResult = isTrueAnswer(
            quest = quest,
            selectedUserAnswers = selectedUserAnswers,
            userAnswer = userAnswer,
        )
        if (trueAnswerResult.isValid) {
            addRightQuest(gameMode, quest.id)
            incrementPoint()
            addSectionQuest(gameMode, quest.id)
            addTrainQuest(gameMode, quest.id)
            getHighlightModel(quest, selectedUserAnswers, true)
        } else {
            addErrorQuest(gameMode, quest.id)
            decrementCondition(gameMode)
            getHighlightModel(
                quest = quest,
                selectedUserAnswers = selectedUserAnswers,
                isSuccess = false,
            )
        }
    }

    private suspend fun isTrueAnswer(
        quest: BaseQuestDomainModel,
        selectedUserAnswers: Set<String>,
        userAnswer: String,
    ): TrueAnswerResultModel {
        return interactorHolder.isTrueAnswer(
            quest = quest,
            selectedUserAnswers = selectedUserAnswers,
            enteredUserAnswer = userAnswer,
        )
    }

    override suspend fun finishGame() = withContext(dispatcherProvider.io) {
        data = data.copy(
            isFinished = isFinished,
            endTime = System.currentTimeMillis()
        )

        saveData(gameMode)

        if (featureManager.isFeatureEnabled(FeatureToggle.GAME_SERVICES)) {
            appScopeProvider.ioScope.launch {
                saveScoreInGameServices(gameMode)
            }
        }

        updateController()

        endPayload
    }

    override suspend fun firstFinishGame() {
        if (gameMode == Mode.TRAIN) {
            finishGame()
        }
    }

    override suspend fun onUserEarnedReward() = withContext(dispatcherProvider.io) {
        data = data.copy(
            condition = condition.inc(),
            isHaveRewardedItem = true
        )
    }

    private fun getConditionValue(mode: Mode) = when (mode) {
        Mode.ARCADE, Mode.TRAIN, Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS -> LIFE_CONDITION
        Mode.NONE -> 0
    }

    private suspend fun getQuestIds(mode: Mode, theme: Int, section: Int, isSort: Boolean) =
        when (mode) {
            Mode.ARCADE -> getQuestIdsBySection(theme, section, isSort)
            Mode.TRAIN -> getQuestIdsByTrainMode(theme, isSort)
            Mode.ERROR -> getQuestIdsByErrors()
            Mode.FAVORITE -> getQuestIdsByFavorites()
            Mode.AI_TASKS -> getQuestIdsByAiTasks(aiThemeId = themeId)
            Mode.NONE -> emptyList()
        }

    private suspend fun getQuestIdsByTheme(theme: Int, isSort: Boolean) =
        questSource.getQuestIds(
            themeId = theme,
            isSort = isSort,
        )

    private suspend fun getQuestIdsBySection(theme: Int, section: Int, isSort: Boolean) =
        questSource.getQuestIdsBySection(
            themeId = theme,
            sectionId = section,
            isSort = isSort,
        )

    private suspend fun getQuestIdsByTrainMode(theme: Int, isSort: Boolean) = trainSource
        .getAll()
        .let { trainIds ->
            if (trainIds.isNotEmpty()) {
                getQuestIdsByTheme(
                    theme = theme,
                    isSort = isSort,
                ).filterNot(trainIds::contains)
            } else {
                getQuestIdsByTheme(
                    theme = theme,
                    isSort = isSort,
                )
            }
        }

    private suspend fun getQuestIdsByErrors() = errorSource.getErrors().shuffled()

    private suspend fun getQuestIdsByFavorites() = favoritesSource.getTaskIds().shuffled()

    private suspend fun getQuestIdsByAiTasks(aiThemeId: Int) = aiTasksInteractor.getQuestIds(
        aiThemeId = aiThemeId,
    ).shuffled()

    private fun isNext(ids: List<Int>) {
        if (ids.isEmpty()) throw FinishGameException()
    }

    private suspend fun nextQuest() = getQuest(
        themeId = themeId,
        questId = getQuestId()
    )
        .let(::getQuestModel)

    private fun nextControl() = ControlModel(
        mode = payload.mode,
        condition = data.condition,
        point = data.point,
        steep = data.steep,
        questCount = data.questCount
    )

    private fun getQuestId(): Int {
        val id = questIds.first()
        data = data.copy(questIds = questIds.minus(id))
        return id
    }

    private suspend fun getQuest(themeId: Int, questId: Int): Quest {
        return when (gameMode) {
            Mode.ARCADE, Mode.TRAIN, Mode.ERROR, Mode.FAVORITE -> {
                questSource
                    .getQuest(questId)
                    .let(separatorParser::parse)
            }

            Mode.AI_TASKS -> {
                aiTasksInteractor
                    .getQuest(
                        aiThemeId = themeId,
                        aiQuestId = questId,
                    )
                    .let(separatorParser::parse)
            }

            Mode.NONE -> error("Invalid game mode")
        }
    }

    private fun getQuestModel(quest: Quest): BaseQuestDomainModel {
        return interactorHolder.getQuestModel(quest)
    }

    private fun incrementPoint() {
        data = data.copy(point = point.inc())
    }

    private fun decrementCondition(mode: Mode) {
        val fine = when (mode) {
            Mode.ARCADE, Mode.ERROR -> LIFE_FINE
            Mode.TRAIN, Mode.FAVORITE, Mode.AI_TASKS -> INFINITY_FINE
            Mode.NONE -> 0
        }
        data = data.copy(condition = condition.minus(fine))
    }

    private suspend fun addErrorQuest(mode: Mode, id: Int) {
        val isAddError = when (mode) {
            Mode.ERROR, Mode.AI_TASKS, Mode.NONE -> false
            Mode.ARCADE, Mode.TRAIN, Mode.FAVORITE -> true
        }

        if (isAddError) {
            data = data.copy(errorIds = errorIds.plus(id))
            errorSource.addError(id)
        }

        data = data.copy(gameSessionErrors = gameSessionErrors.plus(id))
    }

    private suspend fun addRightQuest(mode: Mode, id: Int) {
        if (mode == Mode.ERROR) errorSource.removeError(id)
    }

    private fun addSectionQuest(mode: Mode, id: Int) {
        if (mode == Mode.ARCADE) {
            data = data.copy(sectionIds = sectionIds.plus(id))
        }
    }

    private fun addTrainQuest(mode: Mode, id: Int) {
        if (mode == Mode.TRAIN) {
            data = data.copy(trainIds = trainIds.plus(id))
        }
    }

    private fun getHighlightModel(
        quest: BaseQuestDomainModel,
        selectedUserAnswers: Set<String>,
        isSuccess: Boolean,
    ): HighlightModel {
        return interactorHolder.getHighlightModel(
            quest = quest,
            selectedUserAnswers = selectedUserAnswers,
            isSuccess = isSuccess,
        )
    }

    private fun getFinishError(): Nothing {
        if (!isShowRewarded && gameMode != Mode.TRAIN) {
            data = data.copy(isShowRewarded = true)
            throw RewardedGameException()
        } else {
            throw FinishGameException()
        }
    }

    private suspend fun saveData(mode: Mode) {
        if (!isSaveRecord(mode)) return

        when (mode) {
            Mode.ARCADE -> saveSectionData()
            Mode.TRAIN -> saveTrainData()
            Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS, Mode.NONE -> Unit
        }

        saveRecord(mode)
    }

    private fun isSaveRecord(mode: Mode) = when (mode) {
        Mode.TRAIN -> {
            point != 0
        }

        Mode.ARCADE, Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS -> {
            point > record
        }

        Mode.NONE -> false
    }

    private suspend fun saveSectionData() {
        val resetIds = questSource.getQuestIdsBySection(
            themeId = themeId,
            sectionId = sectionId,
        )
        sectionSource.deleteSectionIds(resetIds)
        sectionSource.updateSectionIds(sectionIds.toList())
    }

    private suspend fun saveTrainData() = trainSource.addAll(trainIds.toList())

    private suspend fun saveRecord(mode: Mode) {
        if (!isSavedRecordMode(mode)) return

        val oldRecordModel = recordSource.getRecord(themeId, mode)
        val record = getPoint(mode)

        if (oldRecordModel != null) {
            val updateRecordModel = oldRecordModel.copy(
                record = record,
                totalTime = oldRecordModel.totalTime.plus(totalTime)
            )

            recordSource.updateRecord(updateRecordModel)
        } else {
            val recordModel = Record(
                themeId = themeId,
                mode = mode,
                record = record,
                totalTime = totalTime
            )

            recordSource.addRecord(recordModel)
        }
    }

    private suspend fun getPoint(mode: Mode) = when (mode) {
        Mode.ARCADE -> {
            val allIds = questSource.getQuestIds(themeId)
            sectionSource.getSectionTotalProgress(allIds.toIntArray())
        }

        Mode.TRAIN -> {
            val allIds = questSource.getQuestIds(themeId)
            trainSource.getTotalProgress(allIds.toIntArray())
        }

        Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS, Mode.NONE -> {
            point
        }
    }

    private fun isSavedRecordMode(mode: Mode) = when (mode) {
        Mode.ARCADE, Mode.TRAIN -> true
        Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS, Mode.NONE -> false
    }

    private suspend fun saveScoreInGameServices(mode: Mode) {
        gameClient.getAndSubmitScore(
            leaderboardId = gameServicesIdProvider.gameExperienceLeaderboard(),
            score = point.toLong(),
        )

        when (mode) {
            Mode.ARCADE, Mode.TRAIN -> {
                val allQuestIds = questSource.getQuestIds().toIntArray()

                val sections = sectionSource.getAll().count(allQuestIds::contains)
                val train = trainSource.getAll().count(allQuestIds::contains)

                val newRecord = sections + train
                gameClient.submitTotalScore(
                    leaderboardId = gameServicesIdProvider.gameWalkthroughLeaderboard(),
                    score = newRecord.toLong(),
                )
            }

            Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS, Mode.NONE -> Unit
        }
    }

    private suspend fun updateController() = withContext(dispatcherProvider.main) {
        when (gameMode) {
            Mode.ARCADE -> {
                recordController.notifyListeners()
                sectionController.notifyListeners()
            }

            Mode.TRAIN -> {
                recordController.notifyListeners()
            }

            Mode.ERROR, Mode.FAVORITE, Mode.AI_TASKS, Mode.NONE -> Unit
        }

        errorController.notifyListeners()
    }

    companion object {
        private const val LIFE_FINE = 1
        private const val INFINITY_FINE = 0

        private const val LIFE_CONDITION = 2
    }
}
