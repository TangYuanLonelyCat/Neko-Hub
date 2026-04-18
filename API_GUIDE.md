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

控制台系统负责显示信息和处理用户输入，支持彩色输出。

**重要说明**：Console 类的打印方法**不会自动广播**消息。如果需要同时记录日志和显示消息，应该：
1. 调用 `console.printXXX()` 方法显示（带颜色）
2. 手动调用 `broadcastManager.broadcast("Hub.Log", ...)` 记录日志

对于模组间通信消息，应广播到 `Hub.Console`，由 ConsoleMod 统一显示（带颜色和发送者前缀）。

```java
// 控制台类
public class Console {
    // 构造函数
    public Console(ModLoader modLoader)
    
    // 打印方法
    public void printLine(String text)
    public void printLine()
    public void print(String text)
    
    // 彩色输出方法（不自动广播）
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

**使用示例**：
```java
// 仅显示（带颜色），不记录日志
modLoader.getConsole().printSuccess("操作成功");

// 显示并记录日志
String msg = "模组加载成功";
modLoader.getConsole().printSuccess(msg);
modLoader.getBroadcastManager().broadcast("Hub.Log", "[SUCCESS] " + msg, "ModLoader");

// 模组间通信（广播到 Hub.Console，由 ConsoleMod 统一显示）
modLoader.getBroadcastManager().broadcast("Hub.Console", "Hello from my mod!", "MyMod");
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

**重要变更（v3.0.0）**：
- 命令系统已重构，不再使用命令注册机制
- 所有命令改为监听 `Hub.Command` 广播域
- 用户输入 `/` 开头的命令时，会序列化为 JSON 发送到 `Hub.Command` 域
- 模组可以通过监听 `Hub.Command` 域来实现自定义命令

**命令消息格式**：
```json
{
    "command": "命令名",
    "parts": ["参数 1", "参数 2", ...],
    "sender": "发送者 ID"
}
```

```java
// 命令消息类
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

// 命令监听器基类
public abstract class BaseCommandListener implements MessageListener {
    public BaseCommandListener(ModLoader modLoader, String commandName)
    protected abstract void execute(CommandMessage commandMessage, String senderModId)
}
```

**内置命令：**
- `/help` - 显示可用命令
- `/clear` - 清空控制台
- `/load [模组文件名]` - 加载模组（通过文件名，会自动添加.jar 后缀）
- `/unload [模组名称]` - 卸载模组（通过模组名称/ID）
- `/set modpermission [模组名] [level 值]` - 设置模组权限（level 0-3）
- `/set bootfile [文件名]` - 设置 boot 文件名
- `/set language [en/zh]` - 切换语言
- `/autoboot` - 扫描 mods 文件夹并生成 auto.boot 文件
- `/exit` - 优雅地关闭 Neko-Hub
- `/say [域名] "消息内容"` - 向特定广播域发送消息
- `/listen [域名] [start|stop]` - 监听/取消监听特定广播域
- `/list mod [页码]` - 列出已加载的模组

**非命令输入：**
- 不带 `/` 的输入会直接发送到 `Hub.Console` 广播域

**模组自定义命令：**

模组可以通过监听 `Hub.Command` 广播域来实现自定义命令：

```java
// Java 模组示例
@Override
public void registerBroadcastListeners(ModLoader modLoader, String modId) {
    modLoader.getBroadcastManager().listen(
        BroadcastManager.HUB_COMMAND, 
        new BaseCommandListener(modLoader, "mycommand") {
            @Override
            protected void execute(CommandMessage commandMessage, String senderModId) {
                // 命令执行逻辑
                modLoader.getConsole().printLine("Hello from my command! Args: " + commandMessage.getPartsAsString());
            }
        }, 
        modId, 
        "MyMod"
    );
}
```

```kotlin
// Kotlin 模组示例
override fun registerBroadcastListeners(modLoader: ModLoader, modId: String) {
    modLoader.broadcastManager.listen(
        BroadcastManager.HUB_COMMAND,
        object : BaseCommandListener(modLoader, "mycommand") {
            override fun execute(commandMessage: CommandMessage, senderModId: String) {
                // 命令执行逻辑
                modLoader.console.printLine("Hello from my command! Args: ${commandMessage.partsAsString}")
            }
        },
        modId,
        "MyMod"
    )
}
```

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
    public static final String HUB_LOG = "Hub.Log";         // 公开公共域（日志专用）
    
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

Java 模组需要实现 `IModAPI` 接口。建议使用 `ModAPI` 工具类来简化开发。

**基础接口**：
```java
public interface IModAPI {
    String getModId();
    String getVersion();
    String getPackageName();
    void onLoad(ModLoader modLoader);
    void onUnload();
    default void registerBroadcastListeners(ModLoader modLoader, String modId) {}
}
```

