package com.bugfender

import org.gradle.api.provider.Property

abstract class UploadMappingPluginExtension {
    abstract val apiURL: Property<String>
    abstract val apiKey: Property<String>

    fun apiURL(apiURL: String) {
        this.apiURL.set(apiURL)
    }

    fun apiKey(apiKey: String) {
        this.apiKey.set(apiKey)
    }

    init {
        this.apiURL.convention("https://api.bugfender.com")
    }
}