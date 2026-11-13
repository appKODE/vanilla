plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.vanniktech.mavenPublish) apply false
}

allprojects {
  repositories {
    mavenCentral()
  }
}

val ktlint: Configuration by configurations.creating

dependencies {
  ktlint("com.pinterest:ktlint:0.42.0")
}

val outputDir = layout.buildDirectory.dir("reports/ktlint")
val inputFiles = project.fileTree(mapOf("dir" to ".", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
  inputs.files(inputFiles)
  outputs.dir(outputDir)

  description = "Check Kotlin code style."
  classpath = ktlint
  group = "verification"
  mainClass.set("com.pinterest.ktlint.Main")
  args = listOf("**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
  inputs.files(inputFiles)
  outputs.dir(outputDir)

  description = "Fix Kotlin code style deviations."
  classpath = ktlint
  group = "verification"
  mainClass.set("com.pinterest.ktlint.Main")
  args = listOf("-F", "**/*.kt")
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  // Consumers are Android apps: keep the JVM 11 bytecode floor, build on any JDK >= 11 (CI uses 17)
  extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
  }
  extensions.configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

configure(listOf(project(":library"), project(":processor"))) {
  apply(plugin = "com.vanniktech.maven.publish")

  group = providers.gradleProperty("pomGroupId").get()
  version = providers.gradleProperty("versionName").get()

  extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    coordinates(artifactId = property("pomArtifactId").toString())

    publishToMavenCentral()
    signAllPublications()

    pom {
      name.set(property("pomName").toString())
      description.set(providers.gradleProperty("pomDescription"))
      url.set(providers.gradleProperty("pomUrl"))

      scm {
        url.set(providers.gradleProperty("pomScmUrl"))
        connection.set(providers.gradleProperty("pomScmConnection"))
        developerConnection.set(providers.gradleProperty("pomScmDevConnection"))
      }

      licenses {
        license {
          name.set(providers.gradleProperty("pomLicenseName"))
          url.set(providers.gradleProperty("pomLicenseUrl"))
          distribution.set(providers.gradleProperty("pomLicenseDist"))
        }
      }

      developers {
        developer {
          id.set("dimsuz")
          name.set("Dmitry Suzdalev")
        }
        developer {
          id.set("appKODE")
          name.set("appKODE")
          url.set("https://github.com/appKODE")
        }
      }
    }
  }
}
