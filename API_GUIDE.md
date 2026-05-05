# Neko-Hub API 开发指南

## 项目概述

Neko-Hub 是一个模组加载器核心，目前支持 CLI 模式。项目采用模块化设计，**基于 Java 21**，便于扩展和维护。

## 技术栈

- **Java**: 21 (LTS)
- **Kotlin**: 2.3.10
- **构建工具**: Gradle 9.4.0

## 当前版本

- **ModLoader 版本**: 3.2.4
- **最低 API 版本**: 2.3.0

## 模块结构

### ModLoader 模块 (`net.lemoncookie.neko.modloader`)

模组加载器核心模块，提供 **Java** 和 **Kotlin** API。

#### 目录结构

```
modloader/
├── src/main/java/net/lemoncookie/neko/modloader/
│   ├── api/
│   │   ├── IModAPI.java          # Java 版 API 接口
│   │   ├── ModAPI.java           # Java 版 API 工具类
│   │   └── ModDependency.java    # 依赖类
│   ├── boot/
│   │   └── BootFileManager.java  # Boot 文件管理器
│   ├── broadcast/
│   │   ├── BroadcastDomain.java
│   │   ├── BroadcastManager.java
│   │   ├── MessageListener.java
│   │   ├── ModPermission.java    # 权限枚举
│   │   └── PermissionManager.java
│   ├── command/
│   │   ├── BaseCommandListener.java
│   │   ├── CommandMessage.java
│   │   ├── SetCommand.java
│   │   ├── ChangeCommand.java
│   │   ├── HelpCommand.java
│   │   ├── ClearCommand.java
│   │   ├── LoadCommand.java
│   │   ├── UnloadCommand.java
│   │   ├── ListCommand.java
│   │   ├── ExitCommand.java
│   │   ├── SayCommand.java
│   │   ├── ListenCommand.java
│   │   └── AutobootCommand.java
│   ├── console/
│   │   └── Console.java
│   ├── consolemod/
│   │   └── ConsoleMod.java
│   ├── core/
│   │   └── ModCore.java
│   ├── lang/
│   │   └── LanguageManager.java
│   ├── lib/
│   │   └── ModLibrary.java
│   ├── logging/
│   │   └── SimpleLogger.java
│   ├── systemmod/
│   │   └── SystemMod.java
│   ├── util/
│   │   └── VersionComparator.java
│   └── ModLoader.java            # 主入口类
└── src/main/kotlin/net/lemoncookie/neko/modloader/
    ├── api/
    │   └── KModAPI.kt            # Kotlin 版 API 接口
    └── lib/
        └── KotlinModLibrary.kt   # Kotlin 版库支持
```

---

## 核心组件

### 主入口类 ModLoader

```java
public class ModLoader {
    private static final String VERSION = "3.2.4";
    private static final String MIN_API_VERSION = "2.3.0";
    
    public ModLoader()
    public void initialize()
    
    // 模组注册
    public void registerJavaMod(IModAPI mod)
    public void registerKotlinMod(KModAPI mod)
    
    // 模组卸载
    public void unloadAll()
    public boolean unloadMod(String modName)
    
    // 获取组件
    public ModCore getCore()
    public Console getConsole()
    public BroadcastManager getBroadcastManager()
    public LanguageManager getLanguageManager()
    public BootFileManager getBootFileManager()
    public SimpleLogger getSimpleLogger()
    public ModLibrary getJavaLibrary()
    
    // 获取模组列表
    public List<IModAPI> getJavaMods()
    public List<KModAPI> getKotlinMods()
    
    // 静态方法
    public static String getVersion()
    public static String getMinApiVersion()
    public boolean isInitialized()
    
    // 主方法
    public static void main(String[] args)
}
```

---

### 控制台系统 Console

控制台系统负责显示信息和处理用户输入，支持彩色输出。

```java
public class Console {
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
    
    // 输入框控制
    public void setShowInputBox(boolean show)
    public boolean isShowInputBox()
    
    // 其他方法
    public void clear()
    public String readLine() throws IOException
    public boolean readConfirmation() throws IOException
    public void close()
}
```

**使用示例**：
```java
// 显示消息
modLoader.getConsole().printSuccess("操作成功");

// 隐藏输入框（适合服务器环境）
modLoader.getConsole().setShowInputBox(false);
```

---

### 广播域系统 BroadcastManager

广播域系统用于模组之间的通信，支持多种域类型和权限控制。

