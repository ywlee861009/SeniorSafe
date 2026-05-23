plugins {
    alias(libs.plugins.anbu.android.library)
    alias(libs.plugins.anbu.android.hilt)
}

android { namespace = "com.kero.anbu.core.data" }

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(libs.coroutines.android)
}
