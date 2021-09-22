Bugfender Android Gradle Plugin
===============================

Used to automatically upload ProGuard mapping files to [Bugfender](https://bugfender.com).

## Usage

Add the plugin to your app `build.gradle` file and configure it with the Symbols Upload Token, obtained from your Bugfender dashboard.

```
plugins {
    id "com.bugfender.upload-mapping" version "1.0.0"
}

bugfender {
    apiKey "<your_token_here>"
}
```

After that, every assembled app bundle will automatically send mapping file to Bugfender.
