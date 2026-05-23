plugins {
    alias(libs.plugins.anbu.android.feature)
}

android { namespace = "com.kero.anbu.feature.senior" }

dependencies {
    implementation(project(":core:activity"))
    implementation(project(":core:datastore"))
}
