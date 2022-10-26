import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import java.net.URL
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
  kotlin("multiplatform") version "1.7.10"
  id("com.diffplug.spotless") version "6.11.0"
  id("maven-publish")
  id("com.vanniktech.maven.publish") version "0.22.0"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.12.1"
  id("org.jetbrains.dokka") version "1.7.20"
  id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

val coroutinesVersion = "1.6.4"
val ktlintVersion = "0.46.1"

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
  iosSimulatorArm64()

  macosX64()
  macosArm64()
  mingwX64()
  linuxX64()

  tvosX64()
  tvosSimulatorArm64()
  tvosArm64()

  watchosArm32()
  watchosArm64()
  watchosX64()
  watchosX86()
  watchosSimulatorArm64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
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
      "iosSimulatorArm64",
      "iosArm64",
      "iosArm32",
      "macosX64",
      "macosArm64",
      "tvosArm64",
      "tvosX64",
      "tvosSimulatorArm64",
      "watchosArm32",
      "watchosArm64",
      "watchosX86",
      "watchosSimulatorArm64",
      "watchosX64"
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
  val EDITOR_CONFIG_KEYS: Set<String> = hashSetOf(
    "ij_kotlin_imports_layout",
    "indent_size",
    "end_of_line",
    "charset",
    "continuation_indent_size",
    "disabled_rules"
  )

  kotlin {
    target("**/*.kt")

    // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
    val data = mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "4",
      "ij_kotlin_imports_layout" to "*",
      "end_of_line" to "lf",
      "charset" to "utf-8",
      "disabled_rules" to arrayOf("filename").joinToString(separator = ",")
    )

    ktlint(ktlintVersion)
      .setUseExperimental(true)
      .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
      .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()

    licenseHeaderFile(rootProject.file("spotless/license.txt"))
  }

  kotlinGradle {
    target("**/*.kts")

    val data = mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "4",
      "ij_kotlin_imports_layout" to "*",
      "end_of_line" to "lf",
      "charset" to "utf-8"
    )
    ktlint(ktlintVersion)
      .setUseExperimental(true)
      .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
      .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

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
