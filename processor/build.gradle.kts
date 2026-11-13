dependencies {
  implementation(project(":library"))
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)
  compileOnly(libs.ksp.api)

  testImplementation(libs.ksp.api)
  testImplementation(libs.kotlin.metadata)
  testImplementation(libs.truth)
  testImplementation(libs.kctfork.ksp)
}

kotlin {
  compilerOptions {
    moduleName.set("vanilla-processor")
    optIn.add("com.squareup.kotlinpoet.DelicateKotlinPoetApi")
  }
}

tasks.compileTestKotlin {
  compilerOptions.optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}
