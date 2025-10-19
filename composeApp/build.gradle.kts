import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        all { languageSettings{
            optIn("kotlin.time.ExperimentalTime")
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn("kotlin.ExperimentalStdlibApi")
        } }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            
            // Core Libraries
            implementation(libs.timber)
            implementation(libs.accompanist.permissions)
            implementation(libs.androidx.security.crypto)
            
            // AndroidX Startup para resolver NoClassDefFoundError
            implementation(libs.androidx.startup.runtime)
            // Ktor Android
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
            
            // CameraX for QR scanning
            implementation(libs.camerax.core)
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.view)
            implementation(libs.camerax.mlkit.vision)
            
            // ML Kit for QR code detection
            implementation(libs.mlkit.barcode.scanning)

            // Koin for Android
            implementation(libs.koin.android)
            
            // AndroidX Startup - explicitly add to resolve version conflicts
            implementation(libs.androidx.startup.runtime)
        }
        
        commonMain.dependencies {
            // Kotlinx IO for image processing
            implementation(libs.kotlinx.io.core)
        }
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Material Icons - Usando la versión latest por defecto
            implementation(compose.materialIconsExtended)

            // Lottie for animations
            implementation(libs.lottie.compose)

            
            // Kotlinx
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.androidx.compose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

android {
    compileSdk = 36
    namespace = "com.yapechamo.composeapp"
    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }
}
