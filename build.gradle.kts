// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.0-alpha10" apply false
    id("com.android.library") version "8.0.0-alpha10" apply false
    kotlin("android") version Version.kotlin apply false
}

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Version.hilt}")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}