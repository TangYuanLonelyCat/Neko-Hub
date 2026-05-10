# Neko-Hub API Development Guide

## Project Overview

Neko-Hub is a mod loader core that currently supports CLI mode. The project uses a modular design, **based on Java 21**, making it easy to extend and maintain.

## Tech Stack

- **Java**: 21 (LTS)
- **Kotlin**: 2.3.10
- **Build Tool**: Gradle 9.4.0

## Current Version

- **ModLoader Version**: 3.2.4
- **Minimum API Version**: 2.3.0

## Module Structure

### ModLoader Module (`net.lemoncookie.neko.modloader`)

The mod loader core module, providing **Java** and **Kotlin** APIs.

#### Directory Structure

```
modloader/
├── src/main/java/net/lemoncookie/neko/modloader/
│   ├── api/
│   │   ├── IModAPI.java          # Java API Interface
│   │   ├── ModAPI.java           # Java API Utility Class
│   │   └── ModDependency.java    # Dependency Class
│   ├── boot/
│   │   └── BootFileManager.java  # Boot File Manager
│   ├── broadcast/
│   │   ├── BroadcastDomain.java
│   │   ├── BroadcastManager.java
│   │   ├── MessageListener.java
│   │   ├── ModPermission.java    # Permission Enum
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
│   └── ModLoader.java            # Main Entry Class
└── src/main/kotlin/net/lemoncookie/neko/modloader/
    ├── api/
    │   └── KModAPI.kt            # Kotlin API Interface
    └── lib/
        └── KotlinModLibrary.kt   # Kotlin Library Support
```

---

## Core Components

### Main Entry Class ModLoader

```java
public class ModLoader {
    private static final String VERSION = "3.2.4";
    private static final String MIN_API_VERSION = "2.3.0";
    
    public ModLoader()
    public void initialize()
    
    // Mod Registration
    public void registerJavaMod(IModAPI mod)
    public void registerKotlinMod(KModAPI mod)
    
    // Mod Unloading
    public void unloadAll()
    public boolean unloadMod(String modName)
    
    // Get Components
    public ModCore getCore()
    public Console getConsole()
    public BroadcastManager getBroadcastManager()
    public LanguageManager getLanguageManager()
    public BootFileManager getBootFileManager()
    public SimpleLogger getSimpleLogger()
    public ModLibrary getJavaLibrary()
    
    // Get Mod Lists
    public List<IModAPI> getJavaMods()
    public List<KModAPI> getKotlinMods()
    
    // Static Methods
    public static String getVersion()
    public static String getMinApiVersion()
    public boolean isInitialized()
    
    // Main Method
    public static void main(String[] args)
}
```

---

### Console System

The console system is responsible for displaying messages and handling user input, supporting colored output.

```java
public class Console {
    public Console(ModLoader modLoader)
    
    // Print Methods
    public void printLine(String text)
    public void printLine()
    public void print(String text)
    
    // Colored Output Methods
    public void printError(String text)    // Red
    public void printWarning(String text)  // Yellow
    public void printSuccess(String text)  // Green
    public void printInfo(String text)     // Blue
    public void printCyan(String text)     // Cyan
    public void printMagenta(String text)  // Magenta
    public void printWhite(String text)    // White
    
    // Interactive Console
    public void startInteractive()
    
    // Input Box Control
    public void setShowInputBox(boolean show)
    public boolean isShowInputBox()
    
    // Other Methods
    public void clear()
    public String readLine() throws IOException
    public boolean readConfirmation() throws IOException
    public void close()
}
```

**Usage Example**:
```java
// Display message
modLoader.getConsole().printSuccess("Operation successful");

// Hide input box (suitable for server environments)
modLoader.getConsole().setShowInputBox(false);
```

---

### Broadcast Domain System BroadcastManager

The broadcast domain system is used for communication between mods, supporting multiple domain types and permission control.

**System Broadcast Domain Constants**:
```java
public static final String HUB_ALL = "Hub.ALL";         // Public domain
public static final String HUB_SYSTEM = "Hub.System";   // Public domain (log dedicated)
public static final String HUB_CONSOLE = "Hub.Console"; // Public domain (console)
public static final String HUB_COMMAND = "Hub.Command"; // Public domain (command)
```