**重要变更（v3.0.0）**：
- 删除了 `registerCommands` 方法，命令系统改为基于广播域
- 模组通过监听 `Hub.Command` 广播域来实现自定义命令
- `onLoad` 方法不再接收 `modId` 参数

**使用 ModAPI 工具类**：
```java
public class MyMod extends ModAPI implements IModAPI {
    
    public MyMod() {
        super(null, null); // 会在 onLoad 中初始化
    }
    
    @Override
    public void onLoad(ModLoader modLoader, String modId) {
        // 初始化 API 工具类
        this.modLoader = modLoader;
        this.modId = modId;
        
        // 注册命令（不允许覆盖系统命令）
        registerCommand("hello", new Command() {
            @Override
            public void execute(ModLoader modLoader, String args) {
                printSuccess("Hello from " + getModName() + "!");
            }
            
            @Override
            public String getDescription() {
                return "Say hello";
            }
            
            @Override
            public String getUsage() {
                return "/hello";
            }
        }, false);
        
        // 广播消息
        broadcastConsole("Mod loaded successfully!");
        broadcastLog("Initialization complete");
        broadcastAll("Hello from all mods!");
        
        // 监听广播
        listen("Hub.ALL", (domain, message, senderModId) -> {
            printInfo("Received: " + message);
        });
        
        // 创建私有域
        createPrivateDomain("Hub." + getModId());
    }
    
    @Override
    public void onUnload() {
        printWarning("Mod is unloading...");
    }
}
```

**ModAPI 工具类提供的方法**：

```java
// 命令注册
public boolean registerCommand(String name, Command command, boolean allowOverride)
public void registerCommand(String name, Command command)

// 广播系统
public void broadcast(String domain, String message)
public void broadcastAll(String message)           // Hub.ALL
public void broadcastConsole(String message)       // Hub.Console
public void broadcastLog(String message)           // Hub.Log
public void listen(String domain, MessageListener listener)

// 域管理
public int createDomain(String name, boolean isPrivate, boolean isPublic)
public int createPrivateDomain(String name)
public int createPublicDomain(String name)

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
public CommandSystem getCommandSystem()
public Console getConsole()
```

**Java 版库支持**：
```java
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

- `FileLabel` - 文件标签管理

### 3. Markdown 模块 (`net.lemoncookie.neko.markdown`)

Markdown 处理模块，提供完整的 Markdown 解析、渲染和导出功能（v2.0.0）。

#### 核心功能

- **GitHub Flavored Markdown (GFM) 支持**
  - 任务列表 (Task Lists)
  - 删除线 (Strikethrough)
  - 表格 (Tables)

- **代码语法高亮** - 使用 Highlight.js
- **数学公式支持** - 使用 KaTeX/LaTeX
- **自动生成目录 (TOC)** - 提取 h1-h6 标题
- **主题切换** - 浅色/深色模式
- **图片相对路径解析** - 自动处理本地图片路径
- **导出功能** - HTML/PDF 导出
- **配置系统** - 用户自定义选项

#### 目录结构

```
markdown/
├── src/main/kotlin/net/lemoncookie/neko/markdown/
│   ├── Markdown.kt                    # 主类，解析核心
│   ├── config/
│   │   └── MarkdownConfig.kt          # 配置管理
│   ├── export/
│   │   └── MarkdownExporter.kt        # 导出功能
│   └── javafx/
│       └── MarkdownRenderer.kt        # JavaFX 渲染器
└── src/main/resources/lang/
    ├── en.json                        # 英文资源
    └── zh.json                        # 中文资源
```

#### 主要 API

```kotlin
// Markdown 主类
class Markdown : IModAPI {
    val config: MarkdownConfig
    
    // 解析 Markdown 为 HTML
    fun parse(markdown: String): String
    
    // 从文件读取并解析
    fun parseFile(filePath: String): String?
    
    // 生成带目录的 HTML
    fun generateWithToc(htmlContent: String, title: String = "", generateToc: Boolean = true): String
}

// 配置管理
class MarkdownConfig {
    var syntaxHighlightEnabled: Boolean
    var mathSupportEnabled: Boolean
    var autoTocEnabled: Boolean
    var imageRelativePathEnabled: Boolean
    var theme: Theme
    
    enum class Theme { LIGHT, DARK, SYSTEM }
    
    fun load(baseDir: File)
    fun save()
    fun update(syntaxHighlight: Boolean? = null, ...)
}

// JavaFX 渲染器
class MarkdownRenderer(
    private val markdown: Markdown, 
    private val modLoader: ModLoader,
    private val basePath: String? = null
) {
    fun createWebView(initialMarkdown: String? = null): WebView
    fun updateContent(markdownText: String)
    fun loadFromFile(filePath: String): Boolean
    fun setTheme(theme: String)
    fun exportToHtml(outputPath: String): Boolean
    fun exportToPdf(outputPath: String): Boolean
}

