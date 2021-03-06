plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version Version.kotlin
}

fun getVersionCode(): Int {
    val information = file("information.properties")
    return if (information.canRead()) {
        val versionProps =
            `java.util`.Properties().apply { load(information.reader()) }
        var versionCode = versionProps["VERSION_CODE"].toString().toInt()
        val runTasks = gradle.startParameter.taskNames
        if ("assembleRelease" in runTasks) {
            versionProps["VERSION_CODE"] = (++versionCode).toString()
            versionProps.store(information.writer(), null)
        }
        versionCode
    } else -1
}

fun getInfo(info: String): String {
    val prop = `java.util`.Properties().apply { load(file("information.properties").reader()) }
    return prop[info].toString()
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file(getInfo("STORE_FILE"))
            storePassword = getInfo("STORE_PASSWORD")
            keyAlias = getInfo("DEBUG_ALIAS")
            keyPassword = getInfo("DEBUG_PASSWORD")
        }
        create("release") {
            storeFile = file(getInfo("STORE_FILE"))
            storePassword = getInfo("STORE_PASSWORD")
            keyAlias = getInfo("RELEASE_ALIAS")
            keyPassword = getInfo("RELEASE_PASSWORD")
        }
        create("canary") {
            storeFile = file(getInfo("STORE_FILE"))
            storePassword = getInfo("STORE_PASSWORD")
            keyAlias = getInfo("CANARY_ALIAS")
            keyPassword = getInfo("CANARY_PASSWORD")
        }
    }

    compileSdk = BuildVersion.compileSdk

    defaultConfig {
        applicationId = "zdz.imageURL"
        minSdk = BuildVersion.minSdk
        targetSdk = BuildVersion.targetSdk
        versionCode = getVersionCode()
        versionName = getInfo("VERSION_NAME")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "?????????"
            resValue("string", "provider", ".debug")
        }
        named("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
            resValue("string", "provider", "")
        }
        create("canary") {
            signingConfig = signingConfigs.getByName("canary")
            applicationIdSuffix = ".canary"
            versionNameSuffix = "??????"
            resValue("string", "provider", ".canary")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Version.compose
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.compose.ui:ui:${Version.ui}")
    implementation(Lib.material3)
    implementation(Lib.material)
    implementation("androidx.compose.ui:ui-tooling-preview:${Version.ui}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.navigation:navigation-compose:${Version.navigation}")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation(Lib.json)
    implementation("com.google.dagger:hilt-android:${Version.hilt}")
    kapt("com.google.dagger:hilt-android-compiler:${Version.hilt}")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("io.coil-kt:coil:${Version.coil}")
    implementation("io.coil-kt:coil-compose:${Version.coil}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutine}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutine}")
    implementation(project(":compose"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Version.ui}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Version.ui}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Version.ui}")

    implementation(Lib.preference)

//    implementation("com.github.ZIDOUZI:compose-component:v1.0.1")
}