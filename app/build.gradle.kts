plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "kr.ac.lecture.mobilegame"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.ac.lecture.mobilegame"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Kotlin에는 #if 전처리기가 없으므로 빌드 시 생성되는 static final 상수를 사용합니다.
        buildConfigField("boolean", "SAMPLE_BLOCK", "true")
        buildConfigField("boolean", "DEBUG_BLOCK", "true")
    }

    buildTypes {
        release {
            // Release 빌드에서는 debugPrint()의 분기를 비활성화합니다.
            buildConfigField("boolean", "DEBUG_BLOCK", "false")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
