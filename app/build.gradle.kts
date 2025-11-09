plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.aktarjabed.androphoshop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aktarjabed.androphoshop"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "2.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    // Jetpack Compose Core
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt + Navigation
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Image Loading (Coil)
    implementation(libs.coil.compose)

    // MLKit (for background removal / relighting)
    implementation(libs.mlkit.segmentation.selfie)
    implementation(libs.mlkit.face.detection)

    // DataStore & Room (optional for projects)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Testing (optional)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
}