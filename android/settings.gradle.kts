pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SeniorSafe"

include(":app")

// Core
include(":core:model")
include(":core:network")
include(":core:datastore")
include(":core:data")
include(":core:diagnostics")
include(":core:util")
include(":core:activity")
include(":core:fall-detection")
include(":core:ui")

// Feature
include(":feature:login")
include(":feature:senior")
include(":feature:guardian")
include(":feature:mvp")
