// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.3.0-alpha03" apply false
    id("com.android.library") version "7.3.0-alpha03" apply false
    kotlin("android") version "1.6.10" apply false
}

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
 }