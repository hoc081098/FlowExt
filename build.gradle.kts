import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.net.URL

plugins {
  kotlin("multiplatform") version "1.6.0"
  id("com.diffplug.spotless") version "6.0.0"
  id("maven-publish")
  id("com.vanniktech.maven.publish") version "0.18.0"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.8.0"
  id("org.jetbrains.dokka") version "1.6.0"
}

group = "io.github.hoc081098"
version = "0.2.0-SNAPSHOT"

repositories {
  google()
  mavenCentral()
  gradlePluginPortal()
}

val kotlinCoroutinesVersion = "1.6.0-RC"
val ktlintVersion = "0.43.0"

kotlin {
  explicitApi()

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = "1.8"
    }
  }
  js(BOTH) {
    compilations.all {
      kotlinOptions {
        sourceMap = true
        moduleKind = "umd"
        metaInfo = true
      }
    }
    browser {
      testTask {
        useMocha {
          timeout = "5s"
        }
      }
    }
    nodejs {
      testTask {
        useMocha {
          timeout = "5s"
        }
      }
    }
  }

  iosArm64()
  iosArm32()
  iosX64()

  macosX64()
  mingwX64()
  linuxX64()

  tvosX64()
  tvosArm64()

  watchosArm32()
  watchosArm64()
  watchosX64()
  watchosX86()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("org.jetbrains.kotlinx:atomicfu:0.17.0")
      }
    }
    val jvmMain by getting {
      dependsOn(commonMain)
    }
    val jvmTest by getting {
      dependsOn(commonTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }
    val jsMain by getting {
      dependsOn(commonMain)
    }
    val jsTest by getting {
      dependencies {
        implementation(kotlin("test-js"))
      }
    }

    val nativeMain by creating {
      dependsOn(commonMain)
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion") {
          version {
            strictly(kotlinCoroutinesVersion)
          }
        }
      }
    }
    val nativeTest by creating {
      dependsOn(commonTest)
    }

    val appleTargets = listOf(
      "iosX64",
      "iosArm64",
      "iosArm32",
      "macosX64",
      "tvosArm64",
      "tvosX64",
      "watchosArm32",
      "watchosArm64",
      "watchosX86",
      "watchosX64",
    )

    (appleTargets + listOf("mingwX64", "linuxX64")).forEach {
      getByName("${it}Main") {
        dependsOn(nativeMain)
      }
      getByName("${it}Test") {
        dependsOn(nativeTest)
      }
    }
  }

  // enable running ios tests on a background thread as well
  // configuration copied from: https://github.com/square/okio/pull/929
  targets.withType<KotlinNativeTargetWithTests<*>>().all {
    binaries {
      // Configure a separate test where code runs in background
      test("background", setOf(NativeBuildType.DEBUG)) {
        freeCompilerArgs = freeCompilerArgs + "-trw"
      }
    }
    testRuns {
      val background by creating {
        setExecutionSourceFrom(binaries.getTest("background", NativeBuildType.DEBUG))
      }
    }
  }
}

spotless {
  kotlin {
    target("**/*.kt")

    ktlint(ktlintVersion).userData(
      mapOf(
        // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8"
      )
    )

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }

  kotlinGradle {
    target("**/*.kts")

    ktlint(ktlintVersion).userData(
      mapOf(
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8"
      )
    )

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
}

allprojects {
  plugins.withId("com.vanniktech.maven.publish") {
    mavenPublish {
      sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
    }
  }
}

tasks.withType<Test> {
  testLogging {
    showExceptions = true
    showCauses = true
    showStackTraces = true
    showStandardStreams = true
    events = setOf(
      TestLogEvent.PASSED,
      TestLogEvent.FAILED,
      TestLogEvent.SKIPPED,
      TestLogEvent.STANDARD_OUT,
      TestLogEvent.STANDARD_ERROR
    )
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets {
    configureEach {
      externalDocumentationLink {
        url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
      }
    }
  }
}
