package net.lemoncookie.neko.modloader.api

import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.ModDependency

/**
 * Kotlin 版模组 API 接口
 * 提供更符合 Kotlin 习惯的 API 设计
 * 与 Java 版 IModAPI 功能对齐
 */
interface KModAPI {
    /**
     * 模组 ID
     */
    val modId: String

    /**
     * 模组版本
     */
    val version: String

    /**
     * 模组使用的 API 版本
     * 用于检查与 ModLoader 的兼容性
     * 
     * 默认返回模组版本，子类可以覆盖
     */
    val apiVersion: String
        get() = version

    /**
     * 模组名称
     * 
     * 默认返回模组 ID，子类可以覆盖
     */
    val name: String
        get() = modId

    /**
     * 模组包名
     */
    val packageName: String

    /**
     * 模组依赖列表
     * 
     * 默认返回空列表，子类可以覆盖
     */
    val dependencies: List<ModDependency>
        get() = emptyList()

    /**
     * 模组加载时调用
     */
    fun onLoad(modLoader: ModLoader)

    /**
     * 模组卸载时调用
     */
    fun onUnload()

    /**
     * 监听广播域
     * 子类可以覆盖此方法注册广播域监听器
     */
    fun registerBroadcastListeners(modLoader: ModLoader, modId: String) {
        // 默认实现为空
    }

    /**
     * 获取模组信息
     */
    fun getInfo(): ModInfo = ModInfo(modId, name, version, apiVersion)
}

/**
 * 模组信息数据类
 */
data class ModInfo(
    val id: String,
    val name: String,
    val version: String,
    val apiVersion: String
)
