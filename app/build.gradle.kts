plugins {
    id("com.android.application")
    kotlin("android")
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
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "屯屯屯"
            resValue("string", "provider", ".debug")
            buildConfigField("String", "VERSION", "\"0.0.0\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
            resValue("string", "provider", "")
            buildConfigField("String", "VERSION", "\"${getInfo("VERSION")}\"")
        }
        create("canary") {
            signingConfig = signingConfigs.getByName("canary")
            isDebuggable = true
            applicationIdSuffix = ".canary"
            versionNameSuffix = "奇异鸟"
            resValue("string", "provider", ".canary")
            buildConfigField("String", "VERSION", "\"${getInfo("VERSION")}\"")
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
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Version.compose
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "zdz.imageURL"
}

dependencies {

    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui:${Version.ui}")
    implementation(Lib.material3)
    implementation(Lib.material)
    implementation("androidx.compose.ui:ui-tooling-preview:${Version.ui}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.navigation:navigation-compose:${Version.navigation}")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("io.ktor:ktor-client-core:${Version.ktor}")
    implementation("io.ktor:ktor-client-cio-jvm:${Version.ktor}")
    implementation("io.ktor:ktor-client-encoding:${Version.ktor}")
    implementation(Lib.json)
    implementation("com.google.dagger:hilt-android:${Version.hilt}")
    implementation(files("..\\lib\\URL.jar"))
    implementation(files("..\\lib\\encode.jar"))
    kapt("com.google.dagger:hilt-android-compiler:${Version.hilt}")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("io.coil-kt:coil:${Version.coil}")
    implementation("io.coil-kt:coil-compose:${Version.coil}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutine}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutine}")
    implementation(project(":compose"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Version.ui}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Version.ui}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Version.ui}")

    implementation(Lib.preference)

//    implementation("com.github.ZIDOUZI:compose-component:v1.0.1")
}