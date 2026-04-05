# Neko-Hub API Development Guide

## Project Overview

Neko-Hub is a multi-functional project that currently supports CLI mode and will support GUI mode in the future. The project adopts a modular design, **based on Java 21**, for easy extension and maintenance.

## Important Changes (v1.1.0)

**API Updates:**
- `registerCommands` and `registerBroadcastListeners` methods now receive a `modId` parameter (automatically passed)
- Command registration now requires: `modLoader.getCommandSystem().registerCommand("commandName", modId, command, allowOverride)`
- Multiple mods can register the same command, use `--modName` suffix to specify execution source
- **Command Priority:** system implementation > specified mod > single implementation > error message
- Added `ModAPI` utility class to simplify mod development (wraps common APIs)
- Kotlin API renamed from `ModAPI` to `KModAPI` to avoid naming conflicts

**Console and Logging:**
- Console class print methods **NO LONGER auto-broadcast** messages
- Separated display and logging concerns:
  - Call `console.printXXX()` for colored display
  - Broadcast to `Hub.Log` for logging (not displayed in console)
- Inter-mod communication: broadcast to `Hub.Console`, displayed by ConsoleMod

**Exception Handling:**
- All `catch(Exception)` changed to `catch(Throwable)` for better stability
- Ensures the loader won't crash from any errors

**Command System:**
- System commands use `allowOverride=false` to prevent being overridden
- Support for `--system` and `--modname` parameters in `/help` command

## Technology Stack

- **Java**: 21 (LTS)
- **Kotlin**: 2.3.10
- **Build Tool**: Gradle

## Module Structure

### ModLoader Module (`net.lemoncookie.neko.modloader`)

The core mod loader module, providing **Java** and **Kotlin** API.

#### Directory Structure

```
modloader/
├── src/main/java/net/lemoncookie/neko/modloader/
│   ├── api/
│   │   └── IModAPI.java          # Java API interface
│   ├── boot/                      # Boot file system
│   │   └── BootFileManager.java   # Boot file manager
│   ├── broadcast/                 # Broadcast domain system
│   │   ├── BroadcastDomain.java
│   │   ├── BroadcastManager.java
│   │   ├── MessageListener.java
│   │   ├── ModPermission.java     # Permission enum
│   │   └── PermissionManager.java # Permission manager
│   ├── command/                   # Command system
│   │   ├── Command.java
│   │   ├── CommandSystem.java
│   │   ├── ClearCommand.java
│   │   ├── HelpCommand.java
│   │   ├── LoadCommand.java
│   │   ├── UnloadCommand.java
│   │   ├── SetCommand.java        # Set command
│   │   └── AutobootCommand.java   # Autoboot command
│   ├── config/                    # Configuration management
│   │   └── ConfigManager.java     # Config manager
│   ├── console/                   # Console system
│   │   └── Console.java
│   ├── consolemod/                # Console mod
│   │   └── ConsoleMod.java        # Console mod implementation
│   ├── core/
│   │   └── ModCore.java          # Java 21 core implementation
│   ├── lang/                      # Language management system
│   │   └── LanguageManager.java
│   ├── lib/
│   │   └── ModLibrary.java       # Java library support
│   └── ModLoader.java            # Main entry class
└── src/main/kotlin/net/lemoncookie/neko/modloader/
    ├── api/
    │   └── ModAPI.kt             # Kotlin API interface
    └── lib/
        └── KotlinModLibrary.kt         # Kotlin library support
```

#### Console System (`net.lemoncookie.neko.modloader.console`)

The console system is responsible for displaying information and handling user input, supporting colored output.

**Important Note (v1.1.0)**: Console's print methods **do NOT automatically broadcast** messages. This separation allows:
- Display: Call `console.printXXX()` methods (with colors)
- Logging: Manually broadcast to `Hub.Log` (not displayed in console)

For inter-mod communication messages, broadcast to `Hub.Console`, which will be displayed uniformly by ConsoleMod (with colors and sender prefix).

