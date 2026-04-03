# Neko-Hub API 开发指南

## 项目概述

Neko-Hub 是一个多功能合一的项目，目前支持 CLI 模式，未来将支持 GUI 模式。项目采用模块化设计，**基于 Java 21**，便于扩展和维护。

## 技术栈

- **Java**: 21 (LTS)
- **Kotlin**: 2.3.10
- **构建工具**: Gradle

## 模块结构

### ModLoader 模块 (`net.lemoncookie.neko.modloader`)

模组加载器核心模块，提供 **Java** API。

#### 目录结构

```
modloader/
├── src/main/java/net/lemoncookie/neko/modloader/
│   ├── api/
│   │   └── IModAPI.java          # Java 版 API 接口
│   ├── boot/                      # Boot 文件系统
│   │   └── BootFileManager.java   # Boot 文件管理器
│   ├── broadcast/                 # 广播域系统
│   │   ├── BroadcastDomain.java
│   │   ├── BroadcastManager.java
│   │   ├── MessageListener.java
│   │   ├── ModPermission.java     # 权限枚举
│   │   └── PermissionManager.java # 权限管理器
│   ├── command/                   # 命令系统
│   │   ├── Command.java
│   │   ├── CommandSystem.java
│   │   ├── ClearCommand.java
│   │   ├── HelpCommand.java
│   │   ├── LoadCommand.java
│   │   ├── UnloadCommand.java
│   │   ├── SetCommand.java        # 设置命令
│   │   └── AutobootCommand.java   # 自动启动命令
│   ├── config/                    # 配置管理
│   │   └── ConfigManager.java     # 配置管理器
│   ├── console/                   # 控制台系统
│   │   └── Console.java
│   ├── consolemod/                # 控制台模组
│   │   └── ConsoleMod.java        # 控制台模组实现
│   ├── core/
│   │   └── ModCore.java          # Java 21 核心实现
│   ├── lang/                      # 语言管理系统
│   │   └── LanguageManager.java
│   ├── lib/
│   │   └── ModLibrary.java       # Java 版库支持
│   └── ModLoader.java            # 主入口类
└── src/main/kotlin/net/lemoncookie/neko/modloader/
    ├── api/
    │   └── ModAPI.kt             # Kotlin 版 API 接口
    └── lib/
        └── ModLibrary.kt         # Kotlin 版库支持
```

#### 控制台系统 (`net.lemoncookie.neko.modloader.console`)

控制台系统负责显示信息和处理用户输入，支持彩色输出。

```java
// 控制台类
public class Console {
    // 构造函数
    public Console(ModLoader modLoader)
    
    // 打印方法
    public void printLine(String text)
    public void printLine()
    public void print(String text)
    
    // 彩色输出方法
    public void printError(String text)    // 红色
    public void printWarning(String text)  // 黄色
    public void printSuccess(String text)  // 绿色
    public void printInfo(String text)     // 蓝色
    
    // 交互式控制台
    public void startInteractive()
    
    // 其他方法
    public void clear()
    public String readLine() throws IOException
    public boolean readConfirmation() throws IOException
    public void close()
}
```

#### 命令系统 (`net.lemoncookie.neko.modloader.command`)

命令系统负责解析和执行命令，支持内置命令和自定义命令。

```java
// 命令系统
public class CommandSystem {
    // 构造函数
    public CommandSystem(ModLoader modLoader)
    
    // 命令管理
    public void registerCommand(String name, Command command)
    public void executeCommand(String input)
    public Map<String, Command> getCommands()
}

// 命令接口
public interface Command {
    void execute(ModLoader modLoader, String args) throws Exception;
}
```

内置命令：
- `/help` - 显示帮助信息
- `/clear` - 清空控制台
- `/load` - 加载模组
- `/unload` - 卸载模组

#### 广播域系统 (`net.lemoncookie.neko.modloader.broadcast`)

广播域系统用于模组之间的通信，支持公共域和私有域。

```java
// 广播域管理器
public class BroadcastManager {
    // 系统广播域
    public static final String HUB_ALL = "Hub.ALL";
    public static final String HUB_SYSTEM = "Hub.System";
    
    // 构造函数
    public BroadcastManager(ModLoader modLoader)
    
    // 广播域管理
    public boolean addDomain(String name, boolean isPrivate, String ownerModId)
    public BroadcastDomain getDomain(String name)
    public boolean removeDomain(String name, String modId)
    
    // 广播功能
    public void broadcast(String domainName, String message, String senderModId)
    public boolean listen(String domainName, MessageListener listener, String modId, String modName)
    public boolean listenPrivate(String modId, MessageListener listener)
    
    // 其他方法
    public Map<String, BroadcastDomain> getDomains()
    public int getDomainCount()
    public boolean hasDomain(String name)
}

// 消息监听器接口
public interface MessageListener {
    void onMessage(String domain, String message, String senderModId);
}
```

#### Java API (`net.lemoncookie.neko.modloader.api`)

