plugins {
    alias(libs.plugins.seniorsafe.android.library)
    alias(libs.plugins.seniorsafe.android.compose)
    alias(libs.plugins.seniorsafe.android.hilt)
}

android { namespace = "com.seniorsafe.feature.mvp" }

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:diagnostics"))
    implementation(project(":core:activity"))
    implementation(project(":core:fall-detection"))

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.android)
}
