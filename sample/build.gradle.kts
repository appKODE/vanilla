plugins {
  application
  alias(libs.plugins.ksp)
}

application {
  mainClass.set("ru.dimsuz.vanilla.sample.MainKt")
}

dependencies {
  implementation(project(":library"))
  ksp(project(":processor"))

  testImplementation(libs.truth)
}
