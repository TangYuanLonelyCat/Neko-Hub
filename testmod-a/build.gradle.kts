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
    // 依赖 ModLoader 项目
    implementation(project(":modloader"))
}

tasks.jar {
    manifest {
        attributes(
            "Mod-Id" to "TestModA",
            "Mod-Version" to "1.0.0"
        )
    }
}
