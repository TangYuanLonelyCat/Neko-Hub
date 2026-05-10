package net.lemoncookie.neko.modloader.core;

import net.lemoncookie.neko.modloader.api.BaseModAPI;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.KModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.util.VersionComparator;
import net.lemoncookie.neko.modloader.console.Console;
import net.lemoncookie.neko.modloader.lang.LanguageManager;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.ModPermission;

import java.util.List;

/**
 * ModLoader 核心实现类 - Java 21 实现
 * 提供稳定的模组加载核心功能，包括：
 * - 初始化管理
 * - 模组注册与验证
 * - API 版本兼容性检查
 * - 模组依赖检查
 * - 权限分配
 */
public class ModCore {

    private volatile boolean initialized = false;
    private final String version = "2.0.0";
    
    // 依赖组件
    private Console console;
    private LanguageManager languageManager;
    private BroadcastManager broadcastManager;
    
    // 版本常量
    private String minApiVersion;
    private String loaderVersion;
    
    // 模组列表引用（由 ModLoader 提供）
    private List<IModAPI> javaMods;
    private List<KModAPI> kotlinMods;

    /**
     * 空构造函数
     */
    public ModCore() {
    }

    /**
     * 注入核心依赖组件
     */
    public void injectComponents(ModCoreConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ModCoreConfig cannot be null");
        }
        
