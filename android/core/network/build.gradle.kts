plugins {
    alias(libs.plugins.seniorsafe.android.library)
    alias(libs.plugins.seniorsafe.android.hilt)
}

android {
    namespace = "com.seniorsafe.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val apiBaseUrl = providers
            .gradleProperty("SENIORSAFE_API_BASE_URL")
            .orElse("http://10.0.2.2:8000/")
            .get()
        buildConfigField("String", "SENIORSAFE_API_BASE_URL", "\"$apiBaseUrl\"")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
}
