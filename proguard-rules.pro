# Add your ProGuard rules here

# Hilt
-keep class com.androphoshop.Hilt_* { *; }
-dontwarn dagger.hilt.internal.processedrootsentinel.codegen.*
-keepclassmembers class ** {
    @dagger.hilt.android.internal.managers.ViewComponentManager.ViewWithFragmentComponent *;
}
-keepnames class * {
    @dagger.hilt.android.lifecycle.HiltViewModel
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.support.** { *; }

# LiteRT (for 2025)
-keep class com.google.ai.edge.litert.** { *; }

# Image processing libraries
-keep class com.github.mukeshsolanki.photofilter.** { *; }
-keep class ja.burhanrashid52.photoeditor.** { *; }

# Coil
-keepclassmembers class * {
    @coil.annotation.ExperimentalCoilApi *;
}

# Keep model classes (data classes)
-keep class com.androphoshop.**.models.** { *; }

# Keep composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {
    * SUPPORTED_DISPATCHERS;
}