```java
// Console class
public class Console {
    // Constructor
    public Console(ModLoader modLoader)
    
    // Print methods
    public void printLine(String text)
    public void printLine()
    public void print(String text)
    
    // Colored output methods (no auto-broadcast)
    public void printError(String text)    // Red
    public void printWarning(String text)  // Yellow
    public void printSuccess(String text)  // Green
    public void printInfo(String text)     // Blue
    public void printCyan(String text)     // Cyan
    public void printMagenta(String text)  // Magenta
    public void printWhite(String text)    // White
    
    // Interactive console
    public void startInteractive()
    
    // Other methods
    public void clear()
    public String readLine() throws IOException
    public boolean readConfirmation() throws IOException
    public void close()
}
```

**Usage Examples:**
```java
// Display only (with colors), no logging
modLoader.getConsole().printSuccess("Operation successful");

// Display and log separately
String msg = "Mod loaded successfully";
modLoader.getConsole().printSuccess(msg);  // Display with color
modLoader.getBroadcastManager().broadcast("Hub.Log", "[SUCCESS] " + msg, "ModLoader");  // Log only

// Inter-mod communication (broadcast to Hub.Console, displayed by ConsoleMod)
modLoader.getBroadcastManager().broadcast("Hub.Console", "Hello from my mod!", "MyMod");

// Using ModAPI utility (v1.1.0 recommended)
ModAPI api = new ModAPI(modLoader, getModId());
api.printSuccess("My mod loaded!");        // Display
api.broadcastLog("Initialization done");   // Log only
api.broadcastConsole("Hello!");            // Display in console with sender prefix
```

#### Boot File System (`net.lemoncookie.neko.modloader.boot`)

The boot file system is used to execute command sequences automatically at startup, supporting custom boot files.

```java
// Boot file manager
public class BootFileManager {
    // Constructor
    public BootFileManager(ModLoader modLoader)
    
    // Boot file operations
    public List<String> readBootFile(String fileName)
    public boolean executeBootFile(String fileName)
    public void generateAutoBoot()                    // Generate auto.boot
    public void setCurrentBootFile(String fileName)
    public String getCurrentBootFile()
    public boolean executeCurrentBootFile()
}
```

**Boot File Syntax:**
- One command per line
- Supports all commands like `/load`, `/unload`, `/set`, etc.
- Lines starting with `#` are comments
- Default filename is `auto.boot`

**Example:**
```
# auto.boot example
/load com.example.mod
/load com.example.moda
/set modPermission com.example.moda 1
```

#### Configuration Management (`net.lemoncookie.neko.modloader.config`)

The configuration management system is used for persistent storage of configuration information.

```java
// Configuration manager
public class ConfigManager {
    // Constructor
    public ConfigManager(ModLoader modLoader)
    
    // Configuration operations
    public String getConfig(String key, String defaultValue)
    public void setConfig(String key, String value)
    
    // Boot file configuration
    public String getBootFile()
    public void setBootFile(String fileName)
    
    // Mod permission configuration
    public ModPermission getModPermission(String modId)
    public void setModPermission(String modId, int level)
    public Map<String, Integer> getAllModPermissions()
}
```

#### Console Mod (`net.lemoncookie.neko.modloader.consolemod`)

The console mod is a system mod that loads first, responsible for creating system domains and displaying messages.

```java
// Console mod
public class ConsoleMod implements IModAPI {
    // Mod info
    public String getModId()         // Returns "console-mod"
    public String getVersion()       // Returns "1.0.0"
    public String getPackageName()   // Returns "net.lemoncookie.neko.modloader.consolemod"
    public String getName()          // Returns "Console Mod"
    
    // Lifecycle
    public void onLoad(ModLoader modLoader)    // Create Hub.System and Hub.Console domains
    public void onUnload()
    
    // Registration methods (v1.1.0: with modId parameter)
    public void registerCommands(ModLoader modLoader, String modId)
    public void registerBroadcastListeners(ModLoader modLoader, String modId)
}
```

#### Command System (`net.lemoncookie.neko.modloader.command`)

The command system is responsible for parsing and executing commands, supporting built-in commands and custom commands.

**v1.1.0 Updates:**
- Supports multiple mods registering the same command
- Use `--modName` suffix to specify which mod's command to execute
- Command priority: system implementation > specified mod > single implementation

