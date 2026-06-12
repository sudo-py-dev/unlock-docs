# ProGuard rules for Compose and Coroutines
-keep class androidx.compose.ui.tooling.preview.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Preserve standard Kotlin properties
-keepclassmembers class * {
    @org.jetbrains.annotations.NotNull <fields>;
    @org.jetbrains.annotations.Nullable <fields>;
}

# Preserve data classes to prevent R8 from stripping away JSON serialization properties
-keep class * extends java.lang.Record { *; }

# Obfuscation safety for native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Prevent warnings from OkHttp / Retrofit if added later
-dontwarn okio.**
-dontwarn javax.annotation.**
