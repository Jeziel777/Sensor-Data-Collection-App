@file:Suppress("UNUSED_EXPRESSION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

apply(plugin = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

secrets {
    propertiesFileName = "secret.properties" // Explicitly point to the file
}


android {
    namespace = "com.pav_analytics"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pav_analytics"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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


}

dependencies {

    implementation(libs.androidx.compose.ui.ui4)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx.v260)
    implementation(libs.androidx.activity.compose.v172)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.monitor)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Material Design 3
    implementation(libs.material3)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    // Android Studio Preview support
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)

    // UI Tests
    //androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
    //androidTestImplementation(libs.androidx.ui.test.junit4)

    // Optional - Integration with activities
    implementation(libs.androidx.activity.compose)
    // Optional - Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Optional - Integration with LiveData
    implementation(libs.androidx.runtime.livedata)
    // Optional - Integration with RxJava
    implementation(libs.androidx.runtime.rxjava2)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.mobile.ffmpeg.full.gpl)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.timber)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
    implementation(libs.coil.compose)

    //Libraries to send files to endpoint server
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.isoparser)

    //Library to merge colors
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh.v0265rc)


    implementation(libs.gson)

    //val cameraxVersion = "1.1.0-beta01"
    implementation(libs.androidx.camera.core.v110beta01)
    implementation(libs.androidx.camera.camera2.v110beta01)
    implementation(libs.androidx.camera.lifecycle.v110beta01)
    implementation(libs.androidx.camera.video.v110beta01)
    implementation(libs.androidx.camera.view.v110beta01)
    implementation(libs.androidx.camera.extensions.v110beta01)

    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    //firebase libraries
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.multidex)

    //Libraries to map implementation
    implementation (libs.play.services.location)
    implementation (libs.play.services.maps)
    implementation (libs.maps.compose)
    implementation (libs.maps.compose.utils)
    implementation (libs.maps.compose.widgets)
}

