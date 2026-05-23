plugins {
    alias(libs.plugins.anbu.android.library)
    alias(libs.plugins.anbu.android.hilt)
}

android { namespace = "com.kero.anbu.core.datastore" }

dependencies {
    implementation(project(":core:model"))
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
}
