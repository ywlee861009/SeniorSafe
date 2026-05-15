dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    // 루트 프로젝트의 libs.versions.toml 을 공유
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
