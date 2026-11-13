dependencies {
  api(libs.kotlin.result)

  testImplementation(libs.truth)
}

kotlin {
  compilerOptions {
    moduleName.set("vanilla-library")
  }
}
