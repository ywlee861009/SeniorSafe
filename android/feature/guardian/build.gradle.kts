plugins {
    alias(libs.plugins.anbu.android.feature)
}

android { namespace = "com.kero.anbu.feature.guardian" }

dependencies {
    implementation(project(":core:datastore"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
}
