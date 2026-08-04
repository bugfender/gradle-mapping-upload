package com.bugfender

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class UploadMappingTaskTest {
    private val project = ProjectBuilder.builder().build()

    @Test
    fun uploadMapping() {
        val file = File.createTempFile("bfUploadMapping", "")
        file.writeText("TESTFILECONTENT")
        file.deleteOnExit()
        val webserver = MockWebServer()
        webserver.enqueue(MockResponse())
        webserver.start()

        val task = task("bfUploadMappingTest")
        task.mappingFiles.setFrom(file)
        task.symbolicationToken.set("00009999111133332222444455556666")
        task.symbolicationURL.set(webserver.url("").toString())
        task.versionName.set("1.0-test")
        task.versionCode.set(1)

        task.upload()

        val req = webserver.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/upload-symbols", req.path)
        assertEquals("Bearer 00009999111133332222444455556666", req.getHeader("Authorization"))
        assertFormData(req.body.readUtf8())

        webserver.shutdown()
    }

    @Test
    fun skipsUploadWithoutMappingFiles() {
        val webserver = MockWebServer()
        webserver.start()

        val task = task("bfUploadMappingEmpty")
        task.symbolicationToken.set("00009999111133332222444455556666")
        task.symbolicationURL.set(webserver.url("").toString())

        task.upload()

        assertEquals(0, webserver.requestCount)
        webserver.shutdown()
    }

    private fun task(name: String) =
        project.tasks.register(name, UploadMappingTask::class.java).get()

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
        assert(values["file"]!!.startsWith("PK")) // zip file
    }
}
