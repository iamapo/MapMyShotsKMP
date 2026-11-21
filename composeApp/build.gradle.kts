import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")
    
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
        androidMain.dependencies {
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.exifinterface)

        }
        commonMain.dependencies {
            api("io.github.qdsfdhvh:image-loader:1.10.0")
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.material.icons.extended)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.redred.mapmyshots.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "MapMyShots"
            packageVersion = "1.0.0"
        }
    }
}

android {
    namespace = "com.redred.mapmyshots"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.redred.mapmyshots"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.ui.tooling)
}

// Coverage for the shared logic layer: ./gradlew :composeApp:koverHtmlReport
// UI, DI, generated resources and platform code are excluded; they are verified on devices.
kover {
    reports {
        filters {
            excludes {
                packages(
                    "com.redred.mapmyshots.ui",
                    "com.redred.mapmyshots.di",
                    "mapmyshots.composeapp.generated.resources"
                )
                classes(
                    "com.redred.mapmyshots.AppKt*",
                    "com.redred.mapmyshots.MainKt*",
                    "com.redred.mapmyshots.ComposableSingletons*",
                    "com.redred.mapmyshots.MainActivity*",
                    "com.redred.mapmyshots.AndroidDeleteCoordinator*",
                    "com.redred.mapmyshots.platform.Android*",
                    "com.redred.mapmyshots.platform.Desktop*",
                    "com.redred.mapmyshots.platform.JpegGpsReader*",
                    "com.redred.mapmyshots.platform.Platform*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
    }
}
