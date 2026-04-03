# Neko-Hub

<div align="center">

**一个多功能合一的模块化平台 | A Multi-Functional Modular Platform**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-purple.svg)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.7+-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 📖 简介 | Introduction

Neko-Hub 是一个基于 Java 21 和 Kotlin 构建的多功能模块化平台。项目采用模块化设计，支持 CLI 模式，未来将扩展 GUI 模式。核心模组加载器（ModLoader）提供了完整的模组依赖管理、版本控制、日志记录和广播通信系统。

Neko-Hub is a multi-functional modular platform built on Java 21 and Kotlin. The project features a modular design with CLI support, with plans to extend to GUI mode. The core ModLoader provides comprehensive mod dependency management, version control, logging, and broadcast communication systems.

### ✨ 主要特性 | Key Features

- **🔌 模组系统 (Mod System)**
  - 支持 Java 和 Kotlin 双语言模组开发
  - 自动依赖解析和拓扑排序加载
  - 语义化版本检查和兼容性验证
  - 模组热插拔（加载/卸载）

- **📢 广播域系统 (Broadcast Domain System)**
  - 多类型域支持（公开公共域、公开私有域、私有域）
  - 四级权限控制（SUPER_ADMIN、SYSTEM_COMPONENT、NORMAL_COMPONENT、RESTRICTED_COMPONENT）
  - 模组间安全通信机制

- **📝 日志系统 (Logging System)**
  - 自动日志记录所有广播消息
  - 按日期分割日志文件
  - 支持多日志级别（INFO、WARN、ERROR、DEBUG）
  - 跨天自动切换日志文件

- **⚙️ 配置管理 (Configuration Management)**
  - 持久化配置存储
  - 延迟保存优化
  - 模组权限配置
  - Boot 文件自动管理

- **🛡️ 安全性 (Security)**
  - 文件路径遍历防护
  - 模组元数据验证
  - 配置值验证
  - 循环依赖检测

---

## 🏗️ 项目结构 | Project Structure

```
Neko-Hub/
├── modloader/                 # 模组加载器核心 | ModLoader Core
│   ├── api/                   # Java/Kotlin API 接口
│   ├── boot/                  # Boot 文件系统
│   ├── broadcast/             # 广播域系统
│   ├── command/               # 命令系统
│   ├── config/                # 配置管理
│   ├── console/               # 控制台系统
│   ├── core/                  # 核心实现
│   ├── logging/               # 日志系统
│   └── util/                  # 工具类（版本比较等）
├── bookkeeping/               # 记账模块 | Bookkeeping Module
├── markdown/                  # Markdown 处理模块 | Markdown Module
├── filelabel/                 # 文件标签模块 | File Label Module
├── calendar/                  # 日历模块 | Calendar Module
├── todolist/                  # 待办事项模块 | TodoList Module
└── utils/                     # 通用工具 | Utilities
```

---

## 🚀 快速开始 | Quick Start

### 环境要求 | Requirements

- **JDK**: 21 或更高版本 (LTS)
- **Gradle**: 8.7+
- **Kotlin**: 2.3.10

### 构建项目 | Build Project

```bash
# 构建并运行 | Build and Run
./gradlew run

# 仅构建 | Build Only
./gradlew build

# 运行测试 | Run Tests
./gradlew test

# 清理构建 | Clean Build
./gradlew clean
```

### 使用模组加载器 | Using ModLoader

1. **启动 Neko-Hub**
   ```bash
   ./gradlew :modloader:run
   ```

2. **加载模组 | Load Mods**
   ```bash
   # 通过命令加载
   /load my-mod.jar
   
   # 或使用 boot 文件自动加载
   # 在 auto.boot 文件中添加：
   /load my-mod.jar
   ```

3. **管理模组权限 | Manage Mod Permissions**
   ```bash
   # 设置模组权限等级 (0-3)
   /set modPermission my-mod 1
   ```

4. **查看帮助 | View Help**
   ```bash
   /help
   ```

---

## 📦 模组开发 | Mod Development

### Java 模组示例 | Java Mod Example

