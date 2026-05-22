plugins {
    alias(libs.plugins.seniorsafe.android.feature)
}

android { namespace = "com.seniorsafe.feature.guardian" }

dependencies {
    implementation(project(":core:datastore"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
