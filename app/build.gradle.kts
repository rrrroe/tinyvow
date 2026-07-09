plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val tinyVowVersionName = providers.gradleProperty("TINYVOW_VERSION_NAME")
    .orElse("1.0.1")
    .get()
    .also {
        require(Regex("""\d+\.\d+\.\d+""").matches(it)) {
            "TINYVOW_VERSION_NAME must use MAJOR.MINOR.PATCH, for example 1.0.0."
        }
    }
val tinyVowVersionCode = providers.gradleProperty("TINYVOW_VERSION_CODE")
    .orElse("2")
    .get()
    .toIntOrNull()
    ?.also {
        require(it > 0) {
            "TINYVOW_VERSION_CODE must be a positive integer."
        }
    }
    ?: error("TINYVOW_VERSION_CODE must be a positive integer.")
val googleWebClientId = providers.gradleProperty("TINYVOW_GOOGLE_WEB_CLIENT_ID").orElse("").get()
val defaultActivationPublicKeyBase64 =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvgPRg7yuYzmB0zJOm818Eo0eRZKZmBKcZoNlmu2+IYORRDPcQjYMTNfN9P6VbXisyHFMK5AGUydFLBug+vhP5jeI6+DJjt1Dp5Szd/jysKljEGAQBu2ebIGWWhDwVDIdOZ1YHPQK3HkIRN9TQiwPpK9JdLJPuUEFbdXOVZgLTYITugjb5PoUdT6rX/HU5YQy+VzsgKWTUmdkRzQ1WBR6Oo90W2YqWbHu8ykbWI5vq+Bny13348C4yDSsnqDu6/SeBLR5jwn3WemUgpNCWbQAJ6dJ/BEs5MzDAofqEGw2BxivUrOvyHbyuCAP6H622Rv9XGzyvXt6Fx48afhRTTjV8QIDAQAB"
val activationPublicKeyFile = rootProject.layout.projectDirectory.file("tools/activation/public_key.x509").asFile
val activationPublicKeyBase64 = providers.gradleProperty("TINYVOW_ACTIVATION_PUBLIC_KEY_BASE64")
    .orElse(
        providers.provider {
            if (activationPublicKeyFile.isFile) {
                activationPublicKeyFile.readText().trim()
            } else {
                defaultActivationPublicKeyBase64
            }
        },
    )
    .get()
val chinaSigningPropertiesFile = rootProject.layout.projectDirectory
    .file("release-signing/tinyvow-cn-release.properties")
    .asFile
val chinaSigningProperties = mutableMapOf<String, String>().apply {
    if (chinaSigningPropertiesFile.isFile) {
        chinaSigningPropertiesFile.forEachLine { line ->
            val separator = line.indexOf('=')
            if (separator > 0) {
                val key = line.substring(0, separator).trim()
                val value = line.substring(separator + 1).trim()
                if (key.isNotEmpty()) {
                    put(key, value)
                }
            }
        }
    }
}
val hasChinaSigningConfig = listOf("storeFile", "storePassword", "keyAlias", "keyPassword").all {
    !chinaSigningProperties[it].isNullOrBlank()
}

android {
    namespace = "com.rrrrz.tinyvow"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.rrrrz.tinyvow"
        minSdk = 26
        targetSdk = 36
        versionCode = tinyVowVersionCode
        versionName = tinyVowVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${googleWebClientId.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
    }

    signingConfigs {
        if (hasChinaSigningConfig) {
            create("chinaShared") {
                storeFile = file(chinaSigningProperties.getValue("storeFile"))
                storePassword = chinaSigningProperties.getValue("storePassword")
                keyAlias = chinaSigningProperties.getValue("keyAlias")
                keyPassword = chinaSigningProperties.getValue("keyPassword")
            }
        }
    }

    flavorDimensions += "store"

    productFlavors {
        create("googlePlay") {
            dimension = "store"
            applicationId = "com.rrrrz.tinyvow"
            buildConfigField("String", "STORE_CHANNEL", "\"google_play\"")
            buildConfigField("Boolean", "ENABLE_GOOGLE_LOGIN", "true")
            buildConfigField("Boolean", "ENABLE_PLAY_BILLING", "true")
            buildConfigField("Boolean", "ENABLE_LOCAL_ACTIVATION", "false")
            buildConfigField("String", "ACTIVATION_PUBLIC_KEY_BASE64", "\"\"")
            resValue("string", "accessibility_settings_activity", "com.rrrrz.tinyvow.MainActivity")
        }

        create("china") {
            dimension = "store"
            applicationId = "com.rrrrz.tinyvow.cn"
            versionNameSuffix = "-cn"
            if (hasChinaSigningConfig) {
                signingConfig = signingConfigs.getByName("chinaShared")
            }
            buildConfigField("String", "STORE_CHANNEL", "\"china\"")
            buildConfigField("Boolean", "ENABLE_GOOGLE_LOGIN", "false")
            buildConfigField("Boolean", "ENABLE_PLAY_BILLING", "false")
            buildConfigField("Boolean", "ENABLE_LOCAL_ACTIVATION", "true")
            buildConfigField("String", "ACTIVATION_PUBLIC_KEY_BASE64", "\"$activationPublicKeyBase64\"")
            resValue("string", "accessibility_settings_activity", "com.rrrrz.tinyvow.cn.MainActivity")
        }
    }

    buildTypes {
        debug {
            if (hasChinaSigningConfig) {
                signingConfig = signingConfigs.getByName("chinaShared")
            }
        }

        release {
            isMinifyEnabled = true
            if (hasChinaSigningConfig) {
                signingConfig = signingConfigs.getByName("chinaShared")
            }
            // AppText resolves many localized strings dynamically by key. Resource shrinking
            // can remove those strings because they are not all referenced as R.string.*.
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

configurations.configureEach {
    resolutionStrategy.force("androidx.activity:activity:1.8.0")
}

tasks.register("assembleDefaultDebug") {
    group = "build"
    description = "Builds the default day-to-day debug variant, currently chinaDebug."
    dependsOn("assembleChinaDebug")
}

tasks.register("installDefaultDebug") {
    group = "install"
    description = "Installs the default day-to-day debug variant, currently chinaDebug."
    dependsOn("installChinaDebug")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.google.play.billing.ktx)
    implementation(libs.androidx.palette)
    implementation(libs.material.color.utilities)
    implementation(libs.androidx.health.connect.client)
    ksp(libs.androidx.room.compiler)
}
