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
import net.lemoncookie.neko.modloader.core.ModCoreConfig;

/**
 * ModLoader 主类 - Java 21 实现
 * 负责初始化核心功能、加载模组和启动控制台
 */
public class ModLoader {

    private static final String VERSION = "3.2.5";
    private static final String MIN_API_VERSION = "2.3.0";
    
    // 自定义应用名称和版本（通过 --application 参数设置）
    private String applicationName = null;
    private String applicationVersion = null;

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
        this(null, null);
    }
    
    /**
     * 构造函数（带自定义应用名称和版本）
     * @param applicationName 自定义应用名称，为 null 时使用默认值
     * @param applicationVersion 自定义应用版本，为 null 时使用默认值
     */
    public ModLoader(String applicationName, String applicationVersion) {
        String trimmedName = (applicationName != null) ? applicationName.trim() : "";
        String trimmedVersion = (applicationVersion != null) ? applicationVersion.trim() : "";
        this.applicationName = !trimmedName.isEmpty() ? trimmedName : null;
        this.applicationVersion = !trimmedVersion.isEmpty() ? trimmedVersion : null;
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

        // 注入核心依赖组件
        ModCoreConfig config = new ModCoreConfig.Builder()
                .console(console)
                .languageManager(languageManager)
                .broadcastManager(broadcastManager)
                .loaderVersion(VERSION)
                .minApiVersion(MIN_API_VERSION)
                .javaMods(javaMods)
                .kotlinMods(kotlinMods)
                .build();
        core.injectComponents(config);
        javaLibrary.setModLoader(this);

        // 显示版本信息
        if (applicationName != null && !applicationName.isEmpty()) {
            // 使用自定义应用名称和版本
            String displayVersion = applicationVersion != null ? applicationVersion : "";
            console.printLine(applicationName + " " + displayVersion);
            // 隐去最低 API 版本行
        } else {
            // 使用默认版本信息
            console.printLine(languageManager.getMessage("modloader.version", VERSION));
            console.printLine(languageManager.getMessage("modloader.min_api", MIN_API_VERSION));
        }
        console.printLine();

        // Hub.System 已在 BroadcastManager 初始化时创建（作为日志专用域）
        console.printSuccess(languageManager.getMessage("modloader.success.create_log_domain"));

        // 注册日志监听器（只监听 Hub.System 域，避免重复显示）
        broadcastManager.listen(BroadcastManager.HUB_SYSTEM, simpleLogger, "System", "SimpleLogger");

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
            console.printError(languageManager.getMessage("modloader.error.null_mod"));
            return;
        }

        String name = mod.getName();
        String version = mod.getVersion();
        String modId = mod.getModId();

        // 使用 ModCore 进行注册和验证
        boolean success = core.validateAndRegisterJavaMod(mod);
        
        if (success) {
            javaMods.add(mod);
            // 注册成功后调用 onLoad 和 registerBroadcastListeners
            try {
                mod.onLoad(this);
            } catch (Throwable e) {
                String errorMsg = languageManager.getMessage("modloader.error.loading_mod", name, e.getMessage());
                console.printError(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
            }
            
            try {
                mod.registerBroadcastListeners(this, modId);
            } catch (Throwable e) {
                String errorMsg = languageManager.getMessage("modloader.error.register_listeners", name, e.getMessage());
                console.printWarning(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModLoader");
            }
            
            String successMsg = languageManager.getMessage("modloader.success.load_mod", name, version);
            console.printSuccess(successMsg);
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[SUCCESS] " + successMsg, "ModLoader");
        }
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
            console.printError(languageManager.getMessage("modloader.error.null_mod"));
            return;
        }

        String modId = mod.getModId();

        // 使用 ModCore 进行注册和验证
        boolean success = core.validateAndRegisterKotlinMod(mod);
        
        if (success) {
            kotlinMods.add(mod);
            // 注册成功后调用 onLoad 和 registerBroadcastListeners
            try {
                mod.onLoad(this);
            } catch (Throwable e) {
                String errorMsg = languageManager.getMessage("modloader.error.loading_mod", mod.getName(), e.getMessage());
                console.printError(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
            }
            
            try {
                mod.registerBroadcastListeners(this, modId);
            } catch (Throwable e) {
                String errorMsg = languageManager.getMessage("modloader.error.register_listeners", mod.getName(), e.getMessage());
                console.printWarning(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModLoader");
            }
            
            String successMsg = languageManager.getMessage("modloader.success.load_mod", mod.getName(), mod.getVersion());
            console.printSuccess(successMsg);
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[SUCCESS] " + successMsg, "ModLoader");
        }
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
                        broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
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
                        broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
                    }
                }
            }
            kotlinMods.clear();
            
            console.printLine(languageManager.getMessage("modloader.success.unloaded"));
        } catch (Throwable e) {
            String errorMsg = "Critical error during unloadAll: " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
        }
    }

    /**
     * 卸载指定模组
     * @param modName 模组名称或模组 ID
     * @return 是否卸载成功
     */
    public boolean unloadMod(String modName) {
        if (modName == null || modName.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_name"));
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
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModLoader");
            }
            
            if (javaModToUnload != null) {
                try {
                    javaModToUnload.onUnload();
                } catch (Throwable e) {
                    String errorMsg = "Error unloading mod '" + modName + "': " + e.getMessage();
                    console.printError(errorMsg);
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
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
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[SUCCESS] " + successMsg, "ModLoader");
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
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModLoader");
            }
            
            if (kotlinModToUnload != null) {
                try {
                    kotlinModToUnload.onUnload();
                } catch (Throwable e) {
                    String errorMsg = "Error unloading mod '" + modName + "': " + e.getMessage();
                    console.printError(errorMsg);
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
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
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[SUCCESS] " + successMsg, "ModLoader");
                return true;
            }
            
            // 未找到模组
            console.printError(languageManager.getMessage("modloader.error.mod_not_found", modName));
            return false;
        } catch (Throwable e) {
            String errorMsg = "Critical error in unloadMod: " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModLoader");
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
        String appName = null;
        String appVersion = null;
        
        // 解析命令行参数
        for (int i = 0; i < args.length; i++) {
            if ("--application".equals(args[i]) && i + 2 < args.length) {
                appName = args[i + 1];
                appVersion = args[i + 2];
                break;
            }
        }
        
        ModLoader loader = new ModLoader(appName, appVersion);
        loader.initialize();
        
        // 保持主线程运行，等待控制台输入
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
