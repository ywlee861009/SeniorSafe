plugins {
    alias(libs.plugins.anbu.android.library)
    alias(libs.plugins.anbu.android.compose)
    alias(libs.plugins.anbu.android.hilt)
}

android { namespace = "com.kero.anbu.feature.mvp" }

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:diagnostics"))
    implementation(project(":core:activity"))

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.android)
}