```java
// Command system
public class CommandSystem {
    // Constructor
    public CommandSystem(ModLoader modLoader)
    
    // Command management (v1.1.0: added modId and allowOverride parameters)
    public boolean registerCommand(String name, String modId, Command command, boolean allowOverride)
    public void executeCommand(String input)
    public Map<String, Map<String, Command>> getCommands()  // name -> (modId -> Command)
}

// Command interface
public interface Command {
    void execute(ModLoader modLoader, String args) throws Exception;
    String getDescription();
    String getUsage();
}
```

**Built-in Commands:**
- `/help` - Display available commands
- `/clear` - Clear console
- `/load [mod filename]` - Load mod (by filename, will auto-add .jar extension)
- `/unload [mod name]` - Unload mod (by mod name/ID)
- `/set modPermission [mod name] [level value]` - Set mod permission (level 0-3)
- `/set bootfile [filename]` - Set boot file name
- `/autoboot` - Scan mods folder and generate auto.boot file
- `/exit` - Gracefully shutdown Neko-Hub
- `/say [domain] "message"` - Send message to specific broadcast domain
- `/listen [domain] [start|stop]` - Listen/unlisten to specific broadcast domain

**Non-command Input:**
- Input without `/` prefix will be sent to `Hub.ALL` broadcast domain

**Mod Custom Commands:**

Mods can register their own commands in the `registerCommands` method (v1.1.0 with modId parameter):

```java
// Java mod example (v1.1.0)
@Override
public void registerCommands(ModLoader modLoader, String modId) {
    modLoader.getCommandSystem().registerCommand("mycommand", modId, new Command() {
        @Override
        public void execute(ModLoader modLoader, String args) {
            // Command execution logic
            modLoader.getConsole().printLine("Hello from my command!");
        }
        
        @Override
        public String getDescription() {
            return "My custom command";
        }
        
        @Override
        public String getUsage() {
            return "/mycommand";
        }
    }, false);  // allowOverride=false to prevent being overridden
}
```

```kotlin
// Kotlin mod example (v1.1.0)
override fun registerCommands(modLoader: ModLoader, modId: String) {
    modLoader.commandSystem.registerCommand("mycommand", modId, object : Command {
        override fun execute(modLoader: ModLoader, args: String) {
            // Command execution logic
            modLoader.console.printLine("Hello from my command!")
        }
        
        override fun getDescription() = "My custom command"
        
        override fun getUsage() = "/mycommand"
    }, false)  // allowOverride=false
}
```

**Command Execution with Mod Specification:**
```bash
# Execute mymod's command
/mycommand --mymod

# Execute system command (if exists)
/mycommand --system
```

#### Broadcast Domain System (`net.lemoncookie.neko.modloader.broadcast`)

The broadcast domain system is used for communication between mods, supporting multiple domain types and permission control.

**Domain Types:**
- **Public Public Domain** (`isPrivate=false, isPublic=true`): All mods (except level=3) have permission to listen and send
- **Public Private Domain** (`isPrivate=true, isPublic=true`): Requires permission to listen and send
- **Private Domain** (`isPrivate=true, isPublic=false`): Only accessible by owner

**Permission Levels:**
- **SUPER_ADMIN (level 0)**: Super administrator, has permission for all domains
- **SYSTEM_COMPONENT (level 1)**: System component, has permission for most domains
- **NORMAL_COMPONENT (level 2)**: Normal component, has permission for public domains and own private domains (default)
- **RESTRICTED_COMPONENT (level 3)**: Restricted component, only has listen permission

