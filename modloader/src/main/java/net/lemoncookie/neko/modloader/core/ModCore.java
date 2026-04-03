package net.lemoncookie.neko.modloader.core;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * ModLoader 核心实现类 - Java 21 实现
 * 提供稳定的模组加载核心功能
 */
public class ModCore {

    private volatile boolean initialized = false;
    private final String version = "2.0.0";
    private ModLoader modLoader;

    /**
     * 设置 ModLoader 引用（用于日志输出）
     */
    public void setModLoader(ModLoader modLoader) {
        this.modLoader = modLoader;
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
     * 输出日志消息
     */
    private void logMessage(String message) {
        if (modLoader != null) {
            modLoader.getBroadcastManager().broadcast("Hub.Console", message, "ModCore");
        } else {
            System.out.println(message);
        }
    }
}
