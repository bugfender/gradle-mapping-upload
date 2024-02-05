package com.bugfender.okhttp3

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipFileRequestBody(val contents: Set<File>) : RequestBody() {
  companion object {
    private val FILE_MIME_TYPE = "application/zip".toMediaType()
  }

  override fun contentType() = FILE_MIME_TYPE

  override fun writeTo(sink: BufferedSink) {
    val zos = ZipOutputStream(sink.outputStream())
    contents.forEach {
      zos.putNextEntry(ZipEntry(it.name))
      Files.copy(it.toPath(), zos)
      zos.closeEntry()
    }
    zos.finish() // finishes writing without closing. okhttp3 docs do not state anything, but examples show no close statements and closing causes an exception
  }
}