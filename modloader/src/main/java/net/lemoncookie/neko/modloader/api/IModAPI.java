package net.lemoncookie.neko.modloader.api;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

/**
 * Java版模组API接口
 * 用于Java模组开发
 */
public interface IModAPI {

    /**
     * 获取模组ID
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
     * 模组加载时调用
     */
    void onLoad(ModLoader modLoader);

    /**
     * 模组卸载时调用
     */
    void onUnload();

    /**
     * 注册命令
     */
    default void registerCommands(ModLoader modLoader) {
        // 子类可以覆盖此方法注册命令
        // 使用 modLoader.getCommandSystem().registerCommand("命令名", new 你的命令类 ()) 来注册命令
    }

    /**
     * 监听广播域
     */
    default void registerBroadcastListeners(ModLoader modLoader) {
        // 子类可以覆盖此方法注册广播域监听器
    }
}

