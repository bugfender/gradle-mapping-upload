package com.bugfender

import org.gradle.api.provider.Property

abstract class UploadMappingPluginExtension {
    abstract val symbolicationURL: Property<String>
    abstract val symbolicationToken: Property<String>

    fun symbolicationURL(symbolicationURL: String) {
        this.symbolicationURL.set(symbolicationURL)
    }

    fun symbolicationToken(symbolicationToken: String) {
        this.symbolicationToken.set(symbolicationToken)
    }

    init {
        this.symbolicationURL.convention("https://dashboard.bugfender.com")
    }
}