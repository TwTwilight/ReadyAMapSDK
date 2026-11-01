plugins {
    id("com.android.library") version "9.5.0"
    id("maven-publish")
}

group = "com.github.TwTwilight"
version = "1.0.0"

android {
    namespace = "com.twilight.amapsdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    publishing {
        singleVariant("release")
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources.excludes += setOf("META-INF/DEPENDENCIES")
        jniLibs.keepDebugSymbols += "**/*.so"
    }
}

dependencies {
    api(files("libs/amap-3dmap.jar"))
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
            }
        }
    }
}