```java
// Java版模组API接口
public interface IModAPI {
    String getModId();
    String getVersion();
    String getPackageName();
    void onLoad(ModLoader modLoader);
    void onUnload();
    default String getName() { return getModId(); }
    default void registerCommands(ModLoader modLoader) {}
    default void registerBroadcastListeners(ModLoader modLoader) {}
}

// Java版库支持
public class ModLibrary {
    public void register(String name, Object component);
    public <T> T get(String name);
    public boolean has(String name);
    public Set<String> getRegisteredNames();
}
```

#### 核心实现 (`net.lemoncookie.neko.modloader.core`)

```java
// Java 21 实现，保证稳定性
public class ModCore {
    public void start();
}
```

#### Kotlin API (`net.lemoncookie.neko.modloader.api`)

```kotlin
// Kotlin 版模组 API 接口
interface ModAPI {
    val modId: String
    val version: String
    val name: String
        get() = modId
    val packageName: String
    fun onLoad(modLoader: ModLoader)
    fun onUnload()
    fun registerCommands(modLoader: ModLoader) {}
    fun registerBroadcastListeners(modLoader: ModLoader) {}
    fun getInfo(): ModInfo
}

data class ModInfo(val id: String, val name: String, val version: String)
```

#### Kotlin 版库支持 (`net.lemoncookie.neko.modloader.lib`)

```kotlin
// Kotlin 版库支持（DSL 风格）
class ModLibrary {
    fun register(name: String, component: Any)
    operator fun <T> get(name: String): T?
    operator fun contains(name: String): Boolean
    fun getRegisteredNames(): Set<String>
    inline fun <reified T> register(name: String, noinline init: () -> T): T
    fun registerAll(vararg pairs: Pair<String, Any>)
}

inline fun modLibrary(block: ModLibrary.() -> Unit): ModLibrary
```

#### 主入口类

```java
public class ModLoader {
    public ModLoader()
    public void initialize()
    public void registerJavaMod(IModAPI mod)
    public void registerKotlinMod(ModAPI mod)
    public ModLibrary getJavaLibrary()
    public List<IModAPI> getJavaMods()
    public List<ModAPI> getKotlinMods()
    public void unloadAll()
    public ModCore getCore()
    public Console getConsole()
    public CommandSystem getCommandSystem()
    public BroadcastManager getBroadcastManager()
    public LanguageManager getLanguageManager()
    public static String getVersion()
    public static String getMinApiVersion()
    public static String getGithubVersion()
    public boolean isInitialized()
    public static void main(String[] args)
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

## 模组加载器行为

### 终端模式

Neko-Hub 支持 CLI 模式，通过控制台与用户交互。控制台系统支持：

- **彩色输出**：使用 ANSI 颜色代码，支持红色（错误）、黄色（警告）、绿色（成功）和蓝色（信息）
- **交互式命令**：用户可以输入命令与系统交互
- **跨平台支持**：在 Windows 和 Unix-like 系统上都能正常工作
- **UTF-8 编码**：支持中文等非ASCII字符

### 命令系统运行原理

命令系统的工作流程：

1. **命令注册**：系统启动时注册内置命令，模组也可以注册自定义命令
2. **命令解析**：解析用户输入，提取命令名和参数
3. **命令执行**：找到对应的命令实现并执行
4. **错误处理**：捕获并显示命令执行过程中的错误

### 模组加载流程

1. **初始化**：创建 ModLoader 实例并调用 initialize() 方法
2. **模组扫描**：扫描和加载模组
3. **命令注册**：模组注册自定义命令
4. **广播域监听**：模组注册广播域监听器
5. **控制台启动**：启动交互式控制台

### 广播域系统使用

广播域系统允许模组之间进行通信：

- **公共域**：所有模组都可以监听和发送消息
- **私有域**：只有特定模组可以访问
- **系统域**：系统级别的广播域，需要用户确认才能监听

### 语言系统使用

语言系统支持多语言：

- **默认语言**：英语（en）
- **语言文件**：位于 resources/lang/ 目录下
- **动态切换**：可以在运行时切换语言
- **消息格式化**：支持带参数的消息格式化

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
    public String getPackageName() { return "com.example.myjavamod"; }

    @Override
    public void onLoad(ModLoader modLoader) {
        modLoader.getConsole().printLine("Java mod loaded!");
    }

    @Override
    public void onUnload() {
        System.out.println("Java mod unloaded!");
    }

    @Override
    public String getName() { return "My Java Mod"; }
}

// 注册模组
ModLoader loader = new ModLoader();
loader.initialize();
loader.registerJavaMod(new MyJavaMod());
```

### Kotlin模组开发示例

```kotlin
import net.lemoncookie.neko.modloader.api.ModAPI
import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.lib.modLibrary

class MyKotlinMod : ModAPI {
    override val modId = "my-kotlin-mod"
    override val version = "1.0.0"
    override val packageName = "com.example.mykotlinmod"
    override val name = "My Kotlin Mod"

    override fun onLoad(modLoader: ModLoader) {
        modLoader.getConsole().printLine("Kotlin mod loaded!")
    }

    override fun onUnload() {
        println("Kotlin mod unloaded!")
    }
}

// 注册模组
val loader = ModLoader()
loader.initialize()
loader.registerKotlinMod(MyKotlinMod())

// 使用 Kotlin 版库的 DSL
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
