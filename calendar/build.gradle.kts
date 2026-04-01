plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "net.lemoncookie.neko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