```java
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.ModLoader;

import java.util.List;

public class MyJavaMod implements IModAPI {
    @Override
    public String getModId() { 
        return "my-java-mod"; 
    }

    @Override
    public String getVersion() { 
        return "1.0.0"; 
    }

    @Override
    public String getPackageName() { 
        return "com.example.myjavamod"; 
    }

    @Override
    public List<ModDependency> getDependencies() {
        return List.of(
            new ModDependency("console-mod", "1.0.0")
        );
    }

    @Override
    public void onLoad(ModLoader modLoader) {
        modLoader.getConsole().printLine("Java mod loaded!");
    }

    @Override
    public void onUnload() {
        System.out.println("Java mod unloaded!");
    }
}
```

### Kotlin 模组示例 | Kotlin Mod Example

```kotlin
import net.lemoncookie.neko.modloader.api.ModAPI
import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.ModDependency

class MyKotlinMod : ModAPI {
    override val modId = "my-kotlin-mod"
    override val version = "1.0.0"
    override val packageName = "com.example.mykotlinmod"
    override val name = "My Kotlin Mod"

    override fun getDependencies(): List<ModDependency> {
        return listOf(
            ModDependency("console-mod", "1.0.0")
        )
    }

    override fun onLoad(modLoader: ModLoader) {
        modLoader.console.printLine("Kotlin mod loaded!")
    }

    override fun onUnload() {
        println("Kotlin mod unloaded!")
    }
}
```

### JAR 文件清单配置 | JAR Manifest Configuration

在 `build.gradle.kts` 中配置 JAR 清单：

```kotlin
tasks.jar {
    manifest {
        attributes(
            "Mod-Id" to "my-mod",
            "Mod-Version" to "1.0.0",
            "Mod-Dependencies" to "console-mod:1.0.0,core-lib:2.0.0",
            "Mod-Impl-Class" to "com.example.MyMod"
        )
    }
}
```

---

## 🔧 配置说明 | Configuration

### 配置文件 | Config File

配置文件 `neko-hub.config` 支持以下配置项：

```properties
# Boot 文件名 | Boot File Name
bootfile=auto.boot

# 模组权限配置 | Mod Permission Configuration
modpermission.my-mod=1
modpermission.another-mod=2
```

### 权限等级 | Permission Levels

| 等级 | Level | 名称 | Name | 描述 | Description |
|------|-------|------|------|-------|-------------|
| 0 | SUPER_ADMIN | 超级管理员 | Super Administrator | 拥有所有域的权限 | Access to all domains |
| 1 | SYSTEM_COMPONENT | 系统级组件 | System Component | 可访问公共域和系统域 | Access to public and system domains |
| 2 | NORMAL_COMPONENT | 正常组件 | Normal Component | 只能访问公共域 | Access to public domains only |
| 3 | RESTRICTED_COMPONENT | 限权组件 | Restricted Component | 仅拥有监听权限 | Listen-only access |

---

## 📚 文档 | Documentation

- [API 开发指南（中文）](API_GUIDE.md)
- [API Development Guide (English)](API_GUIDE_EN.md)
- [模组依赖示例](modloader/DEPENDENCY_EXAMPLE.md)

---

## 🛠️ 技术栈 | Tech Stack

- **Java**: 21 (LTS)
- **Kotlin**: 2.3.10
- **构建工具 | Build Tool**: Gradle 8.7+
- **依赖管理 | Dependency Management**: Gradle Version Catalogs
- **日志系统 | Logging**: 自研简单日志系统 (Custom Simple Logger)

---

## 🤝 贡献 | Contributing

欢迎贡献代码、报告问题或提出建议！

Contributions are welcome! Please feel free to submit code, report issues, or suggest improvements.

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证 | License

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 联系方式 | Contact

- **项目主页 | Project Homepage**: [GitHub](https://github.com/yourusername/Neko-Hub)
- **问题反馈 | Issue Tracker**: [GitHub Issues](https://github.com/yourusername/Neko-Hub/issues)

---

## 🙏 致谢 | Acknowledgments

感谢所有为 Neko-Hub 做出贡献的开发者和用户！

Thanks to all developers and users who have contributed to Neko-Hub!

---

<div align="center">

**Made with ❤️ by the Neko-Hub Team**

[返回顶部 | Back to Top](#neko-hub)

</div>
