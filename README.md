Bugfender Android Gradle Plugin
===============================

Used to automatically upload ProGuard mapping files to [Bugfender](https://bugfender.com).

## Usage

Add the plugin to your *app* `build.gradle` file and configure it with the Symbols Upload Token, obtained from your Bugfender dashboard.

After that, every assembled app bundle will automatically send mapping file to Bugfender.

### Kotlin Gradle configuration (`app/build.gradle.kts` file)

Add the Bugfender plugin to the `plugins` section and create a new `bugfender` section, like this:

```kotlin
plugins {
    id("com.android.application")
    // you may have other plugins here
    id("com.bugfender.upload-mapping") version "1.2.0"
}

bugfender {
    symbolicationToken("<your_token_here>")
}
```

### Groovy Gradle configuration (`app/build.gradle` file)

Add the Bugfender plugin to the `plugins` section and create a new `bugfender` section, like this:

```groovy
plugins {
    id "com.android.application"
    // you may have other plugins here
    id "com.bugfender.upload-mapping" version "1.2.0"
}

bugfender {
    symbolicationToken "<your_token_here>"
}
```

Note: in older project configurations, it's possible this file is not under an `app` directory.

### Bugfender On-Premises

If you're using a Bugfender instance other than `dashboard.bugfender.com`, you will need to specify the URL of your instance:

In `app/build.gradle.kts` (Kotlin):

```kotlin
bugfender {
    symbolicationToken("<your_token_here>")
    symbolicationURL("https://bugfender.yourcompany.com/")
}
```

Or, if you have a `app/build.gradle` (Groovy):

```groovy
bugfender {
    symbolicationToken "<your_token_here>"
    symbolicationURL "https://bugfender.yourcompany.com/"
}
```

### Troubleshooting

#### Error message `org.gradle.api.plugins.UnknownPluginException: Plugin [id: 'com.bugfender.upload-mapping', version: 'XXX'] was not found...`

You may need to configure the plugin repositories in your `settings.gradle.kts` or `settings.gradle` file.
Add `gradlePluginPortal()` to the `pluginManagement` > `repositories` section, like this:

```kotlin
pluginManagement {
    repositories {
        // you may have other repos here
        gradlePluginPortal()
    }
}
```


# Contributing

To use a local version that's not published to the maven central.

* Publish it to a local maven repository with `gradle publishToMavenLocal` task.
* In the test project, add `mavenLocal` to repositories in `settings.gradle`:
```groovy
pluginManagement {
    repositories {
        mavenLocal()
        (...)
    }
}
```
* Configure the plugin as described in `Usage`.
