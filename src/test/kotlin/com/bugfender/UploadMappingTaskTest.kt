package com.bugfender

import com.android.build.gradle.api.ApplicationVariant
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
class UploadMappingTaskTest {
    private val project = ProjectBuilder.builder().build()

    @Test
    fun uploadMapping(@MockK variant: ApplicationVariant) {
        val file = File.createTempFile("bfUploadMapping", "")
        file.writeText("TESTFILECONTENT")
        file.deleteOnExit()
        every { variant.mappingFileProvider.get() } returns mockk(relaxed = true) {
            every { files } returns setOf(file)
        }
        every { variant.versionName } returns "1.0-test"
        every { variant.versionCode } returns 1
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse())
        webserver.start()
        val url = webserver.url("")

        val config = project.extensions.create("bugfender", UploadMappingPluginExtension::class.java)
        config.apiKey("00009999111133332222444455556666")
        println(url.toString())
        config.apiURL(url.toString())

        val task = project.tasks.register(
            "bfUploadMappingTest",
            UploadMappingTask::class.java,
            UploadMappingTask.constructor(variant, config),
        ).get()
        task.upload()

        val req = webserver.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/upload-symbols", req.path)
        assertFormData(req.body.readUtf8())

        webserver.shutdown()
    }

    private fun assertFormData(data: String) {
        val values = mutableMapOf<String, String>()
        val parts = data.split("--")
        for (part in parts.slice(1 until parts.size - 2)) {
            val s = part.split("\r\n\r\n")
            val name = s[0].split("name=\"")[1].split('"')[0]

            values[name] = s[1].dropLast(2)
        }
        assertArrayEquals(arrayOf("build", "file", "version"), values.keys.sorted().toTypedArray())
        assertEquals("1.0-test", values["version"])
        assertEquals("1", values["build"])
        assertEquals("TESTFILECONTENT", values["file"])
    }

    @Test
    fun missingApiKey(@MockK variant: ApplicationVariant) {
        val config = project.extensions.create("bugfender", UploadMappingPluginExtension::class.java)

        val exc = assertThrows<Exception>("Should throw exception on missing API key") {
            UploadMappingTask.constructor(variant, config)
        }
        assertEquals("Missing apiKey in configuration", exc.message)
    }
}