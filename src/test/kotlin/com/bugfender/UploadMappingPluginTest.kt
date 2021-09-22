package com.bugfender

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UploadMappingPluginTest {
    @Test
    fun apply() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply("com.bugfender.upload-mapping")

        assertTrue(project.plugins.hasPlugin("com.bugfender.upload-mapping"))
    }
}