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
    implementation(project(":modloader"))
    
    // JavaFX for GUI rendering
    val javafxVersion = "21"
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-swing:$javafxVersion")
    
    // Markdown parsing library
    implementation("org.commonmark:commonmark:0.21.0")
}
