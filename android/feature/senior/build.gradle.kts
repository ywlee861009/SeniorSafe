plugins {
    alias(libs.plugins.seniorsafe.android.feature)
}

android { namespace = "com.seniorsafe.feature.senior" }

dependencies {
    implementation(project(":core:datastore"))
}
