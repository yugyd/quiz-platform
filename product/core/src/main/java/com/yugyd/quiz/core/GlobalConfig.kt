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

package com.yugyd.quiz.core

import kotlin.properties.Delegates

object GlobalConfig {
    var AD_PROVIDER by Delegates.notNull<AdProviderType>()
    var DEBUG by Delegates.notNull<Boolean>()
    var APPLICATION_ID by Delegates.notNull<String>()
    var PRO_APP_PACKAGE by Delegates.notNull<String>()
    var DEV_ID by Delegates.notNull<String>()
    var VERSION_CODE by Delegates.notNull<Int>()
    var IS_BASED_ON_PLATFORM_APP by Delegates.notNull<Boolean>()
    var LOCALE_CODE by Delegates.notNull<String>()
}