**系统广播域常量**：
```java
public static final String HUB_ALL = "Hub.ALL";         // 公开公共域
public static final String HUB_SYSTEM = "Hub.System";   // 公开公共域（日志专用）
public static final String HUB_CONSOLE = "Hub.Console"; // 公开公共域（控制台）
public static final String HUB_COMMAND = "Hub.Command"; // 公开公共域（命令）
```

**错误码**：
```java
public static final int ERROR_SUCCESS = 0;              // 操作成功
public static final int ERROR_PERMISSION_DENIED = 502;  // 权限不足
public static final int ERROR_DOMAIN_NOT_FOUND = 404;   // 域不存在
public static final int ERROR_DOMAIN_EXISTS = 402;      // 域已存在
```

**主要方法**：
```java
public class BroadcastManager {
    public BroadcastManager(ModLoader modLoader)
    
    // 广播域管理
    public int addDomain(String name, boolean isPrivate, boolean isPublic, String ownerModId)
    public BroadcastDomain getDomain(String name)
    public int removeDomain(String name, String modId)
    public int createConsoleDomain(String ownerModId)
    
    // 广播功能
    public int broadcast(String domainName, String message, String senderModId)
    public int listen(String domainName, MessageListener listener, String modId, String modName)
    public int listenPrivate(String modId, MessageListener listener)
    public int unlisten(String domainName, MessageListener listener)
    
    // 权限管理
    public int requestDomainPermission(String domainName, String modId, String modName)
    public int requestPermissionUpgrade(String modId, String modName, int targetLevel)
    public PermissionManager getPermissionManager()
    
    // 其他方法
    public Map<String, BroadcastDomain> getDomains()
    public int getDomainCount()
    public boolean hasDomain(String name)
    public boolean hasPermissionToSend(String domainName, String modId)
    public boolean hasPermissionToListen(String domainName, String modId)
}
```

**域类型**：
- **公开公共域** (`isPrivate=false, isPublic=true`)：所有模组（除 level=3 外）都有权限监听和发送
- **公开私有域** (`isPrivate=true, isPublic=true`)：需要获取权限才能监听和发送
- **私有域** (`isPrivate=true, isPublic=false`)：只能由所有者访问

**域名命名规范**：
- **系统域**：以 `Hub.` 开头（如 `Hub.ALL`、`Hub.System`、`Hub.Console`、`Hub.Command`）
- **私有域**：默认格式为 `Hub.[modId]`（通过 `listenPrivate` 方法自动创建）
- **自定义域**：模组可以自由创建任意名称的广播域，不强制要求以 `Hub.` 开头
  - 例如：`Maomao.KeAi`、`com.example.neko`、`MyMod.Chat` 等都是有效的域名
  - 域名只是字符串键，功能上没有限制

**权限等级**：
| 等级 | 名称 | 描述 |
|------|------|------|
| 0 | SUPER_ADMIN | 超级管理员，拥有所有域的权限 |
| 1 | SYSTEM_COMPONENT | 系统级组件，拥有大多数域的权限 |
| 2 | NORMAL_COMPONENT | 正常组件，拥有公共域和自身私有域的权限（默认） |
| 3 | RESTRICTED_COMPONENT | 限权组件，仅拥有监听权限 |

---

### 权限枚举 ModPermission

```java
public enum ModPermission {
    SUPER_ADMIN(0, "超级管理员"),
    SYSTEM_COMPONENT(1, "系统级组件"),
    NORMAL_COMPONENT(2, "正常组件"),
    RESTRICTED_COMPONENT(3, "限权组件");
    
    public int getLevel()
    public String getDisplayName()
    public static ModPermission fromLevel(int level)
}
```

---

### 消息监听器接口 MessageListener

```java
public interface MessageListener {
    void onMessageReceived(String domain, String message, String senderModId);
}
```

---

### 命令系统

**命令消息类 CommandMessage**：
```java
public class CommandMessage {
    public CommandMessage(String command, String[] parts, String senderModId)
    public static CommandMessage fromJson(String json)
    public String toJson()
    public String getCommand()
    public String[] getParts()
    public int getPartCount()
    public String getPart(int index)
    public String getSenderModId()
    public String getPartsAsString()
}
```

**命令监听器基类 BaseCommandListener**：
```java
public abstract class BaseCommandListener implements MessageListener {
    protected final ModLoader modLoader;
    protected final String commandName;
    
    public BaseCommandListener(ModLoader modLoader, String commandName)
    protected abstract void execute(CommandMessage commandMessage, String senderModId)
}
```

