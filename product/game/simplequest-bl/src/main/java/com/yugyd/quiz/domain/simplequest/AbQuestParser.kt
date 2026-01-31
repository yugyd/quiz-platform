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

package com.yugyd.quiz.domain.simplequest

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AbQuestParser @Inject constructor(@ApplicationContext private val context: Context) :
    IAbQuestParser {
    private val aAnswer by lazy { context.getString(R.string.simple_quest_code_answer_a) }
    private val bAnswer by lazy { context.getString(R.string.simple_quest_code_answer_b) }
    private val yesAnswer by lazy { context.getString(R.string.simple_quest_code_answer_yes) }
    private val yesAnswerVariant by lazy { context.getString(R.string.simple_quest_code_answer_yes_variant) }
    private val noAnswer by lazy { context.getString(R.string.simple_quest_code_answer_no) }
    private val noAnswerVariant by lazy { context.getString(R.string.simple_quest_code_answer_no_variant) }
    private val aQuest by lazy { context.getString(R.string.simple_quest_code_quest_a) }
    private val aQuestVariant by lazy { context.getString(R.string.simple_quest_code_quest_a_variant) }
    private val bQuest by lazy { context.getString(R.string.simple_quest_code_quest_b) }
    private val bQuestVariant by lazy { context.getString(R.string.simple_quest_code_quest_b_variant) }

    private val answerVariables by lazy {
        listOf(aAnswer, bAnswer, yesAnswer, yesAnswerVariant, noAnswer, noAnswerVariant)
    }

    private val sortingAnswerVariables by lazy {
        listOf(aAnswer, bAnswer, yesAnswer, noAnswer)
    }

    private val sortingAnswerVariantVariables by lazy {
        listOf(aAnswer, bAnswer, yesAnswerVariant, noAnswerVariant)
    }

    private val questVariables by lazy {
        listOf(aQuest, aQuestVariant, bQuest, bQuestVariant)
    }

    override fun parse(model: SimpleQuestModel): SimpleQuestModel {
        return if (isAb(model)) {
            parseAbQuest(model)
        } else {
            model
        }
    }

    override fun parse(model: LatexSimpleQuestModel): LatexSimpleQuestModel {
        return model
    }

    private fun isAb(model: SimpleQuestModel) = model.answers.all(answerVariables::contains) &&
            questVariables.all(model.quest::contains)

    private fun parseAbQuest(model: SimpleQuestModel) = when (model.abType) {
        AbType.DEFAULT -> {
            parseToSortedAnswers(model, sortingAnswerVariables).let(::parseToQuests)
        }

        AbType.VARIANT -> {
            parseToSortedAnswers(model, sortingAnswerVariantVariables).let(::parseToQuests)
        }
    }

    private fun parseToSortedAnswers(
        model: SimpleQuestModel,
        answers: List<String>,
    ): SimpleQuestModel {
        val sortedAnswers = model.answers.sortedWith { one, two ->
            val oneIndex = answers.indexOf(one)
            val twoIndex = answers.indexOf(two)

            oneIndex.compareTo(twoIndex)
        }

        return model.copy(
            answers = sortedAnswers
        )
    }

    private fun parseToQuests(model: SimpleQuestModel): SimpleQuestModel {
        val quest = model.quest
        val abType = model.abType

        val indexQualifierA = when (abType) {
            AbType.DEFAULT -> quest.indexOf(aQuest)
            AbType.VARIANT -> quest.indexOf(aQuestVariant)
        }

        val indexQualifierB = when (abType) {
            AbType.DEFAULT -> quest.indexOf(bQuest)
            AbType.VARIANT -> quest.indexOf(bQuestVariant)
        }

        val mappedQuest = if (indexQualifierA > FIRST_INDEX && indexQualifierB > FIRST_INDEX) {
            getQualifierString(getQualifierString(quest, indexQualifierA), indexQualifierB)
        } else {
            quest
        }

        return model.copy(
            quest = mappedQuest
        )
    }

    private fun getQualifierString(quest: String, index: Int): String {
        val startPart = quest.substring(startIndex = FIRST_INDEX, endIndex = index)
        val endPart = quest.substring(startIndex = index)

        return buildString {
            append(startPart.trim())
            append(System.lineSeparator())
            append(System.lineSeparator())
            append(endPart.trim())
        }
    }

    private val SimpleQuestModel.abType: AbType
        get() {
            return if (answers.contains(yesAnswerVariant) && answers.contains(noAnswerVariant)) {
                AbType.VARIANT
            } else {
                AbType.DEFAULT
            }
        }

    private enum class AbType {
        DEFAULT, VARIANT
    }

    companion object {
        private const val FIRST_INDEX = 0
    }
}
