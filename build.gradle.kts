import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.net.URL

plugins {
  kotlin("multiplatform") version "1.6.21"
  id("com.diffplug.spotless") version "6.7.2"
  id("maven-publish")
  id("com.vanniktech.maven.publish") version "0.20.0"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.10.0"
  id("org.jetbrains.dokka") version "1.6.21"
  id("org.jetbrains.kotlinx.kover") version "0.5.1"
}

val coroutinesVersion = "1.6.1"
val ktlintVersion = "0.44.0"

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

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
        useMocha()
      }
    }
    nodejs {
      testTask {
        useMocha()
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
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
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
        "charset" to "utf-8",
        "disabled_rules" to "filename"
      )
    )

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()

    licenseHeaderFile(rootProject.file("spotless/license.txt"))
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
  plugins.withType<MavenPublishBasePlugin> {
    extensions.configure<MavenPublishBaseExtension> {
      publishToMavenCentral(SonatypeHost.S01)
      signAllPublications()
    }
  }
}

tasks.withType<AbstractTestTask> {
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

      sourceLink {
        localDirectory.set(file("src/commonMain/kotlin"))
        remoteUrl.set(URL("https://github.com/hoc081098/FlowExt/tree/master/src/commonMain/kotlin"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}