**Error Codes**:
```java
public static final int ERROR_SUCCESS = 0;              // Operation successful
public static final int ERROR_PERMISSION_DENIED = 502;  // Permission denied
public static final int ERROR_DOMAIN_NOT_FOUND = 404;   // Domain not found
public static final int ERROR_DOMAIN_EXISTS = 402;      // Domain already exists
```

**Main Methods**:
```java
public class BroadcastManager {
    public BroadcastManager(ModLoader modLoader)
    
    // Domain Management
    public int addDomain(String name, boolean isPrivate, boolean isPublic, String ownerModId)
    public BroadcastDomain getDomain(String name)
    public int removeDomain(String name, String modId)
    public int createConsoleDomain(String ownerModId)
    
    // Broadcast Functions
    public int broadcast(String domainName, String message, String senderModId)
    public int listen(String domainName, MessageListener listener, String modId, String modName)
    public int listenPrivate(String modId, MessageListener listener)
    public int unlisten(String domainName, MessageListener listener)
    
    // Permission Management
    public int requestDomainPermission(String domainName, String modId, String modName)
    public int requestPermissionUpgrade(String modId, String modName, int targetLevel)
    public PermissionManager getPermissionManager()
    
    // Other Methods
    public Map<String, BroadcastDomain> getDomains()
    public int getDomainCount()
    public boolean hasDomain(String name)
    public boolean hasPermissionToSend(String domainName, String modId)
    public boolean hasPermissionToListen(String domainName, String modId)
}
```

**Domain Types**:
- **Public Domain** (`isPrivate=false, isPublic=true`): All mods (except level=3) have permission to listen and send
- **Public Private Domain** (`isPrivate=true, isPublic=true`): Requires permission to listen and send
- **Private Domain** (`isPrivate=true, isPublic=false`): Only accessible by the owner

**Domain Naming Convention**:
- **System Domains**: Start with `Hub.` (e.g., `Hub.ALL`, `Hub.System`, `Hub.Console`, `Hub.Command`)
- **Private Domains**: Default format is `Hub.[modId]` (automatically created via `listenPrivate` method)
- **Custom Domains**: Mods can freely create broadcast domains with any name, not required to start with `Hub.`
  - Examples: `Maomao.KeAi`, `com.example.neko`, `MyMod.Chat` are all valid domain names
  - Domain names are just string keys, no functional restrictions

**Permission Levels**:
| Level | Name | Description |
|-------|------|-------------|
| 0 | SUPER_ADMIN | Super administrator, has all domain permissions |
| 1 | SYSTEM_COMPONENT | System component, has most domain permissions |
| 2 | NORMAL_COMPONENT | Normal component, has public domain and own private domain permissions (default) |
| 3 | RESTRICTED_COMPONENT | Restricted component, only has listen permission |

---

### Permission Enum ModPermission

```java
public enum ModPermission {
    SUPER_ADMIN(0, "Super Administrator"),
    SYSTEM_COMPONENT(1, "System Component"),
    NORMAL_COMPONENT(2, "Normal Component"),
    RESTRICTED_COMPONENT(3, "Restricted Component");
    
    public int getLevel()
    public String getDisplayName()
    public static ModPermission fromLevel(int level)
}
```

---

### Message Listener Interface MessageListener

```java
public interface MessageListener {
    void onMessageReceived(String domain, String message, String senderModId);
}
```

---

### Command System

**Command Message Class CommandMessage**:
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

**Command Listener Base Class BaseCommandListener**:
```java
public abstract class BaseCommandListener implements MessageListener {
    protected final ModLoader modLoader;
    protected final String commandName;
    
    public BaseCommandListener(ModLoader modLoader, String commandName)
    protected abstract void execute(CommandMessage commandMessage, String senderModId)
}
```

**Built-in Commands**:
- `/set modpermission [mod_name] [level]` - Set mod permission (0-3)
- `/set bootfile [filename]` - Set boot file name
- `/set language [en/zh]` - Switch language
- `/change bootfile [filename]` - Switch and execute boot file
- `/change InputBoxView [true/false]` - Show/hide input box
- `/help` - Display available commands
- `/clear` - Clear console
- `/load [mod_filename]` - Load mod
- `/unload [mod_name]` - Unload mod
- `/list mod [page]` - List loaded mods
- `/exit` - Exit application
- `/say [domain] "message"` - Send message to specific broadcast domain
- `/listen [domain] [start|stop]` - Listen/stop listening to specific broadcast domain
- `/autoboot` - Scan mods folder and generate auto.boot file

