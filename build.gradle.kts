import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    id("com.android.library") version "9.5.0"
    id("maven-publish")
}

group = "com.github.TwTwilight"
version = "1.0.4"

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
val releaseAarDir = layout.buildDirectory.dir("outputs/aar")
val fatAarFile = layout.buildDirectory.file("outputs/fat/ReadyAMapSDK-release.aar")

fun mergeSdkIntoAar(target: File, jar: File) {
    val tmp = File(target.parentFile, target.name + ".tmp")
    ZipFile(target).use { src ->
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
    tmp.copyTo(target, overwrite = true)
    tmp.delete()
}

val fatAar by tasks.registering {
    group = "build"
    description = "Bundle libs/amap-3dmap.jar into the AAR classes.jar"
    dependsOn("assembleRelease")
    mustRunAfter("bundleDebugAar")
    inputs.file(sdkJar)
    outputs.file(fatAarFile)
    outputs.upToDateWhen { false }
    doLast {
        val src = releaseAarDir.get().asFile
            .listFiles { f -> f.extension == "aar" }
            ?.firstOrNull()
            ?: throw GradleException("release aar not found")
        val out = fatAarFile.get().asFile
        out.parentFile.mkdirs()
        src.copyTo(out, overwrite = true)
        mergeSdkIntoAar(out, sdkJar)
        println("fat aar: ${out.absolutePath} (${out.length()} bytes)")
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            artifact(fatAar) {
                extension = "aar"
                builtBy(fatAar)
            }
        }
    }
}
