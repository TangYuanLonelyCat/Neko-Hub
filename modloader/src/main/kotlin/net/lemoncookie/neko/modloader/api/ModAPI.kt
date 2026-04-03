package net.lemoncookie.neko.modloader.api.kt

import net.lemoncookie.neko.modloader.ModLoader

/**
 * Kotlin 版模组 API 接口
 * 提供更符合 Kotlin 习惯的 API 设计
 */
interface ModAPI {
    /**
     * 模组 ID
     */
    val modId: String

    /**
     * 模组版本
     */
    val version: String

    /**
     * 模组名称
     */
    val name: String
        get() = modId

    /**
     * 模组包名
     */
    val packageName: String

    /**
     * 模组加载时调用
     */
    fun onLoad(modLoader: ModLoader)

    /**
     * 模组卸载时调用
     */
    fun onUnload()

    /**
     * 注册命令
     * 子类可以覆盖此方法注册命令
     */
    fun registerCommands(modLoader: ModLoader) {
        // 默认实现为空
    }

    /**
     * 监听广播域
     * 子类可以覆盖此方法注册广播域监听器
     */
    fun registerBroadcastListeners(modLoader: ModLoader) {
        // 默认实现为空
    }

    /**
     * 获取模组信息
     */
    fun getInfo(): ModInfo = ModInfo(modId, name, version)
}

/**
 * 模组信息数据类
 */
data class ModInfo(
    val id: String,
    val name: String,
    val version: String
)
