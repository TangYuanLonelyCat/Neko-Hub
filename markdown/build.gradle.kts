plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "net.lemoncookie.neko"
version = "2.0.0"

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
    implementation("org.openjfx:javafx-web:$javafxVersion")
    
    // Markdown parsing library with GFM support
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.21.0")
    implementation("org.commonmark:commonmark-ext-task-list-items:0.21.0")
    
    // Math formula support (KaTeX via CDN, no additional dependency needed)
}
