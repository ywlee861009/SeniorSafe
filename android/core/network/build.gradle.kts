plugins {
    alias(libs.plugins.anbu.android.library)
    alias(libs.plugins.anbu.android.hilt)
}

android {
    namespace = "com.kero.anbu.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val apiBaseUrl = providers
            .gradleProperty("ANBU_API_BASE_URL")
            .orElse("http://10.0.2.2:8000/")
            .get()
        buildConfigField("String", "ANBU_API_BASE_URL", "\"$apiBaseUrl\"")
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
