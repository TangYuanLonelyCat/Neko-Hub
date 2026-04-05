# Code Issues Report - Neko-Hub

**Generated**: 2026
**Repository**: https://github.com/TangYuanLonelyCat/Neko-Hub
**Total Issues Found**: 15

---

## 🔴 High Severity Issues (Critical)

### Issue #1: NullPointerException Risk in `getLoadedMod()`

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/ModLoader.java`  
**Lines**: 291-298  
**Severity**: HIGH

#### Problem
The `getLoadedMod()` method does not check if `loadedMod.getModId()` is null before calling `.equals()`, which can cause NullPointerException when a mod with null modId is in the list.

```java
private IModAPI getLoadedMod(String modId) {
    for (IModAPI loadedMod : javaMods) {
        if (loadedMod.getModId().equals(modId)) {  // ❌ NPE risk if getModId() returns null
            return loadedMod;
        }
    }
    return null;
}
```

#### Risk Scenario
If a malicious or buggy mod returns null from `getModId()`, loading any subsequent mod that depends on it will crash the ModLoader.

#### Suggested Fix
```java
private IModAPI getLoadedMod(String modId) {
    for (IModAPI loadedMod : javaMods) {
        String currentModId = loadedMod.getModId();
        if (currentModId != null && currentModId.equals(modId)) {
            return loadedMod;
        }
    }
    return null;
}
```

#### Impact
- Application crash during mod loading
- Dependency checking failure

---

### Issue #2: ConcurrentModificationException Risk in `unloadMod()`

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/ModLoader.java`  
**Lines**: 393-434  
**Severity**: HIGH

#### Problem
The `unloadMod()` method iterates over `javaMods` and `kotlinMods` lists while potentially modifying them, which can cause `ConcurrentModificationException` if the lists are modified elsewhere during iteration.

```java
public boolean unloadMod(String modName) {
    for (IModAPI javaMod : javaMods) {  // ❌ Iterating
        if (javaMod.getModId().equals(modName) || javaMod.getName().equals(modName)) {
            javaMod.onUnload();
            javaMods.removeIf(mod -> ...);  // ❌ Modifying during iteration
            // ...
        }
    }
}
```

#### Risk Scenario
If another thread or callback modifies `javaMods` during the unload process, a `ConcurrentModificationException` will be thrown.

#### Suggested Fix
```java
public boolean unloadMod(String modName) {
    IModAPI modToUnload = null;
    for (IModAPI javaMod : javaMods) {
        if (javaMod.getModId().equals(modName) || javaMod.getName().equals(modName)) {
            modToUnload = javaMod;
            break;
        }
    }
    
    if (modToUnload != null) {
        try {
            modToUnload.onUnload();
        } catch (Throwable e) {
            // handle error
        }
        javaMods.removeIf(mod -> mod.getModId().equals(modName) || mod.getName().equals(modName));
        return true;
    }
    // ... same for kotlin mods
}
```

#### Impact
- Potential crash during mod unloading
- Inconsistent mod state

---

### Issue #3: Permission Level Logic Error in `hasPermissionToAccessDomain()`

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/broadcast/BroadcastManager.java`  
**Lines**: 232-274  
**Severity**: HIGH

#### Problem
The comment states "level 3: can only listen, cannot send" but the implementation completely blocks level 3 components from accessing domains at all, making listening impossible.

```java
// Comment says: 限权组件（level 3）：只能监听，不能发送
// But implementation:
if (level == 3) {
    return false;  // ❌ Blocks ALL access, not just sending
}
```

#### Risk Scenario
Level 3 components cannot function as intended - they cannot even listen to broadcasts, contradicting the design documentation.

#### Suggested Fix
Separate send and listen permissions:
```java
private boolean hasPermissionToSend(String domainName, String modId) {
    // ... existing checks ...
    if (level == 3) {
        return false;  // Cannot send
    }
    // ...
}

