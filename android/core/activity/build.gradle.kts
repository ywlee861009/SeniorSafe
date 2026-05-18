plugins {
    alias(libs.plugins.seniorsafe.android.library)
    alias(libs.plugins.seniorsafe.android.hilt)
}

android { namespace = "com.seniorsafe.core.activity" }

dependencies {
    implementation(project(":core:util"))
    implementation(project(":core:diagnostics"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
