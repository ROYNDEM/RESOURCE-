pluginManagement {
    repositories {
        // 🔑 Order matters — keep Gradle Plugin Portal first for Kotlin plugins
        gradlePluginPortal()
        google()
        mavenCentral()
        // Optional: fallback mirror
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")

        }
    }
}

rootProject.name = "NGONG"
include(":app")
