plugins {
    alias(libs.plugins.seniorsafe.android.library)
}

android { namespace = "com.seniorsafe.core.model" }

dependencies {
    // @SerializedName 어노테이션 사용
    implementation(libs.gson)
}