private boolean hasPermissionToListen(String domainName, String modId) {
    // ... existing checks ...
    // Level 3 CAN listen
    return true;  // With other appropriate checks
}
```

#### Impact
- Feature not working as documented
- Security model inconsistency

---

### Issue #4: Permission Check Condition May Be Incorrect in `addDomain()`

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/broadcast/BroadcastManager.java`  
**Lines**: 55-75  
**Severity**: HIGH

#### Problem
The permission check uses `>= 3` which means level 3 and above cannot create domains. However, according to the permission levels (0-3), level 3 is the lowest (restricted). This logic seems inverted.

```java
ModPermission modPermission = permissionManager.getModPermission(ownerModId);
if (modPermission.getLevel() >= 3) {  // ❌ Should this be > 3 or different logic?
    return ERROR_PERMISSION_DENIED;
}
```

#### Risk Scenario
Legitimate restricted components (level 3) might need to create private domains but are blocked.

#### Suggested Fix
Review and clarify the permission model:
```java
// If level 0 = super admin, level 3 = most restricted
// Then perhaps only level 0 and 1 should create domains?
if (modPermission.getLevel() > 1) {  // Only super admin and system level can create domains
    return ERROR_PERMISSION_DENIED;
}
```

#### Impact
- Unauthorized access or denial of service
- Security model confusion

---

### Issue #5: Version Parsing Edge Cases Not Handled

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/util/VersionComparator.java`  
**Lines**: 43-52  
**Severity**: HIGH

#### Problem
The `parseVersionPart()` method silently returns 0 for any parsing failure, which can lead to incorrect version comparisons. Additionally, the fallback parsing can still throw NumberFormatException.

```java
private static int parseVersionPart(String part) {
    try {
        return Integer.parseInt(part);
    } catch (NumberFormatException e) {
        String digits = part.replaceAll("\\D+", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);  // ❌ Still can throw!
    }
}
```

#### Risk Scenario
Version strings like "alpha", "beta-SNAPSHOT", or malformed versions can cause unexpected behavior or crashes.

#### Suggested Fix
```java
private static int parseVersionPart(String part) {
    if (part == null || part.trim().isEmpty()) {
        return 0;
    }
    try {
        return Integer.parseInt(part.trim());
    } catch (NumberFormatException e) {
        String digits = part.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e2) {
            return 0;  // Safe fallback
        }
    }
}
```

#### Impact
- Incorrect version compatibility checks
- Potential security bypass if version checks are used for security

---

### Issue #6: Path Traversal Vulnerability in BootFileManager

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/boot/BootFileManager.java`  
**Lines**: 43-59  
**Severity**: HIGH

#### Problem
While there's a path traversal check, it uses `startsWith` which can be bypassed with certain path formats on some systems.

```java
String bootFilePath = bootFile.getCanonicalPath();
String projectRootPath = projectRoot.getCanonicalPath();
if (!bootFilePath.startsWith(projectRootPath + File.separator) && 
    !bootFilePath.equals(projectRootPath)) {  // ❌ Can be bypassed
    return null;
}
```

#### Risk Scenario
On Windows, paths like `C:\..\project\evil.boot` might bypass the check depending on how canonical paths are resolved.

#### Suggested Fix
```java
String bootFilePath = bootFile.getCanonicalPath();
String projectRootPath = projectRoot.getCanonicalPath();

// More robust check using Path API
Path bootPath = Paths.get(bootFilePath);
Path rootPath = Paths.get(projectRootPath);
Path normalizedBoot = bootPath.normalize();
Path normalizedRoot = rootPath.normalize();

if (!normalizedBoot.startsWith(normalizedRoot)) {
    modLoader.getConsole().printError("Invalid boot file path: " + fileName);
    return null;
}
```

#### Impact
- Arbitrary file read vulnerability
- Potential code execution via malicious boot files

---

## 🟡 Medium Severity Issues

