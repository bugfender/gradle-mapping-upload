package com.bugfender

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class UploadMappingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.extensions.create("bugfender", UploadMappingPluginExtension::class.java)

        project.pluginManager.withPlugin("com.android.application") {
            val app = project.extensions.getByType(AppExtension::class.java)
            // This makes sure that we get Variants after they are populated
            project.afterEvaluate {
                app.applicationVariants.configureEach { variant ->
                    if (!variant.buildType.isMinifyEnabled) {
                        project.logger.debug("Minification is disabled for {}", variant.name)
                        return@configureEach
                    }

                    val assembleTask = variant.assembleProvider.orNull ?: run {
                        project.logger.warn("No assemble provider available for {}", variant.name)
                        return@configureEach
                    }

                    val constructor = UploadMappingTask.constructor(variant, config)
                    val task = project.tasks.register(
                        "bfUploadMapping${variant.name.capitalize()}",
                        UploadMappingTask::class.java,
                        constructor,
                    )
                    assembleTask.finalizedBy(task)
                }
            }
        }
    }
}