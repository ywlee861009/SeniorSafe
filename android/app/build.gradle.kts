plugins {
    alias(libs.plugins.anbu.android.application)
    alias(libs.plugins.anbu.android.compose)
    alias(libs.plugins.anbu.android.hilt)
    // google-services: Firebase 콘솔에서 google-services.json 발급 후 주석 해제
    // alias(libs.plugins.google.services)
}

android {
    namespace = "com.kero.anbu"
    defaultConfig {
        applicationId = "com.kero.anbu"
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Feature modules
    implementation(project(":feature:login"))
    implementation(project(":feature:senior"))
    implementation(project(":feature:guardian"))
    implementation(project(":feature:mvp"))

    // Core
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:datastore"))
    implementation(project(":core:data"))
    implementation(project(":core:activity"))

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
