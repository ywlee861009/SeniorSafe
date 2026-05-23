plugins {
    alias(libs.plugins.anbu.android.feature)
}

android { namespace = "com.kero.anbu.feature.login" }

dependencies {
    implementation(project(":core:datastore"))
}
