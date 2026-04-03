# Neko-Hub API Development Guide

## Project Overview

Neko-Hub is a multi-functional project that currently supports CLI mode and will support GUI mode in the future. The project adopts a modular design, **based on Java 21**, for easy extension and maintenance.

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
        └── ModLibrary.kt         # Kotlin library support
```

#### Console System (`net.lemoncookie.neko.modloader.console`)

The console system is responsible for displaying information and handling user input, supporting colored output and broadcast domain message display.

```java
// Console class
public class Console {
    // Constructor
    public Console(ModLoader modLoader)
    
    // Print methods
    public void printLine(String text)
    public void printLine()
    public void print(String text)
    
    // Colored output methods
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
    
    // Registration methods
    public void registerCommands(ModLoader modLoader)
    public void registerBroadcastListeners(ModLoader modLoader)
}
```

#### Command System (`net.lemoncookie.neko.modloader.command`)

The command system is responsible for parsing and executing commands, supporting built-in commands and custom commands.

```java
// Command system
public class CommandSystem {
    // Constructor
    public CommandSystem(ModLoader modLoader)
    
    // Command management
    public void registerCommand(String name, Command command)
    public void executeCommand(String input)
    public Map<String, Command> getCommands()
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
- `/load [mod package name or filename]` - Load mod (supports package name or filename, not path)
- `/unload [mod package name or filename]` - Unload mod
- `/set modPermission [mod name] [level value]` - Set mod permission (level 0-3)
- `/set bootfile [filename]` - Set boot file name
- `/autoboot` - Scan mods folder and generate auto.boot file

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
    default void registerCommands(ModLoader modLoader) {}
    default void registerBroadcastListeners(ModLoader modLoader) {}
}

// Java library support
public class ModLibrary {
    public void register(String name, Object component);
    public <T> T get(String name);
    public boolean has(String name);
    public Set<String> getRegisteredNames();
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
// Kotlin mod API interface
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

#### Kotlin Library Support (`net.lemoncookie.neko.modloader.lib`)

```kotlin
// Kotlin library support (DSL style)
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

#### Main Entry Class

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
    public BootFileManager getBootFileManager()
    public ConfigManager getConfigManager()
    public static String getVersion()
    public static String getMinApiVersion()
    public static String getGithubVersion()
    public boolean isInitialized()
    public static void main(String[] args)
}
```

### 2. Bookkeeping Module (`net.lemoncookie.neko.bookkeeping`)

Accounting module, providing financial management features.

- `Bookkeeping` - Accounting management class

### 3. Markdown Module (`net.lemoncookie.neko.markdown`)

Markdown processing module, supporting Markdown parsing and rendering.

- `Markdown` - Markdown parser

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
4. **Load boot file**:
   - Load `auto.boot` file by default
   - Automatically create and execute if `auto.boot` doesn't exist
   - Show error but don't create if user-specified boot file doesn't exist
5. **Start console interaction**: Start interactive console

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
- **Hub.Console**: Public public domain, created by console mod for displaying messages
- **Private domain**: Format `Hub.[modId]`, only accessible by owner
- **Public private domain**: Requires permission to listen and send

### Language System Usage

The language system supports multiple languages:

- **Default language**: Chinese (zh)
- **Language files**: Located in resources/lang/ directory
- **Dynamic switching**: Can switch language at runtime
- **Message formatting**: Supports parameterized message formatting

## Java Mod Development Example

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

// Register mod
ModLoader loader = new ModLoader();
loader.initialize();
loader.registerJavaMod(new MyJavaMod());
```

## Kotlin Mod Development Example

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

// Register mod
val loader = ModLoader()
loader.initialize()
loader.registerKotlinMod(MyKotlinMod())

// Use Kotlin library DSL
val lib = modLibrary {
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