        this.console = config.getConsole();
        this.languageManager = config.getLanguageManager();
        this.broadcastManager = config.getBroadcastManager();
        this.loaderVersion = config.getLoaderVersion();
        this.minApiVersion = config.getMinApiVersion();
        this.javaMods = config.getJavaMods();
        this.kotlinMods = config.getKotlinMods();
    }

    /**
     * 启动 ModCore
     */
    public void start() {
        if (!initialized) {
            initialize();
        }
        logMessage("[ModCore] Started successfully on Java " + System.getProperty("java.version"));
    }

    /**
     * 初始化核心组件
     */
    private synchronized void initialize() {
        if (!initialized) {
            logMessage("[ModCore] Initializing version " + version);
            initialized = true;
        }
    }

    /**
     * 获取核心版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 验证并注册 Java 模组（包含验证、兼容性检查、依赖检查和权限分配）
     * @param mod 模组实例
     * @return 是否验证通过
     */
    public boolean validateAndRegisterJavaMod(IModAPI mod) {
        if (!validateModBasicInfo(mod)) {
            return false;
        }

        return validateAndRegisterModCommon(mod.getModId(), mod.getName(), mod.getApiVersion(), () -> checkDependencies(mod));
    }

    /**
     * 验证并注册 Kotlin 模组（包含验证、兼容性检查、依赖检查和权限分配）
     * @param mod 模组实例
     * @return 是否验证通过
     */
    public boolean validateAndRegisterKotlinMod(KModAPI mod) {
        if (!validateKotlinModBasicInfo(mod)) {
            return false;
        }

        return validateAndRegisterModCommon(mod.getModId(), mod.getName(), mod.getApiVersion(), () -> checkKotlinDependencies(mod));
    }

    /**
     * 模组注册的通用验证逻辑
     */
    private boolean validateAndRegisterModCommon(String modId, String name, String apiVersion, java.util.function.Supplier<Boolean> dependencyChecker) {
        if (isModIdExists(modId)) {
            console.printError(languageManager.getMessage("modloader.error.duplicate_modid", modId));
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + languageManager.getMessage("modloader.error.duplicate_modid", modId), "ModCore");
            return false;
        }

        int compatibilityLevel = checkApiVersionCompatibility(apiVersion);
        
        if (compatibilityLevel == 2) {
            if (VersionComparator.isApiVersionTooHigh(apiVersion, minApiVersion)) {
                console.printError(languageManager.getMessage("modloader.error.api_version_too_high", name));
            } else {
                console.printError(languageManager.getMessage("modloader.error.api_version_too_low", name));
            }
            return false;
        }
        
        if (compatibilityLevel == 1) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version", 
                name, apiVersion, minApiVersion));
        }

        if (!dependencyChecker.get()) {
            return false;
        }

        assignPermission(modId);

        return true;
    }

    /**
     * 验证 Java 模组基本信息
     */
    private boolean validateModBasicInfo(IModAPI mod) {
        if (mod == null) {
            console.printError(languageManager.getMessage("modloader.error.null_mod"));
            return false;
        }

        String version = mod.getVersion();
        if (version == null || version.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_version", mod.getName()));
            return false;
        }

        String name = mod.getName();
        if (name == null || name.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_name"));
            return false;
        }

        String modId = mod.getModId();
        if (modId == null || modId.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_modid", name));
            return false;
        }

        String apiVersion = mod.getApiVersion();
        if (apiVersion == null || apiVersion.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.api_version_not_declared", name));
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + languageManager.getMessage("modloader.error.api_version_not_declared", name), "ModCore");
            return false;
        }

        if (apiVersion.equals(version)) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version_equals_mod_version", name));
        }

        return true;
    }

    /**
     * 验证 Kotlin 模组基本信息
     */
    private boolean validateKotlinModBasicInfo(KModAPI mod) {
        if (mod == null) {
            console.printError(languageManager.getMessage("modloader.error.null_mod"));
            return false;
        }

        String version = mod.getVersion();
        if (version == null || version.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_version", mod.getName()));
            return false;
        }

        String name = mod.getName();
        if (name == null || name.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_name"));
            return false;
        }

        String modId = mod.getModId();
        if (modId == null || modId.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.null_modid", name));
            return false;
        }

        String apiVersion = mod.getApiVersion();
        if (apiVersion == null || apiVersion.trim().isEmpty()) {
            console.printError(languageManager.getMessage("modloader.error.api_version_not_declared", name));
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + languageManager.getMessage("modloader.error.api_version_not_declared", name), "ModCore");
            return false;
        }

        if (apiVersion.equals(version)) {
            console.printWarning(languageManager.getMessage("modloader.warning.api_version_equals_mod_version", name));
        }

        return true;
    }

    /**
     * 检查模组 ID 是否已存在
     */
    private boolean isModIdExists(String modId) {
        for (IModAPI loadedMod : javaMods) {
            if (loadedMod != null && loadedMod.getModId() != null && loadedMod.getModId().equals(modId)) {
                return true;
            }
        }
        for (KModAPI loadedMod : kotlinMods) {
            if (loadedMod != null && loadedMod.getModId() != null && loadedMod.getModId().equals(modId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查 API 版本兼容性
     * @return 0=兼容, 1=警告, 2=不兼容
     */
    private int checkApiVersionCompatibility(String apiVersion) {
        return VersionComparator.checkCompatibilityLevel(apiVersion, minApiVersion);
    }

    /**
     * 检查 Java 模组依赖
     */
    private boolean checkDependencies(IModAPI mod) {
        List<ModDependency> dependencies;
        try {
            dependencies = mod.getDependencies();
            if (dependencies == null) {
                return true;
            }
        } catch (Throwable e) {
            String errorMsg = "Error getting dependencies from mod '" + mod.getName() + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
            return false;
        }
        
        if (dependencies.isEmpty()) {
            return true;
        }
        
        for (ModDependency dependency : dependencies) {
            if (dependency == null) {
                continue;
            }
            
            String requiredModId;
            String requiredVersion;
            try {
                requiredModId = dependency.getModId();
                requiredVersion = dependency.getMinVersion();
                
                if (requiredModId == null || requiredModId.trim().isEmpty()) {
                    String errorMsg = "Mod dependency has null or empty modId: " + mod.getName();
                    console.printWarning(errorMsg);
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModCore");
                    continue;
                }
                
                if (requiredVersion == null || requiredVersion.trim().isEmpty()) {
                    String errorMsg = "Mod dependency has null or empty version: " + requiredModId;
                    console.printWarning(errorMsg);
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModCore");
                    continue;
                }
            } catch (Throwable e) {
                String errorMsg = "Error reading dependency from mod '" + mod.getName() + "': " + e.getMessage();
                console.printWarning(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModCore");
                continue;
            }
            
            BaseModAPI loadedMod = getLoadedMod(requiredModId);
            
            if (loadedMod == null) {
                String errorMsg = String.format(
                    "模组 [%s] 所需的依赖 [%s-%s] 不存在或未加载",
                    mod.getModId(),
                    requiredModId,
                    requiredVersion
                );
                console.printError(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
                return false;
            }
            
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
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
                    return false;
                }
            } catch (Throwable e) {
                String errorMsg = "Error comparing versions for dependency '" + requiredModId + "': " + e.getMessage();
                console.printError(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
                return false;
            }
        }
        
        return true;
    }

    /**
     * 检查 Kotlin 模组依赖
     */
    private boolean checkKotlinDependencies(KModAPI mod) {
        List<ModDependency> dependencies;
        try {
            dependencies = mod.getDependencies();
            if (dependencies == null) {
                return true;
            }
        } catch (Throwable e) {
            String errorMsg = "Error getting dependencies from mod '" + mod.getName() + "': " + e.getMessage();
            console.printError(errorMsg);
            broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
            return false;
        }
        
        if (dependencies.isEmpty()) {
            return true;
        }
        
        for (ModDependency dependency : dependencies) {
            if (dependency == null) {
                continue;
            }
            
            String requiredModId;
            String requiredVersion;
            try {
                requiredModId = dependency.getModId();
                requiredVersion = dependency.getMinVersion();
                
                if (requiredModId == null || requiredModId.trim().isEmpty()) {
                    String errorMsg = "Mod dependency has null or empty modId: " + mod.getName();
                    console.printWarning(errorMsg);
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModCore");
                    continue;
                }
                
                if (requiredVersion == null || requiredVersion.trim().isEmpty()) {
                    String errorMsg = "Mod dependency has null or empty version: " + requiredModId;
                    console.printWarning(errorMsg);
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModCore");
                    continue;
                }
            } catch (Throwable e) {
                String errorMsg = "Error reading dependency from mod '" + mod.getName() + "': " + e.getMessage();
                console.printWarning(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "ModCore");
                continue;
            }
            
            BaseModAPI loadedMod = getLoadedMod(requiredModId);
            
            if (loadedMod == null) {
                String errorMsg = String.format(
                    "模组 [%s] 所需的依赖 [%s-%s] 不存在或未加载",
                    mod.getModId(),
                    requiredModId,
                    requiredVersion
                );
                console.printError(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
                return false;
            }
            
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
                    broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
                    return false;
                }
            } catch (Throwable e) {
                String errorMsg = "Error comparing versions for dependency '" + requiredModId + "': " + e.getMessage();
                console.printError(errorMsg);
                broadcastManager.broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "ModCore");
                return false;
            }
        }
        
        return true;
    }

    /**
     * 根据模组 ID 获取已加载的模组
     */
    private BaseModAPI getLoadedMod(String modId) {
        for (IModAPI mod : javaMods) {
            if (mod != null && modId.equals(mod.getModId())) {
                return mod;
            }
        }
        // 同时检查 Kotlin 模组列表
        for (KModAPI mod : kotlinMods) {
            if (mod != null && modId.equals(mod.getModId())) {
                return mod;
            }
        }
        return null;
    }

    /**
     * 分配模组权限
     */
    private void assignPermission(String modId) {
        if ("system".equals(modId) || "console-mod".equals(modId)) {
            broadcastManager.getPermissionManager().setModPermission(modId, ModPermission.SUPER_ADMIN);
        } else {
            broadcastManager.getPermissionManager().setModPermission(modId, ModPermission.NORMAL_COMPONENT);
        }
    }

    /**
     * 输出日志消息
     */
    private void logMessage(String message) {
        if (broadcastManager != null) {
            broadcastManager.broadcast(BroadcastManager.HUB_CONSOLE, message, "ModCore");
        } else {
            System.out.println(message);
        }
    }
}
