package com.bugfender

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.VariantOutputConfiguration
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class UploadMappingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.extensions.create("bugfender", UploadMappingPluginExtension::class.java)
        project.logger.debug("[Bugfender] Activating plugin")

        project.pluginManager.withPlugin("com.android.application") {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            project.logger.debug("[Bugfender] Found an Android application")

            androidComponents.onVariants(androidComponents.selector().all()) { variant ->
                project.logger.debug("[Bugfender] Attempting to apply plugin to {}", variant.name)

                if (!variant.isMinifyEnabled) {
                    project.logger.debug("[Bugfender] Not applying to {}: minification is disabled", variant.name)
                    return@onVariants
                }

                val token = config.symbolicationToken.orNull
                    ?: throw GradleException("Missing symbolicationToken in configuration")

                val mainOutput = variant.outputs.firstOrNull {
                    it.outputType == VariantOutputConfiguration.OutputType.SINGLE
                } ?: variant.outputs.firstOrNull()

                val capitalName = variant.name.capitalize()
                val uploadTask = project.tasks.register(
                    "bfUploadMapping${capitalName}",
                    UploadMappingTask::class.java,
                ) { task ->
                    task.mappingFiles.setFrom(variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE))
                    task.symbolicationToken.set(token)
                    task.symbolicationURL.set(config.symbolicationURL)
                    mainOutput?.let {
                        task.versionName.set(it.versionName)
                        task.versionCode.set(it.versionCode)
                    }
                }

                // The assemble/bundle tasks are created after this callback runs, so react to them lazily rather than looking them up now.
                val lifecycleTaskNames = setOf("assemble${capitalName}", "bundle${capitalName}")
                project.tasks.matching { it.name in lifecycleTaskNames }.configureEach {
                    it.finalizedBy(uploadTask)
                }

                project.logger.debug("[Bugfender] Applied plugin to {}", variant.name)
            }
        }
    }
}
