package net.lemoncookie.neko.modloader;

import net.lemoncookie.neko.modloader.core.ModCore;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.lib.ModLibrary;
import net.lemoncookie.neko.modloader.console.Console;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.ModPermission;
import net.lemoncookie.neko.modloader.lang.LanguageManager;
import net.lemoncookie.neko.modloader.boot.BootFileManager;
import net.lemoncookie.neko.modloader.consolemod.ConsoleMod;
import net.lemoncookie.neko.modloader.systemmod.SystemMod;
import net.lemoncookie.neko.modloader.util.VersionComparator;
import net.lemoncookie.neko.modloader.logging.SimpleLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.lemoncookie.neko.modloader.api.KModAPI;

/**
 * ModLoader 主类 - Java 21 实现
 * 负责初始化核心功能、加载模组和启动控制台
 */
public class ModLoader {

    private static final String VERSION = "3.2.4";
    private static final String MIN_API_VERSION = "2.3.0";
    
    private static boolean cleaned = false;

    private final ModCore core;
    private final ModLibrary javaLibrary;
    private final List<IModAPI> javaMods;
    private final List<KModAPI> kotlinMods;
    private final Console console;
    private final BroadcastManager broadcastManager;
    private final LanguageManager languageManager;
    private final BootFileManager bootFileManager;
    private final SimpleLogger simpleLogger;

    private boolean initialized = false;

    /**
     * 构造函数
     */
    public ModLoader() {
        this.core = new ModCore();
        this.javaLibrary = new ModLibrary();
        this.javaMods = new ArrayList<>();
        this.kotlinMods = new ArrayList<>();
        this.languageManager = new LanguageManager();
        this.broadcastManager = new BroadcastManager(this);
        this.console = new Console(this);
        this.bootFileManager = new BootFileManager(this);
        
        // 自动创建 mods 文件夹
        createModsFolder();
        
        // 初始化日志系统（在其他组件之后，因为需要 modLoader 引用）
        this.simpleLogger = new SimpleLogger(this);
    }

    /**
     * 创建 mods 文件夹
     */
    private void createModsFolder() {
        File modsDir = new File("mods");
        if (!modsDir.exists()) {
            boolean created = modsDir.mkdirs();
            if (!created) {
                console.printWarning(languageManager.getMessage("modloader.error.create_mods_dir"));
            }
        }
    }

    /**
     * 初始化 ModLoader
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        // 设置核心组件的 ModLoader 引用
        core.setModLoader(this);
        javaLibrary.setModLoader(this);

        console.printLine(languageManager.getMessage("modloader.version", VERSION));
        console.printLine(languageManager.getMessage("modloader.min_api", MIN_API_VERSION));
        console.printLine();

        // 创建日志域（Hub.Log）并显示消息
        int logDomainResult = broadcastManager.addDomain("Hub.Log", false, true, "system");
        if (logDomainResult == BroadcastManager.ERROR_SUCCESS) {
            console.printSuccess(languageManager.getMessage("modloader.success.create_log_domain"));
        } else if (logDomainResult == BroadcastManager.ERROR_DOMAIN_EXISTS) {
            // 域已存在，忽略
        } else {
            console.printError(languageManager.getMessage("modloader.error.create_log_domain", logDomainResult));
        }

        // 注册日志监听器（只监听 Hub.Log 域，避免重复显示）
        broadcastManager.listen("Hub.Log", simpleLogger, "System", "SimpleLogger");

        // 创建 Hub.Command 广播域（公开公共域）
        int commandDomainResult = broadcastManager.addDomain(BroadcastManager.HUB_COMMAND, false, true, "system");
        if (commandDomainResult == BroadcastManager.ERROR_SUCCESS) {
            console.printSuccess(languageManager.getMessage("modloader.success.create_command_domain"));
        } else if (commandDomainResult == BroadcastManager.ERROR_DOMAIN_EXISTS) {
            // 域已存在，忽略
        } else {
            console.printError(languageManager.getMessage("modloader.error.create_command_domain", commandDomainResult));
        }

        // 加载系统模组（SystemMod，权限级别 0）
        loadSystemMod();

        // 加载控制台模组（ConsoleMod，权限级别 0）
        loadConsoleMod();

        // 注册内置命令监听器（SetCommand, ChangeCommand, HelpCommand 等）
        registerBuiltinCommandListeners();

        long startTime = System.currentTimeMillis();

        core.start();
        initialized = true;

        // 加载 boot 文件
        loadBootFile();

        // 显示模组加载统计信息（在 boot 文件加载完成后）
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        console.printLine(languageManager.getMessage("modloader.loaded", javaMods.size() + kotlinMods.size(), seconds));
        console.printLine();

        // 初始化完成提示
        console.printLine(languageManager.getMessage("modloader.info.init_complete"));

        // 启动控制台交互
        console.startInteractive();
    }

    /**
     * 加载 boot 文件
     */
    private void loadBootFile() {
        String bootFileName = bootFileManager.getCurrentBootFile();
        File bootFile = new File(bootFileName);
        
        // 检查是否是默认 boot 文件（auto.boot）
        boolean isDefaultBootFile = "auto.boot".equals(bootFileName);
        
        if (!bootFile.exists() || !bootFile.canRead()) {
            // 如果是默认 boot 文件不存在，自动创建
            if (isDefaultBootFile) {
                console.printLine(languageManager.getMessage("modloader.info.create_default_boot"));
                bootFileManager.generateAutoBoot();
                
                // 重新检查文件是否创建成功
                if (bootFile.exists() && bootFile.canRead()) {
                    console.printLine(languageManager.getMessage("modloader.info.loading_boot", bootFileName));
                    bootFileManager.executeBootFile(bootFileName);
                } else {
                    console.printError(
                        languageManager.getMessage("boot.error.not_found")
                    );
                }
            } else {
                // 用户指定的 boot 文件不存在，只显示错误
                console.printError(
                    languageManager.getMessage("boot.error.not_found")
                );
            }
        } else {
            console.printLine(languageManager.getMessage("modloader.info.loading_boot", bootFileName));
            bootFileManager.executeBootFile(bootFileName);
        }
    }

