plugins {
    alias(libs.plugins.anbu.android.library)
    alias(libs.plugins.anbu.android.hilt)
}

android { namespace = "com.kero.anbu.core.diagnostics" }

dependencies {
    implementation(project(":core:util"))
    implementation(libs.coroutines.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
