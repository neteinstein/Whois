# Compose, Koin, coroutines and multiplatform-settings all ship their own consumer ProGuard
# rules inside their AARs, so this file only needs project-specific exceptions - none required
# yet, since the app has no reflection-based serialization, custom Parcelables, or JNI/WebView
# JS bridges that R8 could strip incorrectly.

# Keep line numbers for readable stack traces from crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
