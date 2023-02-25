// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

plugins {
    // Define the versions of external plugins that may be used by subprojects.

    // Specify that a module is an application.
    // https://developer.android.com/studio/releases/gradle-plugin
    id ("com.android.application") version "7.4.1" apply false

    // Specify that a module is a library.
    // https://developer.android.com/studio/projects/android-library
    id ("com.android.library") version "7.4.1" apply false

    // Kotlin Android plugin.
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android
    id ("org.jetbrains.kotlin.android") version "1.8.10" apply false
}

tasks {
    create("clean", Delete::class.java)
    {
        delete(rootProject.buildDir)
    }
}