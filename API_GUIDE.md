# Neko-Hub API 开发指南

## 项目概述

Neko-Hub 是一个多功能合一的项目，支持 CLI 和 GUI 两种模式。项目采用模块化设计，**基于 Java 21**，便于扩展和维护。

## 技术栈

- **Java**: 21 (LTS)
- **Kotlin**: 2.0.0
- **构建工具**: Gradle

## 模块结构

### ModLoader 模块 (`net.lemoncookie.neko.modloader`)

模组加载器核心模块，提供 **Java** 和 **Kotlin** 两套 API。

#### 目录结构

```
modloader/
├── src/main/java/net/lemoncookie/neko/modloader/
│   ├── core/
│   │   └── ModCore.java          # Java 21 核心实现
│   ├── api/
│   │   └── IModAPI.java          # Java版API接口
│   └── lib/
│       └── ModLibrary.java       # Java版库支持
└── src/main/kotlin/net/lemoncookie/neko/modloader/
    ├── ModLoader.kt              # 主入口类
    ├── api/kt/
    │   └── ModAPI.kt             # Kotlin版API接口
    └── lib/kt/
        └── ModLibrary.kt         # Kotlin版库支持
```

#### Java API (`net.lemoncookie.neko.modloader.api`)

```java
// Java版模组API接口
public interface IModAPI {
    String getModId();
    String getVersion();
    void onLoad();
    void onUnload();
    default String getName() { return getModId(); }
}

// Java版库支持
public class ModLibrary {
    public void register(String name, Object component);
    public <T> T get(String name);
    public boolean has(String name);
    public Set<String> getRegisteredNames();
}
```

#### Kotlin API (`net.lemoncookie.neko.modloader.api.kt`)

```kotlin
// Kotlin版模组API接口
interface ModAPI {
    val modId: String
    val version: String
    val name: String
    fun onLoad()
    fun onUnload()
    fun getInfo(): ModInfo
}

// 模组信息数据类
data class ModInfo(val id: String, val name: String, val version: String)

// Kotlin版库支持（DSL风格）
class ModLibrary {
    fun register(name: String, component: Any)
    operator fun <T> get(name: String): T?
    operator fun contains(name: String): Boolean
    inline fun <reified T> register(name: String, noinline init: () -> T): T
    fun registerAll(vararg pairs: Pair<String, Any>)
}

// DSL函数
inline fun modLibrary(block: ModLibrary.() -> Unit): ModLibrary
```

#### 核心实现 (`net.lemoncookie.neko.modloader.core`)

```java
// Java 21 实现，保证稳定性
public class ModCore {
    public void start();
    public String getVersion();  // 返回 "1.0.0"
    public boolean isInitialized();
}
```

#### 主入口类

```kotlin
class ModLoader {
    fun initialize()
    fun registerJavaMod(mod: IModAPI)
    fun registerKotlinMod(mod: ModAPI)
    fun getJavaLibrary(): JavaModLibrary
    fun getKotlinLibrary(): KotlinModLibrary
    fun getCore(): ModCore
    fun unloadAll()
}
```

### 2. Bookkeeping 模块 (`net.lemoncookie.neko.bookkeeping`)

记账模块，提供财务管理功能。

- `Bookkeeping` - 记账管理类

### 3. Markdown 模块 (`net.lemoncookie.neko.markdown`)

Markdown 处理模块，支持 Markdown 解析和渲染。

- `Markdown` - Markdown 解析器

### 4. FileLabel 模块 (`net.lemoncookie.neko.filelabel`)

文件标签模块，支持为文件添加标签管理。

- `FileLabel` - 文件标签管理

### 5. Calendar 模块 (`net.lemoncookie.neko.calendar`)

日历模块，提供日程管理功能。

- `Calendar` - 日历管理

### 6. TodoList 模块 (`net.lemoncookie.neko.todolist`)

待办事项模块，支持任务管理。

- `TodoList` - 待办事项列表

## 快速开始

### 添加依赖

在 `build.gradle.kts` 中添加模块依赖：

```kotlin
dependencies {
    implementation(project(":modloader"))
    implementation(project(":bookkeeping"))
    implementation(project(":markdown"))
    implementation(project(":filelabel"))
    implementation(project(":calendar"))
    implementation(project(":todolist"))
}
```

### Java模组开发示例

```java
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.ModLoader;

public class MyJavaMod implements IModAPI {
    @Override
    public String getModId() { return "my-java-mod"; }

    @Override
    public String getVersion() { return "1.0.0"; }

    @Override
    public void onLoad() {
        System.out.println("Java mod loaded!");
    }

    @Override
    public void onUnload() {
        System.out.println("Java mod unloaded!");
    }
}

// 注册模组
ModLoader loader = new ModLoader();
loader.initialize();
loader.registerJavaMod(new MyJavaMod());
```

### Kotlin模组开发示例

```kotlin
import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.kt.ModAPI
import net.lemoncookie.neko.modloader.lib.kt.modLibrary

class MyKotlinMod : ModAPI {
    override val modId = "my-kotlin-mod"
    override val version = "1.0.0"
    override val name = "My Kotlin Mod"

    override fun onLoad() {
        println("Kotlin mod loaded!")
    }

    override fun onUnload() {
        println("Kotlin mod unloaded!")
    }
}

// 注册模组
val loader = ModLoader()
loader.initialize()
loader.registerKotlinMod(MyKotlinMod())

// 使用Kotlin版库的DSL
val lib = modLibrary {
    register("config") { loadConfig() }
    register("database", Database())
}
```

## 开发规范

1. **Java版本**: 所有模块必须使用 Java 21
2. **包命名**: 遵循 `net.lemoncookie.neko.{module}` 规范
3. **API设计**: 
   - Java API 使用传统面向对象风格
   - Kotlin API 使用 DSL 和函数式风格
4. **核心稳定性**: 核心功能使用 Java 21 实现
5. **测试**: 每个模块应包含单元测试

## 环境要求

- JDK 21 或更高版本
- Gradle 8.7+

## 构建项目

```bash
./gradlew build
```

## 运行测试

```bash
./gradlew test
```