---

### Boot File System BootFileManager

```java
public class BootFileManager {
    public BootFileManager(ModLoader modLoader)
    
    // Boot File Operations
    public List<String> readBootFile(String fileName)
    public boolean executeBootFile(String fileName)
    public void generateAutoBoot()
    public void setCurrentBootFile(String fileName)
    public String getCurrentBootFile()
    public boolean executeCurrentBootFile()
    public void switchBootFileAndExecute(String fileName)
    
    // Command Insertion
    public void insertCommandAtHead(String command)
    public void insertCommandAtHeadWithReplace(String command, String commandPrefix)
    public void insertCommandAtTail(String command)
}
```

**Boot File Syntax**:
- One command per line
- Supports `/load`, `/unload`, `/set` and all other commands
- Lines starting with `#` are comments
- Default filename is `auto.boot`

---

### Language Manager LanguageManager

```java
public class LanguageManager {
    public LanguageManager()
    public void loadLanguage(String lang)
    public String getMessage(String key, Object... args)
    public boolean hasMessage(String key)
}
```

---

### Logging System SimpleLogger

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

### Library Support ModLibrary

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

### Interface IModAPI

Java mods need to implement the `IModAPI` interface:

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

**Important Notes**:
- `getApiVersion()` must return the API version used by the mod
- Mods without declared API version will be rejected
- A warning will be issued if API version equals mod version

### Utility Class ModAPI

The `ModAPI` utility class provides convenient API access methods:

```java
public class ModAPI {
    public ModAPI(ModLoader modLoader, String modId)
    
    // Broadcast System
    public void broadcast(String domain, String message)
    public void broadcastAll(String message)           // Hub.ALL
    public void broadcastConsole(String message)       // Hub.Console
    public void broadcastLog(String message)           // Hub.System
    public void listen(String domain, MessageListener listener)
    
    // Domain Management
    public int createDomain(String name, boolean isPrivate, boolean isPublic)
    public int createPrivateDomain(String name)
    public int createPublicDomain(String name)
    
    // Permission Management
    public int requestPermissionUpgrade(int targetLevel)
    public int requestSystemPermission()
    public int requestAdminPermission()
    
    // Console Output
    public void print(String text)
    public void printError(String text)
    public void printWarning(String text)
    public void printSuccess(String text)
    public void printInfo(String text)
    
    // Quick Access
    public String getModId()
    public String getModName()
    public ModLoader getModLoader()
    public BroadcastManager getBroadcastManager()
    public Console getConsole()
}
```

### Dependency Class ModDependency

```java
public class ModDependency {
    public ModDependency(String modId, String minVersion)
    public String getModId()
    public String getMinVersion()
}
```

### Java Mod Example

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
        // Implement custom command by listening to Hub.Command domain
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

### Interface KModAPI

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

### Kotlin Mod Example

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

### Kotlin Library Support

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

## Mod Loader Behavior

### Startup Flow

1. **Initialization**: Create ModLoader instance and call `initialize()` method
2. **Create mods folder**: Automatically create if it doesn't exist
3. **Create system domains**: Create Hub.ALL and Hub.System domains
4. **Load system mod**: Load SystemMod first (SUPER_ADMIN permission)
5. **Load console mod**: Load ConsoleMod first (SUPER_ADMIN permission)
6. **Register built-in command listeners**: Register all built-in commands to Hub.Command domain
7. **Load boot file**: Load `auto.boot` file by default
8. **Start console interaction**: Start interactive console

### Permission System

- **Default Permission**: Normal mods default to NORMAL_COMPONENT (level 2)
- **System Mods**: SystemMod and ConsoleMod default to SUPER_ADMIN (level 0)
- **Set Permission**: Use `/set modpermission [mod_name] [level]`
- **Permission Persistence**: Permission settings are saved in boot file

### API Version Check

Mods must explicitly declare API version:
- Mods without declared API version will be rejected
- API version lower than minimum version will be rejected
- A warning will be issued if API version equals mod version

---

## JAR Manifest Configuration

Configure JAR manifest in `build.gradle.kts`:

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

## Requirements

- JDK 21 or higher
- Gradle 8.7+

## Build Project

```bash
./gradlew build
```

## Run Project

```bash
./gradlew :modloader:run
```
