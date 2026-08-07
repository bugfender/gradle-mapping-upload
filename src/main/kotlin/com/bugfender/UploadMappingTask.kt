package com.bugfender

import com.bugfender.okhttp3.ZipFileRequestBody
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.time.Duration


abstract class UploadMappingTask : DefaultTask() {
    companion object {
        private val httpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(600))
                .readTimeout(Duration.ofSeconds(600))
                .writeTimeout(Duration.ofSeconds(600))
                .build()
        }
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFiles: ConfigurableFileCollection

    @get:Input
    abstract val symbolicationToken: Property<String>

    @get:Input
    abstract val symbolicationURL: Property<String>

    @get:Input
    @get:Optional
    abstract val versionName: Property<String>

    @get:Input
    @get:Optional
    abstract val versionCode: Property<Int>

    @TaskAction
    fun upload() {
        val files = this.mappingFiles.files.filterTo(mutableSetOf()) { it.isFile }
        if (files.isEmpty()) {
            this.logger.debug("No mapping files found for {}", this.name)
            return
        }

        val url = this.symbolicationURL.get()
        val reqBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("version", this.versionName.getOrElse(""))
            .addFormDataPart("build", this.versionCode.orNull?.toString() ?: "")
            .addFormDataPart("file", "gradle-mappings.zip", ZipFileRequestBody(files))
            .build()
        val request = Request.Builder()
            .url("${url}${if (url.last() == '/') "" else "/"}api/upload-symbols")
            .header("Authorization", "Bearer ${this.symbolicationToken.get()}")
            .post(reqBody)
            .build()
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful) {
                    this.logger.error(
                        "Error response from mapping send request (status: `{}` message: `{}`",
                        response.message,
                        body?.string() ?: "",
                    )
                    return
                }
                body?.close()
            }
        } catch (exc: Exception) {
            this.logger.error("Error sending mapping file", exc)
        }
    }
}
