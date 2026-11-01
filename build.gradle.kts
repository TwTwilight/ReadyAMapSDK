import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    id("com.android.library") version "9.5.0"
    id("maven-publish")
}

group = "com.github.TwTwilight"
version = "1.0.2"

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

val sdkJar = layout.projectDirectory.file("libs/amap-3dmap.jar").asFile
val aarDir = layout.buildDirectory.dir("outputs/aar")

fun mergeSdkIntoAar(aar: File, jar: File) {
    val tmp = File(aar.parentFile, aar.name + ".tmp")
    ZipFile(aar).use { src ->
        ZipOutputStream(tmp.outputStream().buffered()).use { out ->
            for (e in src.entries()) {
                if (e.name == "classes.jar" || e.name == "libs/" || e.name == "libs/amap-3dmap.jar") continue
                out.putNextEntry(ZipEntry(e.name).apply { time = e.time })
                src.getInputStream(e).use { it.copyTo(out) }
                out.closeEntry()
            }
            out.putNextEntry(ZipEntry("classes.jar").apply { time = 0L })
            jar.inputStream().buffered().use { it.copyTo(out) }
            out.closeEntry()
        }
    }
    tmp.copyTo(aar, overwrite = true)
    tmp.delete()
}

tasks.matching { it.name == "bundleReleaseAar" }.configureEach {
    doLast {
        val dir = aarDir.get().asFile
        val aar = dir.listFiles { f -> f.extension == "aar" }?.firstOrNull()
            ?: throw GradleException("release aar not found in $dir")
        mergeSdkIntoAar(aar, sdkJar)
    }
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
