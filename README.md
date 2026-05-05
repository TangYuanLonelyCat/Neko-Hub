# Neko-Hub

模组加载器核心 | ModLoader Core

---

## 简介 | Introduction

Neko-Hub 是一个基于 Java 21 和 Kotlin 构建的模组加载器核心。项目专注于提供稳定、高效的模组加载和管理系统，支持 CLI 模式。核心模组加载器（ModLoader）提供了完整的模组依赖管理、版本控制、日志记录和广播通信系统。

Neko-Hub is a ModLoader core built on Java 21 and Kotlin. The project focuses on providing a stable and efficient mod loading and management system with CLI support. The core ModLoader provides comprehensive mod dependency management, version control, logging, and broadcast communication systems.

### 主要特性 | Key Features

- 模组系统
  - 支持 Java 和 Kotlin 双语言模组开发
  - 自动依赖解析和拓扑排序加载
  - 语义化版本检查和兼容性验证
  - 模组热插拔（加载/卸载）

- 广播域系统
  - 多类型域支持（公开公共域、公开私有域、私有域）
  - 四级权限控制
  - 模组间安全通信机制

- 日志系统
  - 自动日志记录所有广播消息
  - 按日期分割日志文件
  - 支持多日志级别
  - 跨天自动切换日志文件

- 配置管理
  - Boot 文件持久化配置
  - 模组权限配置
  - 语言配置
  - Boot 文件自动管理

- 安全性
  - 文件路径遍历防护
  - 模组元数据验证
  - 配置值验证
  - 循环依赖检测

---

## 项目结构 | Project Structure

```
Neko-Hub/
├── modloader/                 # 模组加载器核心
│   ├── api/                   # Java/Kotlin API 接口
│   ├── boot/                  # Boot 文件系统
│   ├── broadcast/             # 广播域系统
│   ├── command/               # 命令系统
│   ├── console/               # 控制台系统
│   ├── consolemod/            # 控制台模组
│   ├── core/                  # 核心实现
│   ├── lang/                  # 语言文件管理
│   ├── logging/               # 日志系统
│   ├── systemmod/             # 系统模组
│   └── util/                  # 工具类
├── testmod-a/                 # 测试模组 A（Kotlin）
├── testmod-b/                 # 测试模组 B（Kotlin）
└── utils/                     # 通用工具
```

---

## 快速开始 | Quick Start

### 环境要求 | Requirements

- **JDK**: 21 或更高版本 (LTS)
- **Gradle**: 9.4.0
- **Kotlin**: 2.3.10

### 构建项目 | Build Project

```bash
# 构建并运行
./gradlew run

# 仅构建
./gradlew build

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

### 使用模组加载器 | Using ModLoader

1. **启动 Neko-Hub**
   ```bash
   ./gradlew :modloader:run
   ```

2. **加载模组**
   ```bash
   # 通过命令加载
   /load my-mod.jar

   # 或使用 boot 文件自动加载
   # 在 auto.boot 文件中添加
   /load my-mod.jar
   ```

3. **管理模组权限**
   ```bash
   # 设置模组权限等级 (0-3)
   /set modpermission my-mod 1

   # 权限会自动持久化到 boot 文件
   ```

4. **切换语言**
   ```bash
   # 切换语言为英文
   /set language en

   # 切换语言为中文
   /set language zh
   ```

5. **切换 Boot 文件**
   ```bash
   # 运行时切换 boot 文件
   /change bootfile custom.boot

   # 持久化设置 boot 文件
   /set bootfile custom.boot
   ```

6. **查看帮助**
   ```bash
   /help
   ```

---

## 模组开发 | Mod Development

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
import net.lemoncookie.neko.modloader.api.KModAPI
import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.ModDependency

class MyKotlinMod : KModAPI {
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
        modLoader.console.printLine("Kotlin mod loaded!");
    }

    override fun onUnload() {
        println("Kotlin mod unloaded!");
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

## 配置说明 | Configuration

### Boot 文件 | Boot File

配置通过 boot 文件（默认 `auto.boot`）管理，支持以下命令：

```bash
# 设置模组权限
/set modpermission my-mod 1

# 设置 boot 文件
/set bootfile custom.boot

# 切换语言
/set language en

# 加载模组
/load my-mod.jar

# 监听域名
/listen start Hub.Console
```

### 权限等级 | Permission Levels

| 等级 | Level | 名称 | Name | 描述 | Description |
|------|-------|------|------|-------|-------------|
| 0 | SUPER_ADMIN | 超级管理员 | Super Administrator | 拥有所有域的权限，系统模组和控制台模组使用 | Access to all domains, used by system and console mods |
| 1 | SYSTEM_COMPONENT | 系统级组件 | System Component | 可访问公共域和系统域 | Access to public and system domains |
| 2 | NORMAL_COMPONENT | 正常组件 | Normal Component | 只能访问公共域 | Access to public domains only |
| 3 | RESTRICTED_COMPONENT | 限权组件 | Restricted Component | 仅拥有监听权限 | Listen-only access |

### 命令系统 | Command System

所有命令通过广播域 `Hub.Command` 执行，支持的命令包括：

- `/set` - 设置配置（模组权限、boot 文件、语言）
- `/change` - 变更运行时配置（boot 文件、输入框显示）
- `/clear` - 清空控制台
- `/load` - 从 JAR 文件加载模组
- `/unload` - 按名称卸载模组
- `/list` - 列出已加载的模组
- `/help` - 显示帮助信息
- `/exit` - 退出应用
- `/say` - 广播消息
- `/listen` - 监听域名
- `/autoboot` - 生成自动启动配置

---

## 文档 | Documentation

- [API 开发指南（中文）](API_GUIDE.md)
- [API Development Guide (English)](API_GUIDE_EN.md)

---

## 技术栈 | Tech Stack

- **Java**: 21 (LTS)
- **Kotlin**: 2.3.10
- **构建工具**: Gradle 9.4.0
- **依赖管理**: Gradle Version Catalogs
- **日志系统**: 自研简单日志系统
- **广播系统**: 自研广播域系统
- **语言支持**: 多语言文件支持（中文、英文）

---

## 许可证 | License

本项目采用 GNU AGPLv3 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