```java
// Broadcast domain manager
public class BroadcastManager {
    // System broadcast domains
    public static final String HUB_ALL = "Hub.ALL";         // Public public domain
    public static final String HUB_SYSTEM = "Hub.System";   // Public private domain
    public static final String HUB_CONSOLE = "Hub.Console"; // Public public domain (console)
    public static final String HUB_LOG = "Hub.Log";         // Public public domain (logging)
    
    // Error codes
    public static final int ERROR_SUCCESS = 0;              // Success
    public static final int ERROR_PERMISSION_DENIED = 502;  // Permission denied
    public static final int ERROR_DOMAIN_NOT_FOUND = 404;   // Domain not found
    public static final int ERROR_DOMAIN_EXISTS = 402;      // Domain exists
    
    // Constructor
    public BroadcastManager(ModLoader modLoader)
    
    // Domain management
    public int addDomain(String name, boolean isPrivate, boolean isPublic, String ownerModId)
    public BroadcastDomain getDomain(String name)
    public int removeDomain(String name, String modId)
    public int createSystemDomain(String ownerModId)        // Create system domain
    public int createConsoleDomain(String ownerModId)       // Create console domain
    
    // Broadcast operations
    public int broadcast(String domainName, String message, String senderModId)
    public int listen(String domainName, MessageListener listener, String modId, String modName)
    public int listenPrivate(String modId, MessageListener listener)
    
    // Permission management
    public int requestDomainPermission(String domainName, String modId, String modName)
    public int requestPermissionUpgrade(String modId, String modName, int targetLevel)
    public PermissionManager getPermissionManager()
    
    // Other methods
    public Map<String, BroadcastDomain> getDomains()
    public int getDomainCount()
    public boolean hasDomain(String name)
}

// Broadcast domain class
public class BroadcastDomain {
    public String getName()
    public boolean isPrivate()
    public boolean isPublic()
    public String getOwnerModId()
    public boolean addListener(MessageListener listener, String modId)
    public boolean removeListener(MessageListener listener)
    public void broadcast(String message, String senderModId)
}

// Message listener interface
public interface MessageListener {
    void onMessageReceived(String domain, String message, String senderModId);
}

// Permission enum
public enum ModPermission {
    SUPER_ADMIN(0, "Super Admin"),
    SYSTEM_COMPONENT(1, "System Component"),
    NORMAL_COMPONENT(2, "Normal Component"),
    RESTRICTED_COMPONENT(3, "Restricted Component");
    
    public int getLevel()
    public String getDisplayName()
    public static ModPermission fromLevel(int level)
}

// Permission manager
public class PermissionManager {
    public ModPermission getModPermission(String modId)
    public void setModPermission(String modId, ModPermission permission)
    public boolean hasPermission(String modId, ModPermission requiredPermission)
    public boolean hasLevelPermission(String modId, int requiredLevel)
}
```

#### Java API (`net.lemoncookie.neko.modloader.api`)

```java
// Java mod API interface
public interface IModAPI {
    String getModId();
    String getVersion();
    String getPackageName();
    void onLoad(ModLoader modLoader);
    void onUnload();
    default String getName() { return getModId(); }
    default void registerCommands(ModLoader modLoader, String modId) {}
    default void registerBroadcastListeners(ModLoader modLoader, String modId) {}
}

// Java library support
public class ModLibrary {
    public void register(String name, Object component);
    public <T> T get(String name);
    public boolean has(String name);
    public Set<String> getRegisteredNames();
}
```

#### ModAPI Utility Class (`net.lemoncookie.neko.modloader.api`)

The ModAPI utility class wraps common APIs to simplify mod development:

```java
public class ModAPI {
    // Constructor
    public ModAPI(ModLoader modLoader, String modId)
    
    // Command registration
    public boolean registerCommand(String name, Command command, boolean allowOverride)
    
    // Broadcast system
    public void broadcast(String domain, String message)
    public void broadcastConsole(String message)      // Broadcast to Hub.Console
    public void broadcastLog(String message)          // Broadcast to Hub.Log (logging only)
    
    // Domain management
    public void createPrivateDomain()                 // Create private domain Hub.[modId]
    
    // Console output (display only, no auto-broadcast)
    public void printError(String text)
    public void printWarning(String text)
    public void printSuccess(String text)
    public void printInfo(String text)
    public void printCyan(String text)
    public void printMagenta(String text)
    public void printWhite(String text)
    public void printLine(String text)
    public void printLine()
    
    // Quick access
    public ModLoader getModLoader()
    public String getModId()
    public Console getConsole()
    public CommandSystem getCommandSystem()
    public BroadcastManager getBroadcastManager()
}
```

