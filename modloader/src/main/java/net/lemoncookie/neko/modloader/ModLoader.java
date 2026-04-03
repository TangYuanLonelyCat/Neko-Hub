package net.lemoncookie.neko.modloader;

import net.lemoncookie.neko.modloader.core.ModCore;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.lib.ModLibrary;
import net.lemoncookie.neko.modloader.command.CommandSystem;
import net.lemoncookie.neko.modloader.console.Console;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.lang.LanguageManager;
import net.lemoncookie.neko.modloader.boot.BootFileManager;
import net.lemoncookie.neko.modloader.config.ConfigManager;
import net.lemoncookie.neko.modloader.consolemod.ConsoleMod;
import net.lemoncookie.neko.modloader.util.VersionComparator;
import net.lemoncookie.neko.modloader.logging.SimpleLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.lemoncookie.neko.modloader.api.ModAPI;

/**
 * ModLoader 主类 - Java 21 实现
 * 负责初始化核心功能、加载模组和启动控制台
 */
public class ModLoader {

    private static final String VERSION = "2.0.0";
    private static final String MIN_API_VERSION = "1.0.0";
    private static final String GITHUB_VERSION = "v1.0.0";

    private final ModCore core;
    private final ModLibrary javaLibrary;
    private final List<IModAPI> javaMods;
    private final List<ModAPI> kotlinMods;
    private final Console console;
    private final CommandSystem commandSystem;
    private final BroadcastManager broadcastManager;
    private final LanguageManager languageManager;
    private final BootFileManager bootFileManager;
    private final ConfigManager configManager;
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
        this.commandSystem = new CommandSystem(this);
        this.bootFileManager = new BootFileManager(this);
        this.configManager = new ConfigManager(this);
        
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
                console.printWarning("Failed to create mods directory");
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

        // 注册日志监听器（在加载控制台模组之前，确保所有消息都被记录）
        broadcastManager.listen("Hub.ALL", simpleLogger, "System", "SimpleLogger");
        broadcastManager.listen("Hub.System", simpleLogger, "System", "SimpleLogger");
        broadcastManager.listen("Hub.Console", simpleLogger, "System", "SimpleLogger");

        console.printLine(languageManager.getMessage("modloader.version", VERSION, GITHUB_VERSION));
        console.printLine(languageManager.getMessage("modloader.min_api", MIN_API_VERSION));
        console.printLine();

        long startTime = System.currentTimeMillis();

        // 加载控制台模组（优先加载）
        loadConsoleMod();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        console.printLine(languageManager.getMessage("modloader.loaded", javaMods.size() + kotlinMods.size(), seconds));
        console.printLine();

        core.start();
        initialized = true;

        // 加载 boot 文件
        loadBootFile();

        // 初始化完成提示
        console.printLine("ModLoader initialization completed. Starting interactive console...");

