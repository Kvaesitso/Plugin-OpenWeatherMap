pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/MM2-0/Kvaesitso")
            credentials {
                username = extra.properties["gpr.user"] as String? ?: System.getenv("USERNAME")
                password = extra.properties["gpr.key"] as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

rootProject.name = "Kvaesitso OpenWeatherMap Plugin"
include(":app")
 