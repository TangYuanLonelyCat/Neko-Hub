package net.lemoncookie.neko.modloader.api;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

import java.util.Collections;
import java.util.List;

/**
 * Java 版模组 API 接口
 * 用于 Java 模组开发
 */
public interface IModAPI {

    /**
     * 获取模组 ID
     */
    String getModId();

    /**
     * 获取模组版本
     */
    String getVersion();

    /**
     * 获取模组名称
     */
    default String getName() {
        return getModId();
    }

    /**
     * 获取模组包名
     */
    String getPackageName();

    /**
     * 获取模组依赖列表
     * 
     * @return 依赖列表，无依赖时返回空列表
     */
    default List<ModDependency> getDependencies() {
        return Collections.emptyList();
    }

    /**
     * 模组加载时调用
     */
    void onLoad(ModLoader modLoader);

    /**
     * 模组卸载时调用
     */
    void onUnload();

    /**
     * 监听广播域
     * @param modLoader ModLoader 实例
     * @param modId 当前模组的 ID（自动传入）
     */
    default void registerBroadcastListeners(ModLoader modLoader, String modId) {
        // 子类可以覆盖此方法注册广播域监听器
    }
}

