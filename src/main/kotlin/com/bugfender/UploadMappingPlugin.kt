package com.bugfender

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

abstract class UploadMappingPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    val config = project.extensions.create("bugfender", UploadMappingPluginExtension::class.java)

    project.logger.debug("[Bugfender] Activating plugin")

    project.pluginManager.withPlugin("com.android.application") {
      val app = project.extensions.getByType(AppExtension::class.java)
      project.logger.debug("[Bugfender] Found an Android application")

      // This makes sure that we get Variants after they are populated
      project.afterEvaluate {
        app.applicationVariants.configureEach { variant ->
          project.logger.debug("[Bugfender] Attempting to apply plugin to {}", variant.name)

          if (!variant.buildType.isMinifyEnabled) {
            project.logger.lifecycle("[Bugfender] Not applying to {}: minification is disabled", variant.name)
            return@configureEach
          }

          val assembleTask = variant.assembleProvider.orNull
          val bundleTask = project.tasks.findByName("bundle${variant.name.capitalize()}")
          if (assembleTask == null && bundleTask == null) {
            project.logger.warn("[Bugfender] No assemble or bundle provider available for {}", variant.name)
            return@configureEach
          }

          // Generate Asset file with UUID
          val generateMappingUuidTask = configureGenerateMappingUuidTask(app, project, variant)
          // Upload UUID generated in the previous task
          val uploadMappingTask = configureUploadMappingTask(project, variant, generateMappingUuidTask,config)

          assembleTask?.finalizedBy(uploadMappingTask)
          bundleTask?.finalizedBy(uploadMappingTask)

          project.logger.debug("[Bugfender] Applied plugin to {}", variant.name)
        }
      }
    }
  }

  private fun configureGenerateMappingUuidTask(app: AppExtension, project: Project, variant: ApplicationVariant): TaskProvider<GenerateMappingUuidTask> {
    val capitalName = variant.name.capitalize()

    val generateMappingUuidTask = project.tasks.register(
      "bfGenerateMappingUuid${capitalName}",
      GenerateMappingUuidTask::class.java,
      GenerateMappingUuidTask.constructor(
        project.layout.buildDirectory.dir(
          "generated${File.separator}assets${File.separator}bugfender${File.separator}${variant.name}"
        )
      )
    )

    // Task must be executed prior merging assets
    variant.mergeAssetsProvider.orNull?.dependsOn(generateMappingUuidTask)

    // The following dependencies are declared to avoid warnings about implicit dependencies
    project.tasks.findByName("lintVitalAnalyze$capitalName")?.dependsOn(generateMappingUuidTask)
    project.tasks.findByName("lintVitalReport$capitalName")?.dependsOn(generateMappingUuidTask)

    // Task must be executed prior bundle
    project.tasks.findByName("build${capitalName}PreBundle")?.dependsOn(generateMappingUuidTask)

    // Task must be executed prior package (when assembling)
    variant.packageApplicationProvider.orNull?.dependsOn(generateMappingUuidTask)

    // Task must be executed prior package (when bundling)
    project.tasks.findByName("package${capitalName}Bundle")?.dependsOn(generateMappingUuidTask)

    // Add generated file to the Assets
    app.sourceSets.getByName(variant.name).assets.srcDir(
      generateMappingUuidTask.flatMap { it.output }
    )

    return generateMappingUuidTask
  }

  private fun configureUploadMappingTask(project: Project, variant: ApplicationVariant, generateMappingUuidTask: TaskProvider<GenerateMappingUuidTask>,
                                         config: UploadMappingPluginExtension): TaskProvider<UploadMappingTask> {

    return project.tasks.register(
      "bfUploadMapping${variant.name.capitalize()}",
      UploadMappingTask::class.java,
      UploadMappingTask.constructor(variant, config, generateMappingUuidTask.flatMap { it.outputFile })
    )
  }
}