plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

fun getVersionCode(): Int {
    val information = file("information.properties")
    return if (information.canRead()) {
        val versionProp =
            `java.util`.Properties().apply { load(information.reader()) }
        var versionCode = versionProp["VERSION_CODE"].toString().toInt()
        val runTasks = gradle.startParameter.taskNames
        if ("assembleRelease" in runTasks) {
            versionProp["VERSION_CODE"] = (++versionCode).toString()
            versionProp.store(information.writer(), null)
        }
        versionCode
    } else -1
}

fun getInfo(info: String): String {
    val prop = `java.util`.Properties().apply { load(file("information.properties").reader()) }
    return prop[info].toString()
}

android {
    namespace = "zdz.imageURL"
    compileSdk = 34
    
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
    }
    
    defaultConfig {
        applicationId = "zdz.imageURL"
        minSdk = 24
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getInfo("VERSION_NAME")
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
    }
    
    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "屯屯屯"
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
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "VERSION", "\"${getInfo("VERSION")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions.jvmTarget = "17"
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

dependencies {
    
    implementation(libs.bundles.core)
    implementation(libs.android.material)
    api(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.accompanist)
    implementation(libs.activity.compose)
    implementation(libs.hilt)
    implementation(libs.bundles.navigation)
    implementation(libs.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.ktor)
    
    implementation(libs.bundles.zdz.preferences)
    implementation(libs.zdz.compose.ex)
    
    ksp(libs.bundles.hilt.kapt)
    
    testImplementation(libs.bundles.test)
    kspTest(libs.bundles.hilt.kapt)
    
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    kspAndroidTest(libs.bundles.hilt.kapt)
    
    debugImplementation(libs.bundles.compose.debug)
    kspDebug(libs.bundles.hilt.kapt)
}

hilt.enableAggregatingTask = true
