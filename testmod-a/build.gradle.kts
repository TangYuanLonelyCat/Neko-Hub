plugins {
    java
}

group = "net.lemoncookie.neko.modloader.test"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // 依赖 ModLoader 项目（compileOnly 表示编译时需要，运行时由 ModLoader 提供）
    compileOnly(project(":modloader"))
}

tasks.jar {
    archiveBaseName.set("testmod-a")
    manifest {
        attributes(
            "Mod-Id" to "TestModA",
            "Mod-Version" to "1.0.0",
            "Mod-Name" to "TestModA - Base Module",
            "Mod-Impl-Class" to "net.lemoncookie.neko.modloader.testmod.a.TestModA",
            "Mod-Dependencies" to "TestModB:1.0.0",
            "Implementation-Title" to "TestModA",
            "Implementation-Version" to version
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