**Usage Example:**
```java
import net.lemoncookie.neko.modloader.api.ModAPI;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.ModLoader;

public class MyMod implements IModAPI {
    private ModAPI api;
    
    @Override
    public String getModId() { return "my-mod"; }
    
    @Override
    public String getVersion() { return "1.0.0"; }
    
    @Override
    public String getPackageName() { return "com.example.mymod"; }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        // Initialize ModAPI utility
        api = new ModAPI(modLoader, getModId());
        
        // Use wrapped APIs
        api.printSuccess("My mod loaded!");
        api.broadcastLog("[MyMod] Initialization complete");
        api.broadcastConsole("Hello from MyMod!");
        
        // Register command
        api.registerCommand("mycmd", (loader, args) -> {
            api.printInfo("Command executed!");
        }, false);
    }
    
    @Override
    public void onUnload() {
        api.printWarning("My mod unloaded!");
    }
}
```

#### Core Implementation (`net.lemoncookie.neko.modloader.core`)

```java
// Java 21 implementation for stability
public class ModCore {
    public void start();
}
```

#### Kotlin API (`net.lemoncookie.neko.modloader.api`)

```kotlin
// Kotlin mod API interface (renamed to KModAPI to avoid conflict with Java ModAPI utility)
interface KModAPI {
    val modId: String
    val version: String
    val name: String
        get() = modId
    val packageName: String
    fun onLoad(modLoader: ModLoader)
    fun onUnload()
    fun registerCommands(modLoader: ModLoader, modId: String) {}
    fun registerBroadcastListeners(modLoader: ModLoader, modId: String) {}
    fun getInfo(): ModInfo
}

data class ModInfo(val id: String, val name: String, val version: String)
```

**Note:** Kotlin mods should use `KModAPI` interface instead of `ModAPI` to avoid naming conflicts with the Java `ModAPI` utility class.

#### Kotlin Library Support (`net.lemoncookie.neko.modloader.lib`)

```kotlin
// Kotlin library support (DSL style)
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

#### Main Entry Class

```java
public class ModLoader {
    public ModLoader()
    public void initialize()
    public void registerJavaMod(IModAPI mod)
    public void registerKotlinMod(KModAPI mod)  // v1.1.0: KModAPI
    public ModLibrary getJavaLibrary()
    public List<IModAPI> getJavaMods()
    public List<KModAPI> getKotlinMods()  // v1.1.0: KModAPI
    public void unloadAll()
    public ModCore getCore()
    public Console getConsole()
    public CommandSystem getCommandSystem()
    public BroadcastManager getBroadcastManager()
    public LanguageManager getLanguageManager()
    public BootFileManager getBootFileManager()
    public ConfigManager getConfigManager()
    public static String getVersion()
    public static String getMinApiVersion()  // v1.1.0: "1.1.0"
    public static String getGithubVersion()
    public boolean isInitialized()
    public static void main(String[] args)
}
```

### 2. Bookkeeping Module (`net.lemoncookie.neko.bookkeeping`)

Accounting module, providing financial management features.

- `Bookkeeping` - Accounting management class

### 3. Markdown Module (`net.lemoncookie.neko.markdown`)

Markdown processing module, supporting Markdown parsing and JavaFX rendering.

#### Directory Structure

```
markdown/
├── src/main/kotlin/net/lemoncookie/neko/markdown/
│   ├── Markdown.kt                      # Markdown parser (implements IModAPI)
│   └── javafx/
│       └── MarkdownRenderer.kt          # JavaFX renderer
└── src/main/resources/lang/
    ├── zh.json                          # Chinese language file
    └── en.json                          # English language file
```

#### Markdown Parser (`net.lemoncookie.neko.markdown`)

The Markdown parser is responsible for converting Markdown text to HTML, with support for reading content from files.

```kotlin
// Markdown class
class Markdown : IModAPI {
    // Module info
    override fun getModId(): String              // Returns "markdown"
    override fun getVersion(): String            // Returns "1.1.0"
    override fun getPackageName(): String        // Returns "net.lemoncookie.neko.markdown"
    override fun getName(): String               // Returns "Markdown Module"
    
    // Lifecycle
    override fun onLoad(modLoader: ModLoader, modId: String)
    override fun onUnload()
    
    // Markdown parsing
    fun parse(markdown: String): String          // Parse Markdown to HTML
    fun parseFile(filePath: String): String?     // Read from file and parse
}
```

**Usage Example:**

```kotlin
// Create Markdown instance
val markdown = Markdown()

