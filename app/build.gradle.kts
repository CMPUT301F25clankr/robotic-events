// app/build.gradle.kts
plugins {
    id("com.android.application")   // ← no version here
    // id("org.jetbrains.kotlin.android") // only if you use Kotlin
}

android {
    namespace = "com.example.clankr_roboticevents"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.clankr_roboticevents"
        minSdk = 24
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
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_11
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_11
    }
    // If you add Kotlin later:
    // kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
