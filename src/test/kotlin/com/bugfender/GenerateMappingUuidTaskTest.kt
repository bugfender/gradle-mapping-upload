package com.bugfender

import com.android.build.gradle.api.ApplicationVariant
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID

@ExtendWith(MockKExtension::class)
class GenerateMappingUuidTaskTest {
  private val project = ProjectBuilder.builder().build()

  @Test
  fun uploadMapping(@MockK variant: ApplicationVariant) {
    val outputDir = TemporaryFolder().let { it.create(); it }.newFolder()


    val task = project.tasks.register(
      "bfGenerateMappingUuidTest",
      GenerateMappingUuidTask::class.java,
      GenerateMappingUuidTask.constructor(
        project.layout.buildDirectory.dir(
          outputDir.path
        )
      )
    ).get()
    task.generateAsset()

    assert(outputDir.list()?.contains("bf_mapping_uid") == true)
    assert(UUID.fromString(File(outputDir, "bf_mapping_uid").readText()).toString().isNotEmpty())


  }
}