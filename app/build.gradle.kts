plugins {
    id("com.android.application")
}

android {
    namespace = "kr.ac.lecture.mobilegame"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "kr.ac.lecture.mobilegame"
        minSdk {
            version = release(24)
        }
        targetSdk {
            version = release(37)
        }
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
        // Gradle 실행 JDK와 앱 코드의 Java 언어/바이트코드 수준은 별개입니다.
        // Java/Kotlin 코드는 17을 대상으로 컴파일된 뒤 Android Build Tools가 DEX로 변환합니다.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