// 导出器
class MarkdownExporter(private val webView: WebView) {
    fun exportToHtml(content: String, outputPath: String): Boolean
    fun exportToPdf(outputPath: String): Boolean
    fun generateFullHtml(bodyContent: String, ...): String
}
```

#### 使用示例

```kotlin
// 获取 Markdown 模块实例
val markdown = modLoader.getModule<Markdown>("markdown")

// 解析 Markdown 文本
val html = markdown.parse("# Hello\n\n**World**!")

// 从文件加载
val fileHtml = markdown.parseFile("README.md")

// 创建渲染器
val renderer = MarkdownRenderer(markdown, modLoader, "/path/to/markdown/dir")

// 创建 JavaFX 场景
val scene = renderer.createScene(800.0, 600.0)

// 切换主题
renderer.setTheme("dark")

// 导出为 HTML
renderer.exportToHtml("output.html")

// 导出为 PDF
renderer.exportToPdf("output.pdf")

// 修改配置
markdown.config.update(
    syntaxHighlight = true,
    mathSupport = true,
    autoToc = true,
    theme = MarkdownConfig.Theme.DARK
)
```

#### 配置文件

配置文件位于 `~/.neko-hub/markdown/config.properties`：

```properties
# 启用语法高亮
syntax.highlight=true

# 启用数学公式支持
math.support=true

# 自动生成目录
auto.toc=true

# 启用图片相对路径解析
image.relative.path=true

# 主题选择：LIGHT, DARK, SYSTEM
theme=SYSTEM
```

#### GFM 扩展语法

**任务列表：**
```markdown
- [x] 已完成的任务
- [ ] 未完成的任务
```

**删除线：**
```markdown
~~已删除的内容~~
```

**表格：**
```markdown
| 列 1 | 列 2 |
|------|------|
| 内容 | 内容 |
```

#### 数学公式支持

使用 LaTeX 语法：

- 行内公式：`$E = mc^2$`
- 块级公式：`$$\sum_{i=1}^{n} x_i$$`

#### 依赖项

- `org.commonmark:commonmark` - Markdown 解析核心
- `org.commonmark:commonmark-ext-gfm-tables` - 表格扩展
- `org.commonmark:commonmark-ext-gfm-strikethrough` - 删除线扩展
- `org.commonmark:commonmark-ext-task-list-items` - 任务列表扩展
- `Highlight.js` (CDN) - 代码高亮
- `KaTeX` (CDN) - 数学公式渲染

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
- **Hub.Console**：公开公共域，控制台模组创建，用于模组间通信消息显示
- **Hub.Log**：公开公共域，日志系统专用，用于记录日志（不显示在控制台）
- **私有域**：格式为 `Hub.[modId]`，只能由所有者访问
- **公开私有域**：需要获取权限才能监听和发送

**消息发送最佳实践**：
- **模组间通信**：广播到 `Hub.Console`，由 ConsoleMod 统一显示（带颜色和发送者前缀）
- **日志记录**：广播到 `Hub.Log`，仅记录到日志文件，不显示在控制台
- **系统消息**：直接调用 `console.printXXX()` 显示，同时广播到 `Hub.Log` 记录日志

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

### 3. Markdown 模块 (`net.lemoncookie.neko.markdown`)

Markdown 处理模块，支持 Markdown 解析和 JavaFX 渲染。

#### 目录结构

```
markdown/
├── src/main/kotlin/net/lemoncookie/neko/markdown/
│   ├── Markdown.kt                      # Markdown 解析器（实现 IModAPI）
│   ├── config/
│   │   └── MarkdownConfig.kt            # 配置管理器
│   ├── export/
│   │   └── MarkdownExporter.kt          # 导出功能（HTML/PDF）
│   └── javafx/
│       └── MarkdownRenderer.kt          # JavaFX 渲染器
└── src/main/resources/lang/
    ├── zh.json                          # 中文语言文件
    └── en.json                          # 英文语言文件
```

#### Markdown 解析器 (`net.lemoncookie.neko.markdown`)

Markdown 解析器负责将 Markdown 文本转换为 HTML，支持从文件读取内容。

**支持功能：**
- GitHub Flavored Markdown (GFM)：任务列表、删除线、表格
- 代码语法高亮（Highlight.js）
- 数学公式（KaTeX/LaTeX）
- 目录生成 (TOC)
- 主题切换（浅色/深色模式）
- 图片相对路径解析
- 导出为 HTML/PDF
- 配置系统

```kotlin
// Markdown 类
class Markdown : IModAPI {
    // 模组信息
    override fun getModId(): String              // 返回 "markdown"
    override fun getVersion(): String            // 返回 "2.0.0"
    override fun getPackageName(): String        // 返回 "net.lemoncookie.neko.markdown"
    override fun getName(): String               // 返回 "Markdown Module"