### Issue #7: Resource Leak - BufferedReader Not Closed in Console

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/console/Console.java`  
**Lines**: 17-216  
**Severity**: MEDIUM

#### Problem
The `BufferedReader` is created in the constructor but never closed except in the `close()` method, which may not be called on application shutdown.

```java
public Console(ModLoader modLoader) {
    this.reader = new BufferedReader(new InputStreamReader(System.in));  // ❌ Never closed
    // ...
}

public void close() {  // May not be called
    try {
        reader.close();
    } catch (IOException e) { }
}
```

#### Risk Scenario
Resource leak on abnormal application termination.

#### Suggested Fix
Add a shutdown hook:
```java
public Console(ModLoader modLoader) {
    this.modLoader = modLoader;
    this.reader = new BufferedReader(new InputStreamReader(System.in));
    this.out = System.out;
    
    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
            reader.close();
        } catch (IOException e) {
            // Ignore
        }
    }));
}
```

#### Impact
- Resource leak
- File descriptor exhaustion in long-running scenarios

---

### Issue #8: ScheduledExecutorService Not Shut Down in ConfigManager

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/config/ConfigManager.java`  
**Lines**: 25, 162-178  
**Severity**: MEDIUM

#### Problem
While `shutdown()` method exists, there's no guarantee it will be called. The `ScheduledExecutorService` will prevent JVM shutdown.

```java
private final ScheduledExecutorService scheduler;  // ❌ May not be shut down

public void shutdown() {  // Exists but may not be called
    scheduler.shutdown();
    // ...
}
```

#### Risk Scenario
Application hangs on shutdown if `shutdown()` is not explicitly called.

#### Suggested Fix
Add shutdown hook in constructor:
```java
public ConfigManager(ModLoader modLoader) {
    // ...
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
}
```

#### Impact
- Application hang on shutdown
- Resource leak

---

### Issue #9: URLClassLoader Closed Too Early in LoadCommand

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/command/LoadCommand.java`  
**Lines**: 113-143  
**Severity**: MEDIUM

#### Problem
The `URLClassLoader` is closed immediately after loading the class, but the loaded classes may still need to access resources from the JAR.

```java
try (URLClassLoader urlClassLoader = new URLClassLoader(...)) {  // ❌ Closed too early
    Class<?> modClass = urlClassLoader.loadClass(modClassName);
    modInstance = (IModAPI) constructor.newInstance();
}  // ClassLoader closed here, but modInstance may need it
```

#### Risk Scenario
Mod may fail to load resources (images, configs, etc.) at runtime because the ClassLoader is already closed.

#### Suggested Fix
Keep reference to ClassLoader and close only on mod unload:
```java
// Store ClassLoader references and close them when mod is unloaded
private final Map<IModAPI, URLClassLoader> modClassLoaders = new HashMap<>();

// In loadModFromJar:
URLClassLoader urlClassLoader = new URLClassLoader(...);
modInstance = (IModAPI) constructor.newInstance();
modClassLoaders.put(modInstance, urlClassLoader);

// In ModLoader.unloadMod():
URLClassLoader cl = modClassLoaders.remove(modInstance);
if (cl != null) cl.close();
```

#### Impact
- Mod resource loading failures
- Runtime errors in mods

---

### Issue #10: Missing Null Checks in registerJavaMod/registerKotlinMod

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/ModLoader.java`  
**Lines**: 186-232, 326-368  
**Severity**: MEDIUM

#### Problem
No null checks for critical method returns like `mod.getVersion()`, `mod.getName()`, `mod.getModId()`.

```java
public void registerJavaMod(IModAPI mod) {
    int compatibilityLevel = checkApiVersionCompatibility(mod.getVersion());  // ❌ NPE if getVersion() returns null
    // ...
    console.printError(languageManager.getMessage("modloader.error.api_version", mod.getName()));  // ❌ NPE
}
```

