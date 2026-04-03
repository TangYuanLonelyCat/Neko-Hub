package net.lemoncookie.neko.modloader.lib;

import net.lemoncookie.neko.modloader.ModLoader;

import java.util.*;

/**
 * Java 版模组库支持类
 * 提供 Java 模组开发所需的库功能
 */
public class ModLibrary {

    private final Map<String, Object> registry = new HashMap<>();
    private ModLoader modLoader;

    /**
     * 设置 ModLoader 引用（用于日志输出）
     */
    public void setModLoader(ModLoader modLoader) {
        this.modLoader = modLoader;
    }

    /**
     * 注册库组件
     */
    public void register(String name, Object component) {
        registry.put(name, component);
        logMessage("[ModLibrary] Registered: " + name);
    }

    /**
     * 获取库组件
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) registry.get(name);
    }

    /**
     * 检查组件是否存在
     */
    public boolean has(String name) {
        return registry.containsKey(name);
    }

    /**
     * 获取所有已注册组件名称
     */
    public Set<String> getRegisteredNames() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * 输出日志消息
     */
    private void logMessage(String message) {
        if (modLoader != null) {
            modLoader.getBroadcastManager().broadcast("Hub.Console", message, "ModLibrary");
        } else {
            System.out.println(message);
        }
    }
}
