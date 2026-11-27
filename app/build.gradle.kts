import java.util.Properties

plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sonj.silosmonitoreo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sonj.silosmonitoreo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val props = Properties()
            val archivo = rootProject.file("keystore.properties")
            if (archivo.exists()) props.load(archivo.inputStream())
            storeFile = file(props.getProperty("storeFile", "../keystore/silos-release.jks"))
            storePassword = props.getProperty("storePassword")
            keyAlias = props.getProperty("keyAlias")
            keyPassword = props.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
}

// ---------- Cobertura de pruebas unitarias (JaCoCo) ----------
// Mide solo la logica (data, ViewModel, modelo). Quedan fuera pantallas Compose, tema y SQLite.
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val excluidos = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/ui/screens/**", "**/ui/theme/**", "**/ui/components/**",
        "**/data/db/**", "**/MainActivity*", "**/navigation/**",
        "**/data/DatosPrueba*", "**/data/PreferenciasManager*",
        "**/data/api/ApiClient*", "**/data/api/ApiConfig*",
        "**/data/api/*Request*", "**/data/api/*Response*", "**/data/api/ClimaActual*",
        "**/data/authapi/AuthApiClient*", "**/data/authapi/*Request*", "**/data/authapi/*Response*",
        "**/model/Granja*", "**/model/Movimiento*", "**/model/Alerta*",
        "**/model/MensajeChat*", "**/model/TamanoFuente*", "**/model/EstiloTipografia*",
        "**/*ComposableSingletons*", "**/*\$Lambda*", "**/*\$\$inlined*"
    )
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude(excluidos)
        },
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(excluidos)
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

android.buildTypes.getByName("debug").enableUnitTestCoverage = true