#### Risk Scenario
Malicious or buggy mods returning null from these methods can crash the ModLoader.

#### Suggested Fix
```java
public void registerJavaMod(IModAPI mod) {
    if (mod == null) {
        console.printError("Cannot register null mod");
        return;
    }
    
    String version = mod.getVersion();
    if (version == null || version.trim().isEmpty()) {
        console.printError("Mod version cannot be null or empty");
        return;
    }
    
    String name = mod.getName();
    if (name == null || name.trim().isEmpty()) {
        console.printError("Mod name cannot be null or empty");
        return;
    }
    
    // ... rest of validation
}
```

#### Impact
- Application crash
- Security vulnerability (null injection)

---

### Issue #11: No Fallback Mechanism for Missing Language Files

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/lang/LanguageManager.java`  
**Lines**: 31-46  
**Severity**: MEDIUM

#### Problem
When a language file fails to load, the recursive call to `loadLanguage(DEFAULT_LANG)` can cause infinite recursion if the default language file is also missing or corrupted.

```java
public void loadLanguage(String lang) {
    try (InputStream is = getClass().getResourceAsStream("/lang/" + lang + ".json")) {
        if (is != null) {
            // ...
        } else {
            loadLanguage(DEFAULT_LANG);  // ❌ Can cause infinite recursion
        }
    } catch (IOException e) {
        loadLanguage(DEFAULT_LANG);  // ❌ Can cause infinite recursion
    }
}
```

#### Risk Scenario
If both the requested and default language files are missing, infinite recursion causes StackOverflowError.

#### Suggested Fix
```java
public void loadLanguage(String lang) {
    loadLanguage(lang, false);
}

private void loadLanguage(String lang, boolean isDefaultAttempt) {
    try (InputStream is = getClass().getResourceAsStream("/lang/" + lang + ".json")) {
        if (is != null) {
            Map<String, String> langMessages = objectMapper.readValue(is, Map.class);
            messages.clear();
            messages.putAll(langMessages);
        } else if (!isDefaultAttempt) {
            // Try default language once
            loadLanguage(DEFAULT_LANG, true);
        } else {
            // Even default failed, use empty map
            messages.clear();
        }
    } catch (IOException e) {
        if (!isDefaultAttempt) {
            loadLanguage(DEFAULT_LANG, true);
        } else {
            messages.clear();  // Use empty map as last resort
        }
    }
}
```

#### Impact
- StackOverflowError on startup
- Application crash

---

### Issue #12: Race Condition in Log File Switching

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/logging/SimpleLogger.java`  
**Lines**: 128-135  
**Severity**: MEDIUM

#### Problem
The log file rotation check has a race condition where multiple threads could trigger rotation simultaneously.

```java
private void checkAndRotateLogFile() {
    LocalDate today = LocalDate.now();
    LocalDate storedDate = currentDate.get();
    
    if (storedDate == null || !storedDate.equals(today)) {
        initLogFile();  // ❌ Multiple threads could call this
    }
}
```

#### Risk Scenario
Multiple threads logging at midnight could cause log file corruption or lost log entries.

#### Suggested Fix
```java
private void checkAndRotateLogFile() {
    LocalDate today = LocalDate.now();
    LocalDate storedDate = currentDate.get();
    
    if (storedDate == null || !storedDate.equals(today)) {
        synchronized (this) {
            // Double-check after acquiring lock
            storedDate = currentDate.get();
            if (storedDate == null || !storedDate.equals(today)) {
                initLogFile();
            }
        }
    }
}
```

#### Impact
- Log file corruption
- Lost log entries

---

## 🟢 Low Severity Issues

### Issue #13: Command Parser Doesn't Support Quoted Arguments Despite Comment

**File**: `modloader/src/main/java/net/lemoncookie/neko/modloader/command/CommandSystem.java`  
**Lines**: 166-173  
**Severity**: LOW

#### Problem
The comment claims support for quoted arguments, but the implementation uses simple split.

