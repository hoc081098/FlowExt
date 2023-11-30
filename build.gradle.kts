import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.net.URL

plugins {
  kotlin("multiplatform") version "1.9.21"
  id("com.diffplug.spotless") version "6.23.2"
  id("maven-publish")
  id("com.vanniktech.maven.publish") version "0.25.3"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.2"
  id("org.jetbrains.dokka") version "1.9.10"
  id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

val coroutinesVersion = "1.7.3"
val ktlintVersion = "1.0.0"

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

kotlin {
  explicitApi()

  sourceSets {
    all {
      languageSettings {
        optIn("com.hoc081098.flowext.DelicateFlowExtApi")
      }
    }
  }

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendor.set(JvmVendorSpec.AZUL)
  }

  jvm {
    compilations.all {
      compilerOptions.configure {
        jvmTarget = JvmTarget.JVM_1_8
      }
    }
  }
  js(IR) {
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
          timeout = "10s"
        }
      }
    }
    nodejs {
      testTask {
        useMocha {
          timeout = "10s"
        }
      }
    }
  }

  // According to https://kotlinlang.org/docs/native-target-support.html

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  macosX64()
  macosArm64()
  mingwX64()
  linuxX64()
  linuxArm64()

  tvosX64()
  tvosSimulatorArm64()
  tvosArm64()

  watchosArm32()
  watchosArm64()
  watchosX64()
  watchosSimulatorArm64()
  watchosDeviceArm64()

  androidNativeArm32()
  androidNativeArm64()
  androidNativeX86()
  androidNativeX64()

  sourceSets {
    commonMain {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
      }
    }
    jvmTest {
      dependencies {
        implementation(kotlin("test-junit"))
      }
    }
    jsTest {
      dependencies {
        implementation(kotlin("test-js"))
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

tasks.withType<KotlinCompile<*>>().configureEach {
  kotlinOptions {
    // 'expect'/'actual' classes (including interfaces, objects, annotations, enums,
    // and 'actual' typealiases) are in Beta.
    // You can use -Xexpect-actual-classes flag to suppress this warning.
    // Also see: https://youtrack.jetbrains.com/issue/KT-61573
    freeCompilerArgs +=
      listOf(
        "-Xexpect-actual-classes",
      )
  }
}

spotless {
  kotlin {
    target("**/*.kt")

    ktlint(ktlintVersion)
      .setEditorConfigPath("$rootDir/.editorconfig")

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()

    licenseHeaderFile(rootProject.file("spotless/license.txt"))
  }

  kotlinGradle {
    target("**/*.kts")

    ktlint(ktlintVersion)
      .setEditorConfigPath("$rootDir/.editorconfig")

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
}

allprojects {
  plugins.withType<MavenPublishBasePlugin> {
    extensions.configure<MavenPublishBaseExtension> {
      publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
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
    events =
      setOf(
        TestLogEvent.PASSED,
        TestLogEvent.FAILED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_OUT,
        TestLogEvent.STANDARD_ERROR,
      )
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets {
    configureEach {
      externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")

      sourceLink {
        localDirectory.set(file("src"))
        remoteUrl.set(URL("https://github.com/hoc081098/FlowExt/tree/master/src"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}
