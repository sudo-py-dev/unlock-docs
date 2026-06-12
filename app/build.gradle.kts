import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
        }
        val androidMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {
                implementation("net.lingala.zip4j:zip4j:2.11.5")
                implementation("org.apache.poi:poi-ooxml:5.2.5")
                implementation(compose.uiTooling)
                implementation("androidx.activity:activity-compose:1.8.2")
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {
                implementation("net.lingala.zip4j:zip4j:2.11.5")
                implementation("org.apache.poi:poi-ooxml:5.2.5")
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
            }
        }
    }
}

android {
    namespace = "com.unlock.docs.unlockdocs"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.unlock.docs.unlockdocs"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    val keystorePropertiesFile = rootProject.file("local.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties["KEYSTORE_FILE"]?.let { rootProject.file(it as String) }
            storePassword = keystoreProperties["KEYSTORE_PASSWORD"] as String?
            keyAlias = keystoreProperties["KEY_ALIAS"] as String?
            keyPassword = keystoreProperties["KEY_PASSWORD"] as String?
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.unlock.docs.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "UnlockDocs"
            packageVersion = "1.0.0"
        }
        buildTypes.release.proguard {
            isEnabled = false
        }
    }
}