// Parse Markdown text
val html = markdown.parse("# Hello\n\nThis is **bold** text.")

// Parse from file
val htmlFromFile = markdown.parseFile("path/to/file.md")
```

#### JavaFX Renderer (`net.lemoncookie.neko.markdown.javafx`)

The JavaFX renderer uses WebView component to display rendered Markdown content.

```kotlin
// MarkdownRenderer class
class MarkdownRenderer(private val markdown: Markdown, private val modLoader: ModLoader) {
    // Create WebView component
    fun createWebView(initialMarkdown: String? = null): WebView
    
    // Update content
    fun updateContent(markdownText: String)
    
    // Load from file
    fun loadFromFile(filePath: String): Boolean
    
    // Create full scene
    fun createScene(width: Double = 800.0, height: Double = 600.0): Scene
    
    // Get WebView
    fun getWebView(): WebView?
}
```

**Usage Example:**

```kotlin
// Create renderer
val renderer = MarkdownRenderer(markdown, modLoader)

// Create WebView and display
val webView = renderer.createWebView("# Hello World")

// Update content
renderer.updateContent("## New Content\n\nUpdated text.")

// Load from file
renderer.loadFromFile("document.md")
```

**Style Features:**
- Responsive layout, max width 900px
- Code block highlighting
- Table styling
- Blockquote left border
- Link hover effects
- Image auto-sizing
- Internationalization support (Chinese and English)

### 4. FileLabel Module (`net.lemoncookie.neko.filelabel`)

File label module, supporting file tagging and management.

- `FileLabel` - File label management

### 5. Calendar Module (`net.lemoncookie.neko.calendar`)

Calendar module, providing schedule management features.

- `Calendar` - Calendar management

### 6. TodoList Module (`net.lemoncookie.neko.todolist`)

Todo list module, supporting task management.

- `TodoList` - Todo list

## Quick Start

### Add Dependencies

Add module dependencies in `build.gradle.kts`:

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

## Mod Loader Behavior

### Startup Flow

1. **Initialization**: Create ModLoader instance and call initialize() method
2. **Create mods folder**: Automatically create if not exists
3. **Load console mod**: Load console mod first (default permission SUPER_ADMIN)
4. **Create Hub.Log domain**: Create logging domain (v1.1.0+)
5. **Load boot file**:
   - Load `auto.boot` file by default
   - Automatically create and execute if `auto.boot` doesn't exist
   - Show error but don't create if user-specified boot file doesn't exist
6. **Start console interaction**: Start interactive console

### Boot File System

Boot file is a command sequence file executed during mod loader initialization:

- **Default filename**: `auto.boot`
- **First startup**: Automatically create `auto.boot` file
- **Custom boot file**: Set via `/set bootfile [filename]` command
- **Generate auto.boot**: Generate by scanning mods folder via `/autoboot` command

### Permission System

Mod permission system controls mod access to broadcast domains:

- **Default permission**: Normal mods default to NORMAL_COMPONENT (level 2)
- **Console mod**: Defaults to SUPER_ADMIN (level 0)
- **Set permission**: Set via `/set modPermission [mod name] [level value]` command
- **Permission persistence**: Permission configuration saved in `neko-hub.config` file

### Terminal Mode

Neko-Hub supports CLI mode, interacting with users through the console. The console system supports:

- **Colored output**: Uses ANSI color codes, supporting red (error), yellow (warning), green (success), blue (info), etc.
- **Interactive commands**: Users can input commands to interact with the system
- **Broadcast domain messages**: Console listens to Hub.Console domain, displaying messages from all mods
- **Cross-platform support**: Works on both Windows and Unix-like systems
- **UTF-8 encoding**: Supports non-ASCII characters like Chinese

### Command System Operation Principle

The command system workflow:

1. **Command registration**: Register built-in commands at system startup, mods can also register custom commands
2. **Command parsing**: Parse user input, extract command name and arguments (supports quoted arguments)
3. **Command execution**: Find and execute the corresponding command implementation
4. **Error handling**: Capture and display errors during command execution

### Broadcast Domain System Usage

The broadcast domain system allows communication between mods:

- **Hub.ALL**: Public public domain, all mods (except level=3) can listen and send
- **Hub.System**: Public private domain, requires permission level 1 or lower, and user confirmation to get permission
- **Hub.Console**: Public public domain, created by console mod for inter-mod communication message display
- **Hub.Log**: Public public domain, dedicated for logging system (not displayed in console, v1.1.0+)
- **Private domain**: Format `Hub.[modId]`, only accessible by owner
- **Public private domain**: Requires permission to listen and send

**Best Practices for Message Sending (v1.1.0+):**
- **Inter-mod communication**: Broadcast to `Hub.Console`, displayed uniformly by ConsoleMod (with colors and sender prefix)
- **Logging**: Broadcast to `Hub.Log`, only recorded to log file, not displayed in console
- **System messages**: Call `console.printXXX()` directly to display (with colors), no auto-broadcast
- **Recommended**: Use `ModAPI` utility class for simplified API access

### Language System Usage

The language system supports multiple languages:

- **Default language**: Chinese (zh)
- **Language files**: Located in resources/lang/ directory
- **Dynamic switching**: Can switch language at runtime
- **Message formatting**: Supports parameterized message formatting

## Java Mod Development Example

### Basic Example (Traditional)

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
    
    // v1.1.0: with modId parameter
    @Override
    public void registerCommands(ModLoader modLoader, String modId) {
        modLoader.getCommandSystem().registerCommand("mycmd", modId, (loader, args) -> {
            loader.getConsole().printLine("Command executed!");
        }, false);
    }
}

// Register mod
ModLoader loader = new ModLoader();
loader.initialize();
loader.registerJavaMod(new MyJavaMod());
```

