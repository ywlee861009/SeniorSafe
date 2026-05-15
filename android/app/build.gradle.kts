plugins {
    alias(libs.plugins.seniorsafe.android.application)
    alias(libs.plugins.seniorsafe.android.compose)
    alias(libs.plugins.seniorsafe.android.hilt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.seniorsafe"
    defaultConfig {
        applicationId = "com.seniorsafe"
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

    // Core
    implementation(project(":core:ui"))
    implementation(project(":core:datastore"))

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
