package net.lemoncookie.neko.modloader;

import net.lemoncookie.neko.modloader.core.ModCore;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.lib.ModLibrary;
import net.lemoncookie.neko.modloader.command.CommandSystem;
import net.lemoncookie.neko.modloader.console.Console;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.lang.LanguageManager;
import net.lemoncookie.neko.modloader.broadcast.ModPermission;
import net.lemoncookie.neko.modloader.boot.BootFileManager;
import net.lemoncookie.neko.modloader.config.ConfigManager;
import net.lemoncookie.neko.modloader.consolemod.ConsoleMod;

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

    private static final String VERSION = "1.0.0";
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
        this.console = new Console(this);
        this.commandSystem = new CommandSystem(this);
        this.broadcastManager = new BroadcastManager(this);
        this.bootFileManager = new BootFileManager(this);
        this.configManager = new ConfigManager(this);

        // 自动创建 mods 文件夹
        createModsFolder();
    }

    /**
     * 创建 mods 文件夹
     */
    private void createModsFolder() {
        File modsDir = new File("mods");
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }
    }

    /**
     * 初始化 ModLoader
     */
    public void initialize() {
        if (initialized) {
            return;
        }

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
        if (!isApiVersionCompatible(mod.getVersion())) {
            console.printError(languageManager.getMessage("modloader.error.api_version", mod.getName()));
            return;
        }

        javaMods.add(mod);

        // 调用模组加载方法
        mod.onLoad(this);

        // 注册命令
        mod.registerCommands(this);

        // 注册广播域监听器
        mod.registerBroadcastListeners(this);

        console.printSuccess(languageManager.getMessage("modloader.success.loaded", mod.getName(), mod.getVersion()));
    }

    /**
     * 检查 API 版本兼容性
     */
    private boolean isApiVersionCompatible(String modApiVersion) {
        // 简单的版本比较逻辑
        // 实际项目中应该使用更复杂的版本比较
        return modApiVersion.compareTo(MIN_API_VERSION) >= 0;
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
        if (!isApiVersionCompatible(mod.getInfo().getVersion())) {
            console.printError(languageManager.getMessage("modloader.error.api_version", mod.getName()));
            return;
        }

        kotlinMods.add(mod);

        mod.onLoad(this);

        mod.registerCommands(this);

        mod.registerBroadcastListeners(this);

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
     * 主方法
     */
    public static void main(String[] args) {
        ModLoader loader = new ModLoader();
        loader.initialize();
    }
}
