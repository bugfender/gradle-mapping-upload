package com.bugfender

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.UUID

abstract class GenerateMappingUuidTask : DefaultTask() {

  @get:OutputDirectory
  abstract val output: DirectoryProperty

  @get:Internal
  val outputFile: Provider<RegularFile> get() = output.file("bf_mapping_uid");

  companion object {
    internal fun constructor(
      output: Provider<Directory>
    ): GenerateMappingUuidTask.() -> Unit {
      return {
        this.output.set(output)
      }
    }
  }

  @TaskAction
  fun generateAsset() {
    val outputDir = output.get().asFile
    outputDir.mkdirs()

    outputFile.get().asFile.writeText(UUID.randomUUID().toString())
  }
}