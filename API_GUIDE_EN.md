# Neko-Hub API Development Guide

## Project Overview

Neko-Hub is a multi-functional project that supports both CLI and GUI modes. The project adopts a modular design, **based on Java 21**, for easy extension and maintenance.

## Technology Stack

- **Java**: 21 (LTS)
- **Kotlin**: 2.0.0
- **Build Tool**: Gradle

## Module Structure

### ModLoader Module (`net.lemoncookie.neko.modloader`)

The core mod loader module, providing **Java** and **Kotlin** dual APIs.

#### Directory Structure

```
modloader/
├── src/main/java/net/lemoncookie/neko/modloader/
│   ├── core/
│   │   └── ModCore.java          # Java 21 core implementation
│   ├── api/
│   │   └── IModAPI.java          # Java API interface
│   └── lib/
│       └── ModLibrary.java       # Java library support
└── src/main/kotlin/net/lemoncookie/neko/modloader/
    ├── ModLoader.kt              # Main entry class
    ├── api/kt/
    │   └── ModAPI.kt             # Kotlin API interface
    └── lib/kt/
        └── ModLibrary.kt         # Kotlin library support
```

#### Java API (`net.lemoncookie.neko.modloader.api`)

```java
// Java mod API interface
public interface IModAPI {
    String getModId();
    String getVersion();
    void onLoad();
    void onUnload();
    default String getName() { return getModId(); }
}

// Java library support
public class ModLibrary {
    public void register(String name, Object component);
    public <T> T get(String name);
    public boolean has(String name);
    public Set<String> getRegisteredNames();
}
```

#### Kotlin API (`net.lemoncookie.neko.modloader.api.kt`)

```kotlin
// Kotlin mod API interface
interface ModAPI {
    val modId: String
    val version: String
    val name: String
    fun onLoad()
    fun onUnload()
    fun getInfo(): ModInfo
}

// Mod info data class
data class ModInfo(val id: String, val name: String, val version: String)

// Kotlin library support (DSL style)
class ModLibrary {
    fun register(name: String, component: Any)
    operator fun <T> get(name: String): T?
    operator fun contains(name: String): Boolean
    inline fun <reified T> register(name: String, noinline init: () -> T): T
    fun registerAll(vararg pairs: Pair<String, Any>)
}

// DSL function
inline fun modLibrary(block: ModLibrary.() -> Unit): ModLibrary
```

#### Core Implementation (`net.lemoncookie.neko.modloader.core`)

```java
// Java 21 implementation for stability
public class ModCore {
    public void start();
    public String getVersion();  // Returns "1.0.0"
    public boolean isInitialized();
}
```

#### Main Entry Class

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

### 2. Bookkeeping Module (`net.lemoncookie.neko.bookkeeping`)

Bookkeeping module providing financial management features.

- `Bookkeeping` - Bookkeeping management class

### 3. Markdown Module (`net.lemoncookie.neko.markdown`)

Markdown processing module supporting Markdown parsing and rendering.

- `Markdown` - Markdown parser

### 4. FileLabel Module (`net.lemoncookie.neko.filelabel`)

File labeling module supporting tag management for files.

- `FileLabel` - File label management

### 5. Calendar Module (`net.lemoncookie.neko.calendar`)

Calendar module providing schedule management features.

- `Calendar` - Calendar management

### 6. TodoList Module (`net.lemoncookie.neko.todolist`)

Todo list module supporting task management.

- `TodoList` - Todo list management

## Quick Start

### Adding Dependencies

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

### Java Mod Development Example

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

// Register mod
ModLoader loader = new ModLoader();
loader.initialize();
loader.registerJavaMod(new MyJavaMod());
```

### Kotlin Mod Development Example

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
4. **Core Stability**: Core functionality implemented in Java 21
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