    // 生命周期
    override fun onLoad(modLoader: ModLoader, modId: String)
    override fun onUnload()

    // 配置管理器
    val config: MarkdownConfig

    // Markdown 解析
    fun parse(markdown: String): String          // 解析 Markdown 为 HTML（支持 GFM）
    fun parseFile(filePath: String): String?     // 从文件读取并解析
    fun generateWithToc(htmlContent: String, title: String = "", generateToc: Boolean = true): String  // 生成带目录的文档
}
```

**使用示例：**

```kotlin
// 创建 Markdown 实例
val markdown = Markdown()

// 解析 Markdown 文本（支持 GFM）
val html = markdown.parse("# Hello\n\nThis is **bold** text.\n\n- [ ] Task 1\n- [x] Task 2\n\n~~strikethrough~~")

// 从文件解析
val htmlFromFile = markdown.parseFile("path/to/file.md")

// 生成带目录的文档
val withToc = markdown.generateWithToc(html, "My Document")

// 访问配置
val syntaxHighlight = markdown.config.syntaxHighlightEnabled
markdown.config.update(syntaxHighlight = false)
```

#### 配置管理器 (`net.lemoncookie.neko.markdown.config`)

配置管理器负责管理用户自定义选项。

```kotlin
// MarkdownConfig 类
class MarkdownConfig {
    // 配置项
    var syntaxHighlightEnabled: Boolean      // 语法高亮开关
    var mathSupportEnabled: Boolean          // 数学公式支持开关
    var autoTocEnabled: Boolean              // 自动生成目录开关
    var imageRelativePathEnabled: Boolean    // 相对图片路径解析开关
    var theme: Theme                         // 主题（LIGHT/DARK/SYSTEM）
    
    // 方法
    fun load(baseDir: File)                  // 加载配置
    fun save()                               // 保存配置
    fun update(...)                          // 更新配置
}
```

**使用示例：**

```kotlin
// 加载配置
markdown.config.load(File(System.getProperty("user.home"), ".neko-hub"))

// 更新配置
markdown.config.update(
    syntaxHighlight = true,
    mathSupport = true,
    theme = MarkdownConfig.Theme.DARK
)
```

#### JavaFX 渲染器 (`net.lemoncookie.neko.markdown.javafx`)

JavaFX 渲染器使用 WebView 组件显示渲染后的 Markdown 内容。

```kotlin
// MarkdownRenderer 类
class MarkdownRenderer(
    private val markdown: Markdown, 
    private val modLoader: ModLoader,
    private val basePath: String? = null
) {
    // 创建 WebView 组件
    fun createWebView(initialMarkdown: String? = null): WebView

    // 更新内容
    fun updateContent(markdownText: String)

    // 从文件加载
    fun loadFromFile(filePath: String): Boolean

    // 创建完整场景
    fun createScene(width: Double = 800.0, height: Double = 600.0): Scene

    // 获取 WebView
    fun getWebView(): WebView?
    
    // 切换主题
    fun setTheme(theme: String)
    
    // 导出功能
    fun exportToHtml(outputPath: String): Boolean
    fun exportToPdf(outputPath: String): Boolean
    
    // 生成完整 HTML
    fun generateFullHtml(): String
}
```

**使用示例：**

```kotlin
// 创建渲染器
val renderer = MarkdownRenderer(markdown, modLoader, basePath = "/path/to/markdown/files")

// 创建 WebView 并显示
val webView = renderer.createWebView("# Hello World")

// 更新内容
renderer.updateContent("## New Content\n\nUpdated text.")

// 从文件加载
renderer.loadFromFile("document.md")

// 切换主题
renderer.setTheme("dark")

// 导出为 HTML
renderer.exportToHtml("/path/to/output.html")

// 导出为 PDF
renderer.exportToPdf("/path/to/output.pdf")
```

**样式特性：**
- 响应式布局，最大宽度 900px
- GitHub Flavored Markdown 支持（任务列表、删除线、表格）
- 代码语法高亮（Highlight.js，支持浅色/深色主题）
- 数学公式渲染（KaTeX）
- 自动生成目录（TOC）
- 主题切换（浅色/深色模式）
- 图片相对路径自动解析
- 引用块左侧边框
- 链接悬停效果
- 图片自适应宽度
- 国际化支持（中文和英文）

### 4. FileLabel 模块 (`net.lemoncookie.neko.filelabel`)

文件标签模块，支持为文件添加标签管理。

- `FileLabel` - 文件标签管理

### 5. Calendar 模块 (`net.lemoncookie.neko.calendar`)
