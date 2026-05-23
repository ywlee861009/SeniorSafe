plugins {
    alias(libs.plugins.anbu.android.library)
}

android { namespace = "com.kero.anbu.core.model" }

dependencies {
    // @SerializedName 어노테이션 사용
    implementation(libs.gson)
}
