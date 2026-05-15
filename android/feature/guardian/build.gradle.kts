plugins {
    alias(libs.plugins.seniorsafe.android.feature)
}

android { namespace = "com.seniorsafe.feature.guardian" }

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
