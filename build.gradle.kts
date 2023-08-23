plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.8.22"
  id("org.jetbrains.intellij") version "1.14.1"
}

group = "com.wzc.sw.plugin"
version = "1.3.2"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2022.2.5")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf())
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }

  patchPluginXml {
    sinceBuild.set("173")
    untilBuild.set("232.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

dependencies{
  implementation("org.dom4j:dom4j:2.1.3")
}
