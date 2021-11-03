package com.bugfender

import com.android.build.gradle.api.ApplicationVariant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.lang.Exception

abstract class UploadMappingTask : DefaultTask() {
    companion object {
        private val FILE_MIME_TYPE = "text/plain; charset=utf-8".toMediaType()
        private val httpClient = OkHttpClient()
        private lateinit var requestBuilder: Request.Builder

        internal fun constructor(
            variant: ApplicationVariant,
            config: UploadMappingPluginExtension
        ): UploadMappingTask.() -> Unit {
            if (!config.symbolicationToken.isPresent) {
                throw Exception("Missing symbolicationToken in configuration")
            }

            val url = config.symbolicationURL.get()
            requestBuilder = Request.Builder()
                .url("${url}${if (url.last() == '/') "" else "/"}api/upload-symbols")
                .header("Authorization", "Bearer ${config.symbolicationToken.get()}")

            return {
                this.variant = variant
            }
        }
    }

    @Internal
    lateinit var variant: ApplicationVariant

    @TaskAction
    fun upload() {
        val fileProvider = variant.mappingFileProvider.get()
        val files = try {
            fileProvider.files
        } catch (_: MissingValueException) {
            this.logger.debug("No mapping provider available for {}", this.name)
            emptySet()
        }
        if (files.isEmpty()) {
            this.logger.debug("No mapping files found for {}", this.name)
            return
        }
        val firstFile = files.first()

        val reqBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("version", this.variant.versionName)
            .addFormDataPart("build", this.variant.versionCode.toString())
            .addFormDataPart("file", firstFile.name, firstFile.asRequestBody(FILE_MIME_TYPE))
            .build()
        val request = requestBuilder.post(reqBody).build()
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful) {
                    this.logger.error(
                        "Error response from mapping send request (status: `{}` message: `{}`",
                        response.message,
                        body?.string() ?: "",
                    )
                    throw GradleException("Server returned an error")
                }
                body?.close()
            }
        } catch (exc: Exception) {
            this.logger.error("Error sending mapping file", exc)
        }
    }
}