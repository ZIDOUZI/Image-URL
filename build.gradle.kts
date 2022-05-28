// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.4.0-alpha02" apply false
    id("com.android.library") version "7.4.0-alpha02" apply false
    kotlin("android") version "1.6.21" apply false
}

allprojects {
    extra["kotlin_version"] = "1.6.21"
    extra["compiler_version"] = "1.2.0-beta02"
    extra["compose_version"] = "1.2.0-beta12"
    extra["kotlin_coroutines_version"] = "1.6.1"
    extra["nav_version"] = "2.4.1"
    extra["hilt_version"] = "2.42"
}

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}