        // 启动控制台交互
        console.startInteractive();
    }

    /**
     * 加载 boot 文件
     */
    private void loadBootFile() {
        String bootFileName = configManager.getBootFile();
        File bootFile = new File(bootFileName);
        
        // 检查是否是默认 boot 文件（auto.boot）
        boolean isDefaultBootFile = "auto.boot".equals(bootFileName);
        
        if (!bootFile.exists() || !bootFile.canRead()) {
            // 如果是默认 boot 文件不存在，自动创建
            if (isDefaultBootFile) {
                console.printLine("Creating default auto.boot file...");
                bootFileManager.generateAutoBoot();
                
                // 重新检查文件是否创建成功
                if (bootFile.exists() && bootFile.canRead()) {
                    console.printLine("Loading boot file: " + bootFileName);
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
            console.printLine("Loading boot file: " + bootFileName);
            bootFileManager.executeBootFile(bootFileName);
        }
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
     * 注册 Java 模组
     */
    public void registerJavaMod(IModAPI mod) {
        // 检查 API 版本
        if (isApiVersionIncompatible(mod.getVersion())) {
            console.printError(languageManager.getMessage("modloader.error.api_version", mod.getName()));
            return;
        }

        // 检查依赖
        if (!checkDependencies(mod)) {
            return; // 依赖不满足，拒绝加载
        }

        javaMods.add(mod);
        
        // 调用模组加载方法，捕获异常确保框架稳定
        try {
            mod.onLoad(this);
        } catch (Exception e) {
            String errorMsg = "Error loading mod '" + mod.getName() + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Console", "[ERROR] " + errorMsg, "ModLoader");
        }
        
        // 注册命令
        try {
            mod.registerCommands(this);
        } catch (Exception e) {
            String errorMsg = "Error registering commands for mod '" + mod.getName() + "': " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Console", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        // 注册广播域监听器
        try {
            mod.registerBroadcastListeners(this);
        } catch (Exception e) {
            String errorMsg = "Error registering broadcast listeners for mod '" + mod.getName() + "': " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Console", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        console.printSuccess(languageManager.getMessage("modloader.success.loaded", mod.getName(), mod.getVersion()));
    }

    /**
     * 检查模组依赖
     * 
     * @param mod 要检查的模组
     * @return 依赖是否满足
     */
    private boolean checkDependencies(IModAPI mod) {
        List<ModDependency> dependencies = mod.getDependencies();
        
        if (dependencies.isEmpty()) {
            return true; // 无依赖，直接通过
        }
        
        for (ModDependency dependency : dependencies) {
            String requiredModId = dependency.getModId();
            String requiredVersion = dependency.getMinVersion();
            
            // 检查依赖模组是否已加载
            IModAPI loadedMod = getLoadedMod(requiredModId);
            
            if (loadedMod == null) {
                // 依赖模组未加载
                String errorMsg = String.format(
                    "模组 [%s] 所需的依赖 [%s-%s] 不存在或未加载",
                    mod.getModId(),
                    requiredModId,
                    requiredVersion
                );
                console.printError(errorMsg);
                broadcastManager.broadcast("Hub.Console", "[ERROR] " + errorMsg, "ModLoader");
                return false;
            }
            
            // 检查依赖模组版本
            if (VersionComparator.compare(loadedMod.getVersion(), requiredVersion) < 0) {
                String errorMsg = String.format(
                    "模组 [%s] 所需的依赖 [%s-%s] 版本过低（当前版本：%s）",
                    mod.getModId(),
                    requiredModId,
                    requiredVersion,
                    loadedMod.getVersion()
                );
                console.printError(errorMsg);
                broadcastManager.broadcast("Hub.Console", "[ERROR] " + errorMsg, "ModLoader");
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
        for (IModAPI loadedMod : javaMods) {
            if (loadedMod.getModId().equals(modId)) {
                return loadedMod;
            }
        }
        return null;
    }

    /**
     * 检查 API 版本兼容性
     */
    private boolean isApiVersionCompatible(String modApiVersion) {
        // 使用语义化版本比较
        return VersionComparator.isCompatible(modApiVersion, MIN_API_VERSION);
    }

    /**
     * 检查 API 版本不兼容
     * @param modApiVersion 模组 API 版本
     * @return 是否不兼容
     */
    private boolean isApiVersionIncompatible(String modApiVersion) {
        return !isApiVersionCompatible(modApiVersion);
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
    public void registerKotlinMod(ModAPI mod) {
        if (isApiVersionIncompatible(mod.getInfo().getVersion())) {
            console.printError(languageManager.getMessage("modloader.error.api_version", mod.getName()));
            return;
        }

        kotlinMods.add(mod);
        
        // 调用模组加载方法，捕获异常确保框架稳定
        try {
            mod.onLoad(this);
        } catch (Exception e) {
            String errorMsg = "Error loading mod '" + mod.getName() + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast("Hub.Console", "[ERROR] " + errorMsg, "ModLoader");
        }
        
        // 注册命令
        try {
            mod.registerCommands(this);
        } catch (Exception e) {
            String errorMsg = "Error registering commands for mod '" + mod.getName() + "': " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Console", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        // 注册广播域监听器
        try {
            mod.registerBroadcastListeners(this);
        } catch (Exception e) {
            String errorMsg = "Error registering broadcast listeners for mod '" + mod.getName() + "': " + e.getMessage();
            console.printWarning(errorMsg);
            broadcastManager.broadcast("Hub.Console", "[WARNING] " + errorMsg, "ModLoader");
        }
        
        console.printSuccess(languageManager.getMessage("modloader.success.loaded", mod.getName(), mod.getInfo().getVersion()));
    }

    /**
     * 获取已加载的 Kotlin 模组列表
     */
    public List<ModAPI> getKotlinMods() {
        return new ArrayList<>(kotlinMods);
    }

    /**
     * 卸载所有模组
     */
    public void unloadAll() {
        javaMods.forEach(IModAPI::onUnload);
        javaMods.clear();
        kotlinMods.forEach(ModAPI::onUnload);
        kotlinMods.clear();
        console.printLine(languageManager.getMessage("modloader.success.unloaded"));
    }

    /**
     * 卸载指定模组
     * @param modName 模组名称或模组 ID
     * @return 是否卸载成功
     */
    public boolean unloadMod(String modName) {
        // 尝试从 Java 模组中查找并卸载
        for (IModAPI javaMod : javaMods) {
            if (javaMod.getModId().equals(modName) || javaMod.getName().equals(modName)) {
                try {
                    javaMod.onUnload();
                } catch (Exception e) {
                    String errorMsg = "Error unloading mod '" + modName + "': " + e.getMessage();
                    console.printError(errorMsg);
                    broadcastManager.broadcast("Hub.Console", "[ERROR] " + errorMsg, "ModLoader");
                }
                javaMods.removeIf(mod -> mod.getModId().equals(modName) || mod.getName().equals(modName));
                console.printSuccess("Mod unloaded successfully: " + modName);
                return true;
            }
        }

        // 尝试从 Kotlin 模组中查找并卸载
        for (ModAPI kotlinMod : kotlinMods) {
            if (kotlinMod.getModId().equals(modName) || kotlinMod.getName().equals(modName)) {
                try {
                    kotlinMod.onUnload();
                } catch (Exception e) {
                    String errorMsg = "Error unloading mod '" + modName + "': " + e.getMessage();
                    console.printError(errorMsg);
                    broadcastManager.broadcast("Hub.Console", "[ERROR] " + errorMsg, "ModLoader");
                }
                kotlinMods.removeIf(mod -> mod.getModId().equals(modName) || mod.getName().equals(modName));
                console.printSuccess("Mod unloaded successfully: " + modName);
                return true;
            }
        }

        console.printError("Mod not found: " + modName);
        return false;
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
     * 获取命令系统实例
     */
    public CommandSystem getCommandSystem() {
        return commandSystem;
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
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
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
     * 获取 GitHub 版本
     */
    public static String getGithubVersion() {
        return GITHUB_VERSION;
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
        
        // 保持主线程运行，等待控制台输入
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
