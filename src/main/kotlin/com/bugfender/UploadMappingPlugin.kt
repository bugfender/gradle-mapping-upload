package com.bugfender

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class UploadMappingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.extensions.create("bugfender", UploadMappingPluginExtension::class.java)
        project.logger.debug("[Bugfender] Activating plugin")

        project.pluginManager.withPlugin("com.android.application") {
            val app = project.extensions.getByType(AppExtension::class.java)
            project.logger.debug("[Bugfender] Found an Android application")

            // This makes sure that we get Variants after they are populated
            project.afterEvaluate {
                app.applicationVariants.configureEach { variant ->
                    project.logger.debug("[Bugfender] Applying plugin to {}", variant.name)

                    if (!variant.buildType.isMinifyEnabled) {
                        project.logger.lifecycle("[Bugfender] Not applying to {}, minification is disabled", variant.name)
                        return@configureEach
                    }

                    val assembleTask = variant.assembleProvider.orNull ?: run {
                        project.logger.warn("[Bugfender] No assemble provider available for {}", variant.name)
                        return@configureEach
                    }

                    val constructor = UploadMappingTask.constructor(variant, config)
                    val task = project.tasks.register(
                        "bfUploadMapping${variant.name.capitalize()}",
                        UploadMappingTask::class.java,
                        constructor,
                    )
                    assembleTask.finalizedBy(task)

                    project.logger.debug("[Bugfender] Applied plugin to {}", variant.name)
                }
            }
        }
    }
}