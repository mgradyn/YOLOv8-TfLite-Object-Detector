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
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven")}
    }
}

rootProject.name = "Palm Oil Counter"
include(":app", ":android-sdk-v5-uxsdk")
project(":android-sdk-v5-uxsdk").projectDir = File(rootDir, "app/libs/android-sdk-v5-uxsdk/")
