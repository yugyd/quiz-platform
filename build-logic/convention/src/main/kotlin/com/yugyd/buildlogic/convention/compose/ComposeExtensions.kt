/*
 *    Copyright 2024 Roman Likhachev
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

package com.yugyd.buildlogic.convention.compose

import com.android.build.api.dsl.CommonExtension
import com.yugyd.buildlogic.convention.core.DEBUG_IMPLEMENTATION
import com.yugyd.buildlogic.convention.core.IMPLEMENTATION
import com.yugyd.buildlogic.convention.core.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun CommonExtension<*, *, *, *, *, *>.configureCompose(target: Project) {
    buildFeatures.compose = true

    target.dependencies {
        val bom = target.libs.findLibrary("compose-bom").get()
        add(IMPLEMENTATION, platform(bom))

        // Enable preview
        add(DEBUG_IMPLEMENTATION, target.libs.findLibrary("compose-ui-tooling").get())
        add(IMPLEMENTATION, target.libs.findLibrary("compose-ui-tooling-preview").get())
    }

    configureComposeReports(target)
}

private fun configureComposeReports(target: Project) {
    target.tasks.withType(KotlinCompile::class.java).configureEach {
        compilerOptions {
            val buildDir = target.layout.buildDirectory.get().asFile
            val composeReportsFolder = buildDir.resolve("compose_compiler")

            if (target.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs.addAll(
                    getComposeCompilerProperty(
                        arg = "reportsDestination",
                        path = composeReportsFolder.absolutePath
                    )
                )
            }

            if (target.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs.addAll(
                    getComposeCompilerProperty(
                        arg = "metricsDestination",
                        path = composeReportsFolder.absolutePath
                    )
                )
            }
        }
    }
}

private fun getComposeCompilerProperty(arg: String, path: String) = listOf(
    "-P",
    "plugin:androidx.compose.compiler.plugins.kotlin:$arg=$path"
)
