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

package com.yugyd.quiz.ui.errors

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class AiClientPreviewParameterProvider : PreviewParameterProvider<String> {

    override val values: Sequence<String>
        get() = sequenceOf(
            generateTestMd()
        )

    private fun generateTestMd(): String {
        return """            
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus.
            
            # Contributions

            [Guide](docs/CONTRIBUTION.md)

            # Build

            ## Build debug

            - `./gradlew clean assembleDevDebug`
            
            ## Code integration

            * Switch the `isProductFlavorFilterEnabled` property to `false` in the
              [BuildTypeAndroidApplicationPlugin.kt](build-logic/convention/src/main/kotlin/com/yugyd/buildlogic/convention/buildtype/BuildTypeAndroidApplicationPlugin.kt)
            * Switch the `IS_BASED_ON_PLATFORM_APP` property to `true` in the [build.gradle](app/build.gradle)
              file.
              
            # Code snippet
            
            ```kotlin
            fun main() {
                println("Hello, World!")
            }
            ```
            
            # License

            ```
               Copyright 2023 Roman Likhachev
            
               Licensed under the Apache License, Version 2.0 (the "License");
               you may not use this file except in compliance with the License.
               You may obtain a copy of the License at
            
                   http://www.apache.org/licenses/LICENSE-2.0
            
               Unless required by applicable law or agreed to in writing, software
               distributed under the License is distributed on an "AS IS" BASIS,
               WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
               See the License for the specific language governing permissions and
               limitations under the License.
            ```
        """.trimIndent()
    }
}
