object BuildPlugins {
    object Versions {
        const val gradleVersion = "7.0.4"
        const val kotlinVersion = "1.6.0"
    }

    const val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradleVersion}"
    const val kotlinGradlePlugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"

    const val androidApplication = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinParcelize = "kotlin-parcelize"
    const val mavenPublish = "maven-publish"
}

object Libs {
    object Material {
        const val material = "com.google.android.material:material:1.4.0"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.4.0"
        const val coreKtx = "androidx.core:core-ktx:1.7.0"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:1.4.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.2"
    }

    object PlayServices {
        const val maps = "com.google.android.gms:play-services-maps:18.0.2"
    }

    object Maps {
        const val places = "com.google.android.libraries.places:places:2.5.0"
    }

    object Testing {
        const val junit = "junit:junit:4.13.2"
        const val espresso = "androidx.test.espresso:espresso-core:3.4.0"
    }
}