**内置命令**：
- `/set modpermission [模组名] [level]` - 设置模组权限（0-3）
- `/set bootfile [文件名]` - 设置 boot 文件名
- `/set language [en/zh]` - 切换语言
- `/change bootfile [文件名]` - 切换并执行 boot 文件
- `/change InputBoxView [true/false]` - 显示/隐藏输入框
- `/help` - 显示可用命令
- `/clear` - 清空控制台
- `/load [模组文件名]` - 加载模组
- `/unload [模组名称]` - 卸载模组
- `/list mod [页码]` - 列出已加载的模组
- `/exit` - 退出应用
- `/say [域名] "消息内容"` - 向特定广播域发送消息
- `/listen [域名] [start|stop]` - 监听/取消监听特定广播域
- `/autoboot` - 扫描 mods 文件夹并生成 auto.boot 文件

---

### Boot 文件系统 BootFileManager

```java
public class BootFileManager {
    public BootFileManager(ModLoader modLoader)
    
    // Boot 文件操作
    public List<String> readBootFile(String fileName)
    public boolean executeBootFile(String fileName)
    public void generateAutoBoot()
    public void setCurrentBootFile(String fileName)
    public String getCurrentBootFile()
    public boolean executeCurrentBootFile()
    public void switchBootFileAndExecute(String fileName)
    
    // 命令插入
    public void insertCommandAtHead(String command)
    public void insertCommandAtHeadWithReplace(String command, String commandPrefix)
    public void insertCommandAtTail(String command)
}
```

**Boot 文件语法**：
- 每行一个命令
- 支持 `/load`、`/unload`、`/set` 等所有命令
- 以 `#` 开头的行为注释
- 默认文件名为 `auto.boot`

---

### 语言管理器 LanguageManager

```java
public class LanguageManager {
    public LanguageManager()
    public void loadLanguage(String lang)
    public String getMessage(String key, Object... args)
    public boolean hasMessage(String key)
}
```

---

### 日志系统 SimpleLogger

```java
public class SimpleLogger implements MessageListener {
    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG
    }
    
    public SimpleLogger(ModLoader modLoader)
    public void setDomainLogLevel(String domain, LogLevel level)
    public void close()
}
```

---

### 库支持 ModLibrary

```java
public class ModLibrary {
    public void setModLoader(ModLoader modLoader)
    public void register(String name, Object component)
    public <T> T get(String name)
    public boolean has(String name)
    public Set<String> getRegisteredNames()
}
```

---

## Java API

### 接口 IModAPI

Java 模组需要实现 `IModAPI` 接口：

```java
public interface IModAPI {
    String getModId();
    String getVersion();
    default String getApiVersion() { return getVersion(); }
    default String getName() { return getModId(); }
    String getPackageName();
    default List<ModDependency> getDependencies() { return Collections.emptyList(); }
    void onLoad(ModLoader modLoader);
    void onUnload();
    default void registerBroadcastListeners(ModLoader modLoader, String modId) {}
}
```

**重要说明**：
- `getApiVersion()` 必须返回模组使用的 API 版本
- 未声明 API 版本的模组将被拒绝加载
- API 版本与模组版本相同时会发出警告

### 工具类 ModAPI

`ModAPI` 工具类提供便捷的 API 访问方法：

```java
public class ModAPI {
    public ModAPI(ModLoader modLoader, String modId)
    
    // 广播系统
    public void broadcast(String domain, String message)
    public void broadcastAll(String message)           // Hub.ALL
    public void broadcastConsole(String message)       // Hub.Console
    public void broadcastLog(String message)           // Hub.System
    public void listen(String domain, MessageListener listener)
    
    // 域管理
    public int createDomain(String name, boolean isPrivate, boolean isPublic)
    public int createPrivateDomain(String name)
    public int createPublicDomain(String name)
    
    // 权限管理
    public int requestPermissionUpgrade(int targetLevel)
    public int requestSystemPermission()
    public int requestAdminPermission()
    
    // 控制台输出
    public void print(String text)
    public void printError(String text)
    public void printWarning(String text)
    public void printSuccess(String text)
    public void printInfo(String text)
    
    // 快捷访问
    public String getModId()
    public String getModName()
    public ModLoader getModLoader()
    public BroadcastManager getBroadcastManager()
    public Console getConsole()
}
```

### 依赖类 ModDependency