    /**
     * 加载系统模组（优先加载）
     */
    private void loadSystemMod() {
        // 创建并加载系统模组
        SystemMod systemMod = new SystemMod();
        registerJavaMod(systemMod);
    }

    /**
     * 加载控制台模组（优先加载）
     */
    private void loadConsoleMod() {
        // 创建并加载控制台模组
        ConsoleMod consoleMod = new ConsoleMod();
        registerJavaMod(consoleMod);
    }

    /**
     * 注册内置命令监听器
     * 所有内置命令都监听 Hub.Command 广播域
     */
    private void registerBuiltinCommandListeners() {
        // 注册命令监听器到 Hub.Command 域
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.SetCommand(this), "system", "SetCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.ChangeCommand(this), "system", "ChangeCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.HelpCommand(this), "system", "HelpCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.ClearCommand(this), "system", "ClearCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.LoadCommand(this), "system", "LoadCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.UnloadCommand(this), "system", "UnloadCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.ListCommand(this), "system", "ListCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.ExitCommand(this), "system", "ExitCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.SayCommand(this), "system", "SayCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.ListenCommand(this), "system", "ListenCommand");
        broadcastManager.listen(BroadcastManager.HUB_COMMAND, new net.lemoncookie.neko.modloader.command.AutobootCommand(this), "system", "AutobootCommand");
    }

    /**
     * 注册 Java 模组
     */
    public void registerJavaMod(IModAPI mod) {
        if (mod == null) {
            console.printError("Cannot register null mod");
            return;
        }
        
        String version = mod.getVersion();
        if (version == null || version.trim().isEmpty()) {
            console.printError("Mod version cannot be null or empty: " + mod.getName());
            return;
        }
        
        String name = mod.getName();
        if (name == null || name.trim().isEmpty()) {
            console.printError("Mod name cannot be null or empty");
            return;
        }
        
        String modId = mod.getModId();
        if (modId == null || modId.trim().isEmpty()) {
            console.printError("Mod ID cannot be null or empty for mod: " + name);
            return;
        }
        
        // 检查模组是否显式声明了 API 版本（防止旧模组未实现 getApiVersion()）
        String apiVersion = mod.getApiVersion();
        if (apiVersion == null || apiVersion.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.api_version_not_declared", name));
            broadcastManager.broadcast("Hub.Log", "[ERROR] " + languageManager.getMessage("modloader.error.api_version_not_declared", name), "ModLoader");
            return;
        }
        
        // 检查 API 版本是否与模组版本相同（如果是，说明可能没有显式实现 getApiVersion()，使用的是默认实现）
        if (apiVersion.equals(version)) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version_equals_mod_version", name));
        }
        
