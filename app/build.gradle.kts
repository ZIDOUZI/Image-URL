val composeVersion = "1.2.0-alpha01"
val kotlinCoroutineVersion = "1.6.0"
val navVersion = "2.4.1"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    
    compileSdk = 31

    defaultConfig {
        applicationId = "zdz.bilicover"
        minSdk = 26
        targetSdk = 31
        versionCode = getVersionCode()
        versionName = getInfo("VERSION_NAME")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
        create("canary") {
            signingConfig = signingConfigs.getByName("canary")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha02")
    implementation("androidx.compose.material:material:1.1.0")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("io.coil-kt:coil:1.4.0")
    implementation("io.coil-kt:coil-compose:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutineVersion")
    implementation(files("libs\\URL.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
}