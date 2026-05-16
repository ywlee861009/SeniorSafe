plugins {
    alias(libs.plugins.seniorsafe.android.library)
    alias(libs.plugins.seniorsafe.android.hilt)
}

android { namespace = "com.seniorsafe.core.falldetection" }

dependencies {
    implementation(project(":core:diagnostics"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)
}
