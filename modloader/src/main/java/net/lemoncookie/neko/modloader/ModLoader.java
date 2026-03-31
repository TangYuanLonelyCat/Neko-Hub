package net.lemoncookie.neko.modloader;

import net.lemoncookie.neko.modloader.core.ModCore;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.lib.ModLibrary;
import net.lemoncookie.neko.modloader.command.CommandSystem;
import net.lemoncookie.neko.modloader.console.Console;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.lang.LanguageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final Console console;
    private final CommandSystem commandSystem;
    private final BroadcastManager broadcastManager;
    private final LanguageManager languageManager;

    private boolean initialized = false;

    /**
     * 构造函数
     */
    public ModLoader() {
        this.core = new ModCore();
        this.javaLibrary = new ModLibrary();
        this.javaMods = new ArrayList<>();
        this.languageManager = new LanguageManager();
        this.console = new Console(this);
        this.commandSystem = new CommandSystem(this);
        this.broadcastManager = new BroadcastManager(this);
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

        console.printLine(languageManager.getMessage("modloader.loading"));

        long startTime = System.currentTimeMillis();

        // 扫描和加载模组
        scanAndLoadMods();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        console.printLine(languageManager.getMessage("modloader.loaded", javaMods.size(), seconds));
        console.printLine();

        core.start();
        initialized = true;

        // 启动控制台交互
        console.startInteractive();
    }

    /**
     * 扫描和加载模组
     */
    private void scanAndLoadMods() {
        // 这里实现模组扫描逻辑
        // 暂时添加一个测试模组
        testMod();
    }

    /**
     * 测试模组
     */
    private void testMod() {
        IModAPI testMod = new IModAPI() {
            @Override
            public String getModId() {
                return "test-mod";
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String getPackageName() {
                return "com.example.testmod";
            }

            @Override
            public void onLoad(ModLoader modLoader) {
                console.printSuccess("Test mod loaded");
            }

            @Override
            public void onUnload() {
                console.printLine("Test mod unloaded");
            }

            @Override
            public String getName() {
                return "Test Mod";
            }
        };

        registerJavaMod(testMod);
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
     * 卸载所有模组
     */
    public void unloadAll() {
        javaMods.forEach(IModAPI::onUnload);
        javaMods.clear();
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
