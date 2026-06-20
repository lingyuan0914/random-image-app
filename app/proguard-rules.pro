# Keep all app classes
-keep class com.randomimage.** { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.** { *; }
-keep @com.squareup.moshi.Json class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewWithFragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$ActivityEntryPoint { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory$DefaultActivityEntryPoint { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Coil
-keep class coil.** { *; }
-keep class coil.compose.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Google Error Prone Annotations
-dontwarn com.google.errorprone.annotations.**

# Timber
-keep class timber.log.Timber { *; }

# EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# Keep UI screens and components
-keep class com.randomimage.ui.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Hilt generated
-keep class * extends dagger.hilt.android.internal.** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **Hilt_** { *; }
