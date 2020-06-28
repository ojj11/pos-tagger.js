plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("multiplatform") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.72"
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    jcenter()
}

kotlin {

    js {
        nodejs()
    }
    jvm()

    sourceSets {

        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0")
            }
        }

        get("jsMain").dependencies {
            implementation(kotlin("stdlib-js"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor-js:0.20.0")

        }

        get("jvmMain").dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:0.20.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
        }
    }

}

val runJvm by tasks.creating(JavaExec::class) {
    group = "application"
    main = "edu.stanford.nlp.TaggerDemo"
    kotlin {
        val main = targets["jvm"].compilations["main"]
        dependsOn(main.compileAllTaskName)
        classpath(
                { main.output.allOutputs.files },
                { configurations["jvmRuntimeClasspath"] }
        )
    }
    ///disable app icon on macOS
    systemProperty("java.awt.headless", "true")
}
