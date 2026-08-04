# 1.3.0
* New: Support Android Gradle Plugin 9, which removes the `AppExtension` DSL the plugin relied on
* Update: Migrate to the `AndroidComponentsExtension` variant API. This raises the minimum supported AGP to 7.4
* Update: The upload task is now compatible with the Gradle configuration cache

# 1.2.0
* Update: Zip mapping file for faster upload

# 1.1.2
* Update: Increase upload timeout to 10 minutes

# 1.1.1
* Update: Increase upload timeout to 30 seconds

# 1.1.0
* Update: Plug into both `assemble` and `bundle` tasks

# 1.0.2
* Update: Do not abort build if mapping file upload fails

# 1.0.1
* Update: Use Gradle Plugin Publishing Plugin to upload to Gradle Plugin Portal

# 1.0.0
* Initial version