```java
/**
 * 解析命令（支持引号参数）
 * 例如：/date now "maomao.txt" --Maomao
 */
private String[] parseCommand(String input) {
    // 简单分割，后续可以扩展为支持引号的复杂解析
    return input.split("\\s+", 2);  // ❌ Doesn't support quotes
}
```

#### Risk Scenario
Commands with spaces in quoted arguments will be parsed incorrectly.

#### Suggested Fix
Implement proper quote parsing:
```java
private String[] parseCommand(String input) {
    List<String> result = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    char quoteChar = 0;
    
    for (int i = 0; i < input.length(); i++) {
        char c = input.charAt(i);
        
        if (!inQuotes && (c == '"' || c == '\'')) {
            inQuotes = true;
            quoteChar = c;
        } else if (inQuotes && c == quoteChar) {
            inQuotes = false;
        } else if (!inQuotes && Character.isWhitespace(c)) {
            if (current.length() > 0) {
                result.add(current.toString());
                current.setLength(0);
            }
        } else {
            current.append(c);
        }
    }
    
    if (current.length() > 0) {
        result.add(current.toString());
    }
    
    if (result.isEmpty()) return new String[0];
    if (result.size() == 1) return new String[]{result.get(0), ""};
    
    String command = result.get(0);
    String arguments = String.join(" ", result.subList(1, result.size()));
    return new String[]{command, arguments};
}
```

#### Impact
- User confusion
- Command parsing errors

---

### Issue #14: Inconsistent Exception Handling Strategy

**File**: Multiple files  
**Severity**: LOW

#### Problem
Some methods catch `Throwable`, others catch specific exceptions. Some log errors, others silently ignore them.

Examples:
- `ModLoader.registerJavaMod()`: catches `Throwable`
- `Console.readLine()`: throws `IOException`
- `LanguageManager.loadLanguage()`: catches `IOException` and prints stack trace

#### Risk Scenario
Inconsistent error handling makes debugging difficult and can hide critical errors.

#### Suggested Fix
Establish a consistent exception handling policy:
1. Catch specific exceptions where possible
2. Log all errors through the broadcast system
3. Don't silently swallow exceptions
4. Document when `Throwable` is intentionally caught

#### Impact
- Difficult debugging
- Hidden errors

---

### Issue #15: Thread Safety Guarantees Not Documented

**File**: Multiple files  
**Severity**: LOW

#### Problem
Several classes use concurrent collections (`ConcurrentHashMap`, `AtomicReference`) but don't document their thread safety guarantees or limitations.

#### Risk Scenario
Developers may assume full thread safety where only partial guarantees exist.

#### Suggested Fix
Add JavaDoc documenting:
- Which methods are thread-safe
- Any required external synchronization
- Known race conditions

Example for SimpleLogger:
```java
/**
 * SimpleLogger - Thread-safe log listener.
 * 
 * Thread Safety:
 * - onMessageReceived() is thread-safe and can be called from multiple threads
 * - Log file rotation is synchronized to prevent corruption
 * - close() should only be called once during application shutdown
 */
public class SimpleLogger implements MessageListener {
    // ...
}
```

#### Impact
- Potential misuse in multi-threaded contexts
- Subtle concurrency bugs

---

## Summary

| Severity | Count | Priority |
|----------|-------|----------|
| 🔴 HIGH | 6 | Fix Immediately |
| 🟡 MEDIUM | 6 | Fix Soon |
| 🟢 LOW | 3 | Fix When Possible |

### Recommended Fix Order
1. **Issue #1** - NullPointerException in getLoadedMod()
2. **Issue #3** - Permission logic error
3. **Issue #6** - Path traversal vulnerability
4. **Issue #2** - ConcurrentModificationException risk
5. **Issue #5** - Version parsing edge cases
6. **Issue #11** - Infinite recursion in language loading

---

*This report was generated by automated code review.*
