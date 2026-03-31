package net.lemoncookie.neko.modloader.api.kt

/**
 * Kotlin版模组API接口
 * 提供更符合Kotlin习惯的API设计
 */
interface ModAPI {
    /**
     * 模组ID
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
     * 模组加载时调用
     */
    fun onLoad()

    /**
     * 模组卸载时调用
     */
    fun onUnload()

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
