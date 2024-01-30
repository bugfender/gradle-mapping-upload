package com.bugfender

import com.android.build.gradle.api.ApplicationVariant
import com.bugfender.okhttp3.ZipFileRequestBody
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.internal.provider.MissingValueException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.time.Duration


abstract class UploadMappingTask : DefaultTask() {
    companion object {
        private val httpClient = OkHttpClient.Builder()
          .connectTimeout(Duration.ofSeconds(600))
          .readTimeout(Duration.ofSeconds(600))
          .writeTimeout(Duration.ofSeconds(600))
          .build()
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

        val reqBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("version", this.variant.versionName)
            .addFormDataPart("build", this.variant.versionCode.toString())
            .addFormDataPart("file", "gradle-mappings.zip", ZipFileRequestBody(files))
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