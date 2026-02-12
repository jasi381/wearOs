plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.capabiltiesa"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.capabiltiesa"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.serialization.json)

    // Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Image loading
    implementation(libs.coil.compose)

    // Mobile-specific dependencies
    implementation(libs.bundles.mobile)

    // Networking
    implementation(libs.bundles.networking)

    // Debug tools
    debugImplementation(libs.bundles.debug)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.testing)
}