### Recommended (Using ModAPI Utility - v1.1.0+)

```java
import net.lemoncookie.neko.modloader.api.ModAPI;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.ModLoader;

public class MyJavaMod implements IModAPI {
    private ModAPI api;
    
    @Override
    public String getModId() { return "my-java-mod"; }

    @Override
    public String getVersion() { return "1.0.0"; }

    @Override
    public String getPackageName() { return "com.example.myjavamod"; }

    @Override
    public void onLoad(ModLoader modLoader) {
        // Initialize ModAPI utility
        api = new ModAPI(modLoader, getModId());
        
        // Use wrapped APIs for easier development
        api.printSuccess("Java mod loaded!");
        api.broadcastLog("Initialization complete");
        api.broadcastConsole("Hello from MyJavaMod!");
        
        // Register command with simplified API
        api.registerCommand("mycmd", (loader, args) -> {
            api.printInfo("Command executed!");
        }, false);
    }

    @Override
    public void onUnload() {
        api.printWarning("Java mod unloaded!");
    }
}
```

## Kotlin Mod Development Example

```kotlin
import net.lemoncookie.neko.modloader.api.KModAPI  // Note: KModAPI, not ModAPI
import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.lib.kotlinModLibrary

class MyKotlinMod : KModAPI {
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
    
    // v1.1.0: with modId parameter
    override fun registerCommands(modLoader: ModLoader, modId: String) {
        modLoader.commandSystem.registerCommand("mycmd", modId, object : Command {
            override fun execute(modLoader: ModLoader, args: String) {
                modLoader.console.printLine("Command executed!")
            }
            override fun getDescription() = "My command"
            override fun getUsage() = "/mycmd"
        }, false)
    }
}

// Register mod
val loader = ModLoader()
loader.initialize()
loader.registerKotlinMod(MyKotlinMod())

// Use Kotlin library DSL
val lib = kotlinModLibrary {
    register("config") { loadConfig() }
    register("database", Database())
}
```

## Development Standards

1. **Java Version**: All modules must use Java 21
2. **Package Naming**: Follow `net.lemoncookie.neko.{module}` convention
3. **API Design**: 
   - Java API uses traditional OOP style
   - Kotlin API uses DSL and functional style
4. **Core Stability**: Core functionality uses Java 21 implementation
5. **Testing**: Each module should include unit tests

## Environment Requirements

- JDK 21 or higher
- Gradle 8.7+

## Build Project

```bash
./gradlew build
```

## Run Tests

```bash
./gradlew test
```
