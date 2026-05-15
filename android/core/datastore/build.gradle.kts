plugins {
    alias(libs.plugins.seniorsafe.android.library)
    alias(libs.plugins.seniorsafe.android.hilt)
}

android { namespace = "com.seniorsafe.core.datastore" }

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
}