```java
public class ModDependency {
    public ModDependency(String modId, String minVersion)
    public String getModId()
    public String getMinVersion()
}
```

### Java 模组示例

```java
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.command.BaseCommandListener;
import net.lemoncookie.neko.modloader.command.CommandMessage;

import java.util.List;

public class MyJavaMod implements IModAPI {
    
    private ModAPI api;
    
    @Override
    public String getModId() { return "my-java-mod"; }
    
    @Override
    public String getVersion() { return "1.0.0"; }
    
    @Override
    public String getApiVersion() { return "2.3.0"; }
    
    @Override
    public String getName() { return "My Java Mod"; }
    
    @Override
    public String getPackageName() { return "com.example.myjavamod"; }
    
    @Override
    public List<ModDependency> getDependencies() {
        return List.of(new ModDependency("console-mod", "1.0.0"));
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        api = new ModAPI(modLoader, getModId());
        api.printSuccess("Java mod loaded!");
        api.broadcastLog("[INFO] MyJavaMod initialized");
    }
    
    @Override
    public void onUnload() {
        api.printWarning("Java mod unloading...");
    }
    
    @Override
    public void registerBroadcastListeners(ModLoader modLoader, String modId) {
        // 注册自定义命令
        modLoader.getBroadcastManager().listen(
            BroadcastManager.HUB_COMMAND,
            new BaseCommandListener(modLoader, "mycommand") {
                @Override
                protected void execute(CommandMessage commandMessage, String senderModId) {
                    modLoader.getConsole().printLine("Hello from my command! Args: " + commandMessage.getPartsAsString());
                }
            },
            modId,
            getName()
        );
    }
}
```

---

## Kotlin API

### 接口 KModAPI

```kotlin
interface KModAPI {
    val modId: String
    val version: String
    val apiVersion: String
        get() = version
    val name: String
        get() = modId
    val packageName: String
    val dependencies: List<ModDependency>
        get() = emptyList()
    
    fun onLoad(modLoader: ModLoader)
    fun onUnload()
    fun registerBroadcastListeners(modLoader: ModLoader, modId: String) {}
    fun getInfo(): ModInfo
}

data class ModInfo(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: String
)
```

### Kotlin 模组示例

```kotlin
import net.lemoncookie.neko.modloader.api.KModAPI
import net.lemoncookie.neko.modloader.api.ModDependency
import net.lemoncookie.neko.modloader.ModLoader

class MyKotlinMod : KModAPI {
    override val modId = "my-kotlin-mod"
    override val version = "1.0.0"
    override val apiVersion = "2.3.0"
    override val name = "My Kotlin Mod"
    override val packageName = "com.example.mykotlinmod"
    override val dependencies = listOf(
        ModDependency("console-mod", "1.0.0")
    )
    
    override fun onLoad(modLoader: ModLoader) {
        modLoader.console.printSuccess("Kotlin mod loaded!")
    }
    
    override fun onUnload() {
        println("Kotlin mod unloaded!")
    }
}
```

### Kotlin 库支持

```kotlin
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

---

## 模组加载器行为

### 启动流程

1. **初始化**：创建 ModLoader 实例并调用 `initialize()` 方法
2. **创建 mods 文件夹**：如果不存在自动创建
3. **创建系统域**：创建 Hub.ALL 和 Hub.System 域
4. **加载系统模组**：优先加载 SystemMod（权限 SUPER_ADMIN）
5. **加载控制台模组**：优先加载 ConsoleMod（权限 SUPER_ADMIN）
6. **注册内置命令监听器**：注册所有内置命令到 Hub.Command 域
7. **加载 boot 文件**：默认加载 `auto.boot` 文件
8. **启动控制台交互**：启动交互式控制台

### 权限系统

- **默认权限**：普通模组默认为 NORMAL_COMPONENT (level 2)
- **系统模组**：SystemMod 和 ConsoleMod 默认为 SUPER_ADMIN (level 0)
- **设置权限**：通过 `/set modpermission [模组名] [level]` 设置
- **权限持久化**：权限配置保存在 boot 文件中

### API 版本检查

模组必须显式声明 API 版本：
- 未声明 API 版本的模组将被拒绝加载
- API 版本低于最低版本将拒绝加载
- API 版本与模组版本相同时会发出警告

---

## JAR 文件清单配置

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

## 环境要求

- JDK 21 或更高版本
- Gradle 9.4.0

## 构建项目

```bash
./gradlew build
```

## 运行项目

```bash
./gradlew :modloader:run
```
