import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(BuildPlugins.gradlePlugin)
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://maven.google.com")
        maven(url = "https://jitpack.io")
    }
}

tasks.register("clean").configure {
    delete("build")
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

fun String.isSameVersionNumber(otherVersion: String): Boolean {
    if (contains("-") && otherVersion.contains("-")) {
        return split("-")[0] == otherVersion.split("-")[0]
    }
    return false
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.isNonStable() && currentVersion.isSameVersionNumber(candidate.version).not()
    }
}

plugins {
    idea
    /**
     * Should be remove when android studio highlight outdated dependencies
     * usage ./gradlew dependencyUpdates
     */
    id("com.github.ben-manes.versions") version "0.41.0"
}