        // 检查模组 ID 是否已存在（禁止同名模组）
        for (IModAPI loadedMod : javaMods) {
            if (loadedMod != null && loadedMod.getModId() != null && loadedMod.getModId().equals(modId)) {
                console.printError("Mod ID already exists: " + modId);
                broadcastManager.broadcast("Hub.Log", "[ERROR] Duplicate mod ID: " + modId, "ModLoader");
                return;
            }
        }
        
        // 检查 API 版本兼容性
        int compatibilityLevel = checkApiVersionCompatibility(apiVersion);
        
        if (compatibilityLevel == 2) {
            // 检查是版本过高还是过低
            if (VersionComparator.isApiVersionTooHigh(mod.getApiVersion(), MIN_API_VERSION)) {
                console.printError(languageManager.getMessage("modloader.error.api_version_too_high", name));
            } else {
                console.printError(languageManager.getMessage("modloader.error.api_version_too_low", name));
            }
            return;
        }
        
        if (compatibilityLevel == 1) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version", 
                name, mod.getApiVersion(), MIN_API_VERSION));
        }

        // 检查模组依赖
        if (!checkDependencies(mod)) {
            return;
        }

        // 注册模组，设置权限
        // SystemMod 和 ConsoleMod 需要 SUPER_ADMIN 权限，其他模组默认为 NORMAL_COMPONENT
        if ("system".equals(modId) || "console-mod".equals(modId)) {
            broadcastManager.getPermissionManager().setModPermission(modId, ModPermission.SUPER_ADMIN);
        } else {
            broadcastManager.getPermissionManager().setModPermission(modId, ModPermission.NORMAL_COMPONENT);
        }

        javaMods.add(mod);
        
        try {
            mod.onLoad(this);
        } catch (Throwable e) {
            String errorMsg = "Error loading mod '" + name + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
        }
        
        try {
            mod.registerBroadcastListeners(this, modId);
        } catch (Throwable e) {
            String errorMsg = "Error registering broadcast listeners for mod '" + name + "': " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        String successMsg = "Mod loaded successfully: " + name + " v" + version;
        console.printSuccess(successMsg);
        broadcastManager.broadcast("Hub.Log", "[SUCCESS] " + successMsg, "ModLoader");
    }

    /**
     * 检查模组依赖
     * 
     * @param mod 要检查的模组
     * @return 依赖是否满足
     */
    private boolean checkDependencies(IModAPI mod) {
        if (mod == null) {
            return false;
        }
        
        List<ModDependency> dependencies;
        try {
            dependencies = mod.getDependencies();
            if (dependencies == null) {
                return true; // 返回 null 视为无依赖
            }
        } catch (Throwable e) {
            String errorMsg = "Error getting dependencies from mod '" + mod.getName() + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
            return false;
        }
        
        if (dependencies.isEmpty()) {
            return true; // 无依赖，直接通过
        }
        
        for (ModDependency dependency : dependencies) {
            if (dependency == null) {
                continue; // 跳过 null 依赖项
            }
            
            String requiredModId;
            String requiredVersion;
            try {
                requiredModId = dependency.getModId();
                requiredVersion = dependency.getMinVersion();
                
                if (requiredModId == null || requiredModId.trim().isEmpty()) {
                    String errorMsg = "Mod dependency has null or empty modId: " + mod.getName();
                    console.printWarning(errorMsg);
                    broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
                    continue;
                }
                
                if (requiredVersion == null || requiredVersion.trim().isEmpty()) {
                    String errorMsg = "Mod dependency has null or empty version: " + requiredModId;
                    console.printWarning(errorMsg);
                    broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
                    continue;
                }
            } catch (Throwable e) {
                String errorMsg = "Error reading dependency from mod '" + mod.getName() + "': " + e.getMessage();
                console.printWarning(errorMsg);
                broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
                continue;
            }
            
            // 检查依赖模组是否已加载
            IModAPI loadedMod;
            try {
                loadedMod = getLoadedMod(requiredModId);
            } catch (Throwable e) {
                String errorMsg = "Error checking dependency '" + requiredModId + "' for mod '" + mod.getName() + "': " + e.getMessage();
                console.printError(errorMsg);
                broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                return false;
            }
            
            if (loadedMod == null) {
                // 依赖模组未加载
                String errorMsg = String.format(
                    "模组 [%s] 所需的依赖 [%s-%s] 不存在或未加载",
                    mod.getModId(),
                    requiredModId,
                    requiredVersion
                );
                console.printError(errorMsg);
                broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                return false;
            }
            
            // 检查依赖模组版本
            try {
                if (VersionComparator.compare(loadedMod.getVersion(), requiredVersion) < 0) {
                    String errorMsg = String.format(
                        "模组 [%s] 所需的依赖 [%s-%s] 版本过低（当前版本：%s）",
                        mod.getModId(),
                        requiredModId,
                        requiredVersion,
                        loadedMod.getVersion()
                    );
                    console.printError(errorMsg);
                    broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                    return false;
                }
            } catch (Throwable e) {
                String errorMsg = "Error comparing versions for dependency '" + requiredModId + "': " + e.getMessage();
                console.printError(errorMsg);
                broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                return false;
            }
        }
        
        return true; // 所有依赖都满足
    }

    /**
     * 根据模组 ID 获取已加载的模组
     * 
     * @param modId 模组 ID
     * @return 模组实例，未找到返回 null
     */
    private IModAPI getLoadedMod(String modId) {
        if (modId == null || modId.trim().isEmpty()) {
            return null;
        }
        
        try {
            for (IModAPI loadedMod : javaMods) {
                if (loadedMod == null) {
                    continue;
                }
                
                String currentModId;
                try {
                    currentModId = loadedMod.getModId();
                } catch (Throwable e) {
                    // 忽略获取失败的模组
                    continue;
                }
                
                if (currentModId != null && currentModId.equals(modId)) {
                    return loadedMod;
                }
            }
        } catch (Throwable e) {
            // 极端情况下遍历失败，返回 null
            String errorMsg = "Error iterating loaded mods: " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        return null;
    }

    /**
     * 检查 API 版本兼容性
     * @param modApiVersion 模组 API 版本
     * @return 兼容性级别：0=完全兼容，1=兼容但需要警告，2=不兼容
     */
    private int checkApiVersionCompatibility(String modApiVersion) {
        return VersionComparator.checkCompatibilityLevel(modApiVersion, MIN_API_VERSION);
    }

    /**
     * 获取 Java 版库
     */
    public ModLibrary getJavaLibrary() {
        return javaLibrary;
    }

    /**
     * 获取已加载的 Java 模组列表
     */
    public List<IModAPI> getJavaMods() {
        return new ArrayList<>(javaMods);
    }

    /**
     * 注册 Kotlin 模组
     */
    public void registerKotlinMod(KModAPI mod) {
        if (mod == null) {
            console.printError("Cannot register null mod");
            return;
        }
        
        String modId = mod.getModId();
        if (modId == null || modId.trim().isEmpty()) {
            console.printError("Mod ID cannot be null or empty for mod: " + mod.getName());
            return;
        }
        
        // 检查模组 ID 是否已存在（禁止同名模组）
        for (KModAPI loadedMod : kotlinMods) {
            if (loadedMod != null && loadedMod.getModId() != null && loadedMod.getModId().equals(modId)) {
                console.printError("Mod ID already exists: " + modId);
                broadcastManager.broadcast("Hub.Log", "[ERROR] Duplicate mod ID: " + modId, "ModLoader");
                return;
            }
        }
        
        String apiVersion = mod.getInfo().getApiVersion();
        
        // 检查 API 版本是否与模组版本相同（如果是，可能说明没有正确设置）
        if (apiVersion.equals(mod.getInfo().getVersion())) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version_equals_mod_version", mod.getName()));
        }
        
        // 检查 API 版本兼容性
        int compatibilityLevel = checkApiVersionCompatibility(apiVersion);
        
        if (compatibilityLevel == 2) {
            // 检查是版本过高还是过低
            if (VersionComparator.isApiVersionTooHigh(mod.getInfo().getApiVersion(), MIN_API_VERSION)) {
                console.printError(languageManager.getMessage("modloader.error.api_version_too_high", mod.getName()));
            } else {
                console.printError(languageManager.getMessage("modloader.error.api_version_too_low", mod.getName()));
            }
            return;
        }
        
        if (compatibilityLevel == 1) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version", 
                mod.getName(), mod.getInfo().getApiVersion(), MIN_API_VERSION));
        }

        // 注册模组，设置权限
        // SystemMod 和 ConsoleMod 需要 SUPER_ADMIN 权限，其他模组默认为 NORMAL_COMPONENT
        if ("system".equals(modId) || "console-mod".equals(modId)) {
            broadcastManager.getPermissionManager().setModPermission(modId, ModPermission.SUPER_ADMIN);
        } else {
            broadcastManager.getPermissionManager().setModPermission(modId, ModPermission.NORMAL_COMPONENT);
        }

        kotlinMods.add(mod);
        
        try {
            mod.onLoad(this);
        } catch (Throwable e) {
            String errorMsg = "Error loading mod '" + mod.getName() + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
        }
        
        try {
            mod.registerBroadcastListeners(this, mod.getModId());
        } catch (Throwable e) {
            String errorMsg = "Error registering broadcast listeners for mod '" + mod.getName() + "': " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        String successMsg = "Mod loaded successfully: " + mod.getName() + " v" + mod.getInfo().getVersion();
        console.printSuccess(successMsg);
        broadcastManager.broadcast("Hub.Log", "[SUCCESS] " + successMsg, "ModLoader");
    }

    /**
     * 获取已加载的 Kotlin 模组列表
     */
    public List<KModAPI> getKotlinMods() {
        return new ArrayList<>(kotlinMods);
    }

    /**
     * 卸载所有模组
     */
    public void unloadAll() {
        try {
            // 卸载 Java 模组
            for (IModAPI mod : javaMods) {
                if (mod != null) {
                    try {
                        mod.onUnload();
                    } catch (Throwable e) {
                        String errorMsg = "Error unloading mod '" + mod.getName() + "': " + e.getMessage();
                        console.printError(errorMsg);
                        broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                    }
                }
            }
            javaMods.clear();
            
            // 卸载 Kotlin 模组
            for (KModAPI mod : kotlinMods) {
                if (mod != null) {
                    try {
                        mod.onUnload();
                    } catch (Throwable e) {
                        String errorMsg = "Error unloading mod '" + mod.getName() + "': " + e.getMessage();
                        console.printError(errorMsg);
                        broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                    }
                }
            }
            kotlinMods.clear();
            
            console.printLine(languageManager.getMessage("modloader.success.unloaded"));
        } catch (Throwable e) {
            String errorMsg = "Critical error during unloadAll: " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
        }
    }

    /**
     * 卸载指定模组
     * @param modName 模组名称或模组 ID
     * @return 是否卸载成功
     */
    public boolean unloadMod(String modName) {
        if (modName == null || modName.trim().isEmpty()) {
            console.printError("Mod name cannot be null or empty");
            return false;
        }
        
        try {
            // 尝试从 Java 模组中查找并卸载
            IModAPI javaModToUnload = null;
            try {
                for (IModAPI javaMod : javaMods) {
                    if (javaMod == null) {
                        continue;
                    }
                    
                    String modId;
                    String name;
                    try {
                        modId = javaMod.getModId();
                        name = javaMod.getName();
                    } catch (Throwable e) {
                        // 忽略获取失败的模组
                        continue;
                    }
                    
                    if ((modId != null && modId.equals(modName)) || (name != null && name.equals(modName))) {
                        javaModToUnload = javaMod;
                        break;
                    }
                }
            } catch (Throwable e) {
                String errorMsg = "Error iterating Java mods: " + e.getMessage();
                console.printWarning(errorMsg);
                broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
            }
            
            if (javaModToUnload != null) {
                try {
                    javaModToUnload.onUnload();
                } catch (Throwable e) {
                    String errorMsg = "Error unloading mod '" + modName + "': " + e.getMessage();
                    console.printError(errorMsg);
                    broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                }
                javaMods.removeIf(mod -> {
                    if (mod == null) return false;
                    try {
                        return (mod.getModId() != null && mod.getModId().equals(modName)) || 
                               (mod.getName() != null && mod.getName().equals(modName));
                    } catch (Throwable e) {
                        return false;
                    }
                });
                String successMsg = "Mod unloaded successfully: " + modName;
                console.printSuccess(successMsg);
                broadcastManager.broadcast("Hub.Log", "[SUCCESS] " + successMsg, "ModLoader");
                return true;
            }

            // 尝试从 Kotlin 模组中查找并卸载
            KModAPI kotlinModToUnload = null;
            try {
                for (KModAPI kotlinMod : kotlinMods) {
                    if (kotlinMod == null) {
                        continue;
                    }
                    
                    String modId;
                    String name;
                    try {
                        modId = kotlinMod.getModId();
                        name = kotlinMod.getName();
                    } catch (Throwable e) {
                        // 忽略获取失败的模组
                        continue;
                    }
                    
                    if ((modId != null && modId.equals(modName)) || (name != null && name.equals(modName))) {
                        kotlinModToUnload = kotlinMod;
                        break;
                    }
                }
            } catch (Throwable e) {
                String errorMsg = "Error iterating Kotlin mods: " + e.getMessage();
                console.printWarning(errorMsg);
                broadcastManager.broadcast("Hub.Log", "[WARNING] " + errorMsg, "ModLoader");
            }
            
            if (kotlinModToUnload != null) {
                try {
                    kotlinModToUnload.onUnload();
                } catch (Throwable e) {
                    String errorMsg = "Error unloading mod '" + modName + "': " + e.getMessage();
                    console.printError(errorMsg);
                    broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
                }
                kotlinMods.removeIf(mod -> {
                    if (mod == null) return false;
                    try {
                        return (mod.getModId() != null && mod.getModId().equals(modName)) || 
                               (mod.getName() != null && mod.getName().equals(modName));
                    } catch (Throwable e) {
                        return false;
                    }
                });
                String successMsg = "Mod unloaded successfully: " + modName;
                console.printSuccess(successMsg);
                broadcastManager.broadcast("Hub.Log", "[SUCCESS] " + successMsg, "ModLoader");
                return true;
            }
            
            // 未找到模组
            console.printError(languageManager.getMessage("modloader.error.mod_not_found", modName));
            return false;
        } catch (Throwable e) {
            String errorMsg = "Critical error in unloadMod: " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Log", "[ERROR] " + errorMsg, "ModLoader");
            return false;
        }
    }

    /**
     * 获取核心实例
     */
    public ModCore getCore() {
        return core;
    }

    /**
     * 获取控制台实例
     */
    public Console getConsole() {
        return console;
    }

    /**
     * 获取广播域管理器
     */
    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    /**
     * 获取语言管理器
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * 获取 Boot 文件管理器
     */
    public BootFileManager getBootFileManager() {
        return bootFileManager;
    }

    /**
     * 获取版本
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * 获取最低 API 版本
     */
    public static String getMinApiVersion() {
        return MIN_API_VERSION;
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取简单日志器实例
     */
    public SimpleLogger getSimpleLogger() {
        return simpleLogger;
    }

    /**
     * 主方法
     */
    public static void main(String[] args) {
        ModLoader loader = new ModLoader();
        loader.initialize();
        
        // 添加关闭钩子（处理 Ctrl+C）
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (cleaned) return;
            cleaned = true;
            
            System.out.println("\n[Neko-Hub] Shutdown hook triggered...");
            
            try {
                // 标准关闭顺序：1. 优先卸载所有模组
                loader.unloadAll();
                
                // 2. 关闭控制台（中断阻塞线程 + 关闭流）
                Console console = loader.getConsole();
                console.running = false;
                if (console.getConsoleThread() != null) {
                    console.getConsoleThread().interrupt();
                }
                console.close();
                
                // 3. 最后关闭日志等收尾资源
                loader.getSimpleLogger().close();
                
                System.out.println("[Neko-Hub] Cleanup complete.");
            } catch (Throwable t) {
                System.err.println("[ERROR] Shutdown failed: " + t.getMessage());
            }
        }));
        
        // 主线程保持运行，等待控制台输入
        // 控制台线程现在是非守护线程，JVM 会等待其终止
        // 使用无限休眠保持主线程存活
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            // 被中断，准备退出
            Thread.currentThread().interrupt();
        }
    }
}
