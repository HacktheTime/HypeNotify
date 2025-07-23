pluginManagement {
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
        maven { url = uri("https://repo.nea.moe/releases") }
    }
}

rootProject.name = "HypeNotify"
include(":app")
include(":annotations") // The new module for annotations
include(":processor") // The annotation processor module