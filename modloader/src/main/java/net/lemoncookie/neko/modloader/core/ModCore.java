package net.lemoncookie.neko.modloader.core;

/**
 * ModLoader核心实现类 - Java 21实现
 * 提供稳定的模组加载核心功能
 */
public class ModCore {

    private volatile boolean initialized = false;
    private final String version = "1.0.0";

    /**
     * 启动ModCore
     */
    public void start() {
        if (!initialized) {
            initialize();
        }
        System.out.println("[ModCore] Started successfully on Java " + System.getProperty("java.version"));
    }

    /**
     * 初始化核心组件
     */
    private synchronized void initialize() {
        if (!initialized) {
            System.out.println("[ModCore] Initializing version " + version);
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
}
