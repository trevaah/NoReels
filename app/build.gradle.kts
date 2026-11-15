plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.noreels"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.noreels"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "3.0"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("noreels.keystore")
            storePassword = "noreels123"
            keyAlias = "noreels_key"
            keyPassword = "noreels123"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
