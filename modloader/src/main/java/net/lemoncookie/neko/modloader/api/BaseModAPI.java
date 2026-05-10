package net.lemoncookie.neko.modloader.api;

/**
 * 模组 API 基础接口
 * 定义 Java 和 Kotlin 模组的公共方法
 */
public interface BaseModAPI {
    
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
    String getName();

    /**
     * 获取 API 版本
     */
    String getApiVersion();
}
