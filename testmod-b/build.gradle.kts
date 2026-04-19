plugins {
    kotlin("jvm")
    java
}

group = "net.lemoncookie.neko.modloader.test"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    // 依赖 ModLoader 项目（compileOnly 表示编译时需要，运行时由 ModLoader 提供）
    compileOnly(project(":modloader"))
    implementation(kotlin("stdlib"))
}

tasks.jar {
    archiveBaseName.set("testmod-b")
    manifest {
        attributes(
            "Mod-Id" to "TestModB",
            "Mod-Version" to version,
            "Mod-Name" to "TestModB - Complete Module (Kotlin)",
            "Mod-Impl-Class" to "net.lemoncookie.neko.modloader.testmod.b.TestModB",
            "Mod-API-Version" to "2.2.0",
            "Implementation-Title" to "TestModB",
            "Implementation-Version" to version
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
