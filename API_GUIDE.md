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
        └── KotlinModLibrary.kt         # Kotlin 版库支持
```

#### 控制台系统 (`net.lemoncookie.neko.modloader.console`)

控制台系统负责显示信息和处理用户输入，支持彩色输出和广播域消息显示。

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
   public void printCyan(String text)     // 青色
   public void printMagenta(String text)  // 紫色
   public void printWhite(String text)    // 白色

   // 交互式控制台
   public void startInteractive()

   // 其他方法
   public void clear()
   public String readLine() throws IOException
   public boolean readConfirmation() throws IOException
   public void close()
}
```

#### Boot 文件系统 (`net.lemoncookie.neko.modloader.boot`)

Boot 文件系统用于在启动时自动执行命令序列，支持自定义 boot 文件。

```java
// Boot 文件管理器
public class BootFileManager {
   // 构造函数
   public BootFileManager(ModLoader modLoader)

   // Boot 文件操作
   public List<String> readBootFile(String fileName)
   public boolean executeBootFile(String fileName)
   public void generateAutoBoot()                    // 生成 auto.boot
   public void setCurrentBootFile(String fileName)
   public String getCurrentBootFile()
   public boolean executeCurrentBootFile()
}
```

**Boot 文件语法：**
- 每行一个命令
- 支持 `/load`、`/unload`、`/set` 等所有命令
- 以 `#` 开头的行为注释
- 默认文件名为 `auto.boot`

**示例：**
```
# auto.boot 示例
/load com.example.mod
/load com.example.moda
/set modPermission com.example.moda 1
```

#### 配置管理 (`net.lemoncookie.neko.modloader.config`)

配置管理系统用于持久化存储配置信息。

```java
// 配置管理器
public class ConfigManager {
   // 构造函数
   public ConfigManager(ModLoader modLoader)

   // 配置操作
   public String getConfig(String key, String defaultValue)
   public void setConfig(String key, String value)

   // Boot 文件配置
   public String getBootFile()
   public void setBootFile(String fileName)

   // 模组权限配置
   public ModPermission getModPermission(String modId)
   public void setModPermission(String modId, int level)
   public Map<String, Integer> getAllModPermissions()
}
```

#### 控制台模组 (`net.lemoncookie.neko.modloader.consolemod`)

控制台模组是优先加载的系统模组，负责创建系统域和显示消息。

```java
// 控制台模组
public class ConsoleMod implements IModAPI {
   // 模组信息
   public String getModId()         // 返回 "console-mod"
   public String getVersion()       // 返回 "1.0.0"
   public String getPackageName()   // 返回 "net.lemoncookie.neko.modloader.consolemod"
   public String getName()          // 返回 "Console Mod"

   // 生命周期
   public void onLoad(ModLoader modLoader)    // 创建 Hub.System 和 Hub.Console 域
   public void onUnload()

   // 注册方法
   public void registerCommands(ModLoader modLoader)
   public void registerBroadcastListeners(ModLoader modLoader)
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
   String getDescription();
   String getUsage();
}
```

**内置命令：**
- `/help` - 显示可用命令
- `/clear` - 清空控制台
- `/load [模组文件名]` - 加载模组（通过文件名，会自动添加.jar 后缀）
- `/unload [模组名称]` - 卸载模组（通过模组名称/ID）
- `/set modPermission [模组名] [level 值]` - 设置模组权限（level 0-3）
- `/set bootfile [文件名]` - 设置 boot 文件名
- `/autoboot` - 扫描 mods 文件夹并生成 auto.boot 文件
- `/exit` - 优雅地关闭 Neko-Hub

#### 广播域系统 (`net.lemoncookie.neko.modloader.broadcast`)

广播域系统用于模组之间的通信，支持多种域类型和权限控制。

**域类型：**
- **公开公共域** (`isPrivate=false, isPublic=true`)：所有模组（除 level=3 外）都有权限监听和发送
- **公开私有域** (`isPrivate=true, isPublic=true`)：需要获取权限才能监听和发送
- **私有域** (`isPrivate=true, isPublic=false`)：只能由所有者访问

**权限等级：**
- **SUPER_ADMIN (level 0)**：超级管理员，拥有所有域的权限
- **SYSTEM_COMPONENT (level 1)**：系统级组件，拥有大多数域的权限
- **NORMAL_COMPONENT (level 2)**：正常组件，拥有公共域和自身私有域的权限（默认）
- **RESTRICTED_COMPONENT (level 3)**：限权组件，仅拥有监听权限

