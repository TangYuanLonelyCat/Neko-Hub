package net.lemoncookie.neko.modloader.lib;

import java.util.*;

/**
 * Java版模组库支持类
 * 提供Java模组开发所需的库功能
 */
public class ModLibrary {

    private final Map<String, Object> registry = new HashMap<>();

    /**
     * 注册库组件
     */
    public void register(String name, Object component) {
        registry.put(name, component);
        System.out.println("[ModLibrary] Registered: " + name);
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
}
