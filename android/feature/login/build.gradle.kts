plugins {
    alias(libs.plugins.seniorsafe.android.feature)
}

android { namespace = "com.seniorsafe.feature.login" }

dependencies {
    implementation(project(":core:datastore"))
}