```java
// 广播域管理器
public class BroadcastManager {
    // 系统广播域
    public static final String HUB_ALL = "Hub.ALL";         // 公开公共域
    public static final String HUB_SYSTEM = "Hub.System";   // 公开私有域
    public static final String HUB_CONSOLE = "Hub.Console"; // 公开公共域（控制台）
    
    // 错误码
    public static final int ERROR_SUCCESS = 0;              // 操作成功
    public static final int ERROR_PERMISSION_DENIED = 502;  // 权限不足
    public static final int ERROR_DOMAIN_NOT_FOUND = 404;   // 域不存在
    public static final int ERROR_DOMAIN_EXISTS = 402;      // 域已存在
    
    // 构造函数
    public BroadcastManager(ModLoader modLoader)
    
    // 广播域管理
    public int addDomain(String name, boolean isPrivate, boolean isPublic, String ownerModId)
    public BroadcastDomain getDomain(String name)
    public int removeDomain(String name, String modId)
    public int createSystemDomain(String ownerModId)        // 创建系统域
    public int createConsoleDomain(String ownerModId)       // 创建控制台域
    
    // 广播功能
    public int broadcast(String domainName, String message, String senderModId)
    public int listen(String domainName, MessageListener listener, String modId, String modName)
    public int listenPrivate(String modId, MessageListener listener)
    
    // 权限管理
    public int requestDomainPermission(String domainName, String modId, String modName)
    public int requestPermissionUpgrade(String modId, String modName, int targetLevel)
    public PermissionManager getPermissionManager()
    
    // 其他方法
    public Map<String, BroadcastDomain> getDomains()
    public int getDomainCount()
    public boolean hasDomain(String name)
}

// 广播域类
public class BroadcastDomain {
    public String getName()
    public boolean isPrivate()
    public boolean isPublic()
    public String getOwnerModId()
    public boolean addListener(MessageListener listener, String modId)
    public boolean removeListener(MessageListener listener)
    public void broadcast(String message, String senderModId)
}

// 消息监听器接口
public interface MessageListener {
    void onMessageReceived(String domain, String message, String senderModId);
}

// 权限枚举
public enum ModPermission {
    SUPER_ADMIN(0, "超级管理员"),
    SYSTEM_COMPONENT(1, "系统级组件"),
    NORMAL_COMPONENT(2, "正常组件"),
    RESTRICTED_COMPONENT(3, "限权组件");
    
    public int getLevel()
    public String getDisplayName()
    public static ModPermission fromLevel(int level)
}

// 权限管理器
public class PermissionManager {
    public ModPermission getModPermission(String modId)
    public void setModPermission(String modId, ModPermission permission)
    public boolean hasPermission(String modId, ModPermission requiredPermission)
    public boolean hasLevelPermission(String modId, int requiredLevel)
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
class KotlinModLibrary {
    fun register(name: String, component: Any)
    operator fun <T> get(name: String): T?
    operator fun contains(name: String): Boolean
    fun getRegisteredNames(): Set<String>
    inline fun <reified T> register(name: String, noinline init: () -> T): T
    fun registerAll(vararg pairs: Pair<String, Any>)
}

inline fun kotlinModLibrary(block: KotlinModLibrary.() -> Unit): KotlinModLibrary
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

### 模组加载器行为

#### 启动流程

1. **初始化**：创建 ModLoader 实例并调用 initialize() 方法
2. **创建 mods 文件夹**：如果不存在自动创建
3. **加载控制台模组**：优先加载控制台模组（默认权限 SUPER_ADMIN）
4. **加载 boot 文件**：
   - 默认加载 `auto.boot` 文件
   - 如果 `auto.boot` 不存在，自动创建并执行
   - 如果用户指定的 boot 文件不存在，显示错误但不创建
5. **启动控制台交互**：启动交互式控制台

#### Boot 文件系统

Boot 文件是命令序列文件，在模组加载器初始化时执行：

- **默认文件名**：`auto.boot`
- **首次启动**：自动创建 `auto.boot` 文件
- **自定义 boot 文件**：通过 `/set bootfile [文件名]` 设置
- **生成 auto.boot**：通过 `/autoboot` 命令扫描 mods 文件夹生成

#### 权限系统

模组权限系统控制模组对广播域的访问：

- **默认权限**：普通模组默认为 NORMAL_COMPONENT (level 2)
- **控制台模组**：默认为 SUPER_ADMIN (level 0)
- **设置权限**：通过 `/set modPermission [模组名] [level 值]` 设置
- **权限持久化**：权限配置保存在 `neko-hub.config` 文件中

#### 终端模式

Neko-Hub 支持 CLI 模式，通过控制台与用户交互。控制台系统支持：

- **彩色输出**：使用 ANSI 颜色代码，支持红色（错误）、黄色（警告）、绿色（成功）、蓝色（信息）等
- **交互式命令**：用户可以输入命令与系统交互
- **广播域消息**：控制台监听 Hub.Console 域，显示所有模组发送的消息
- **跨平台支持**：在 Windows 和 Unix-like 系统上都能正常工作
- **UTF-8 编码**：支持中文等非 ASCII 字符

#### 命令系统运行原理

命令系统的工作流程：

1. **命令注册**：系统启动时注册内置命令，模组也可以注册自定义命令
2. **命令解析**：解析用户输入，提取命令名和参数（支持引号包裹的参数）
3. **命令执行**：找到对应的命令实现并执行
4. **错误处理**：捕获并显示命令执行过程中的错误

#### 广播域系统使用

广播域系统允许模组之间进行通信：

- **Hub.ALL**：公开公共域，所有模组（除 level=3 外）都可以监听和发送
- **Hub.System**：公开私有域，需要权限等级 1 或更低，并且需要用户确认获取权限
- **Hub.Console**：公开公共域，控制台模组创建，用于显示消息
- **私有域**：格式为 `Hub.[modId]`，只能由所有者访问
- **公开私有域**：需要获取权限才能监听和发送

#### 语言系统使用

语言系统支持多语言：

- **默认语言**：中文（zh）
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
import net.lemoncookie.neko.modloader.lib.kotlinModLibrary

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
val lib = kotlinModLibrary {
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
