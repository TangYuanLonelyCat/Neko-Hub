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

tasks.withType<Jar> {
    archiveBaseName.set("Neko-Hub-ModLoader")
    manifest {
        attributes(
            "Main-Class" to "net.lemoncookie.neko.modloader.ModLoader"
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
