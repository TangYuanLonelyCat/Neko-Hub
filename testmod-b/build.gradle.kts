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
    archiveBaseName.set("testmod-b")
    manifest {
        attributes(
            "Mod-Id" to "TestModB",
            "Mod-Version" to "1.0.0",
            "Mod-Name" to "TestModB - Dependent Module",
            "Mod-Impl-Class" to "net.lemoncookie.neko.modloader.testmod.b.TestModB",
            "Implementation-Title" to "TestModB",
            "Implementation-Version" to version
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
