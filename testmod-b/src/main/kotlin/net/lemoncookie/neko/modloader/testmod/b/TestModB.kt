package net.lemoncookie.neko.modloader.testmod.b

import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.KModAPI
import net.lemoncookie.neko.modloader.api.ModDependency

/**
 * 测试模组 B - 被 TestModA 依赖的模组（Kotlin 实现）
 */
class TestModB : KModAPI {
    
    private lateinit var modLoader: ModLoader
    
    override val modId: String
        get() = "TestModB"
    
    override val version: String
        get() = "3.0.0"  // 模组版本
    
    override val apiVersion: String
        get() = "2.3.0"  // API 版本，必须与 ModLoader 的 MIN_API_VERSION 一致
    
    override val name: String
        get() = "TestModB - Complete Module (Kotlin)"
    
    override val packageName: String
        get() = "net.lemoncookie.neko.modloader.testmod.b"
    
    override val dependencies: List<ModDependency>
        get() = emptyList()
    
    override fun onLoad(modLoader: ModLoader) {
        this.modLoader = modLoader
        modLoader.broadcastManager.broadcast("Hub.Console", "Loaded successfully!", "TestModB")
        modLoader.broadcastManager.broadcast("Hub.Console", "Hello from TestModB (Kotlin)!", "TestModB")
    }
    
    override fun onUnload() {
        modLoader.console.printLine("[TestModB] Unloaded!")
    }
    
    /**
     * 完成操作，返回成功消息
     */
    fun doComplete(): String {
        return "TestModB: Complete!"
    }
}
