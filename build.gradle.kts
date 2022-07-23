// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.4.0-alpha08" apply false
    id("com.android.library") version "7.4.0-alpha08" apply false
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