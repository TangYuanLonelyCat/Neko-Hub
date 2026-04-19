package net.lemoncookie.neko.modloader.testmod.a

import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.KModAPI
import net.lemoncookie.neko.modloader.api.ModDependency
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager
import net.lemoncookie.neko.modloader.command.BaseCommandListener
import net.lemoncookie.neko.modloader.command.CommandMessage

/**
 * 测试模组 A - 无依赖的基础模组（Kotlin 实现）
 */
class TestModA : KModAPI {
    
    private lateinit var modLoader: ModLoader
    
    override val modId: String
        get() = "TestModA"
    
    override val version: String
        get() = "3.0.0"  // 模组版本
    
    override val apiVersion: String
        get() = "2.3.0"  // API 版本，必须与 ModLoader 的 MIN_API_VERSION 一致
    
    override val name: String
        get() = "TestModA - Base Module (Kotlin)"
    
    override val packageName: String
        get() = "net.lemoncookie.neko.modloader.testmod.a"
    
    override val dependencies: List<ModDependency>
        get() = emptyList()
    
    override fun onLoad(modLoader: ModLoader) {
        this.modLoader = modLoader
        
        modLoader.broadcastManager.broadcast("Hub.Console", "Loaded successfully!", "TestModA")
        modLoader.broadcastManager.broadcast("Hub.Console", "Hello from TestModA (Kotlin)!", "TestModA")
    }
    
    override fun onUnload() {
        modLoader.console.printLine("[TestModA] Unloaded!")
    }
    
    override fun registerBroadcastListeners(modLoader: ModLoader, modId: String) {
        modLoader.broadcastManager.listen(
            BroadcastManager.HUB_COMMAND,
            object : BaseCommandListener(modLoader, "test") {
                override fun execute(commandMessage: CommandMessage, senderModId: String) {
                    modLoader.console.printLine("OK")
                    modLoader.broadcastManager.broadcast("Hub.Log", "OK", modId)
                    
                    // 尝试请求权限升级到 SYSTEM_COMPONENT (level 1)
                    modLoader.console.printLine("Attempting to upgrade permission to SYSTEM_COMPONENT (level 1)...")
                    val upgradeResult = modLoader.broadcastManager.requestPermissionUpgrade(modId, "TestModA", 1)
                    if (upgradeResult == 0) {
                        modLoader.console.printSuccess("Permission upgrade successful!")
                        modLoader.broadcastManager.broadcast("Hub.Log", "[SUCCESS] TestModA upgraded to SYSTEM_COMPONENT (level 1)", "TestModA")
                    } else {
                        modLoader.console.printError("Permission upgrade failed or denied (error code: $upgradeResult)")
                        modLoader.broadcastManager.broadcast("Hub.Log", "[ERROR] TestModA permission upgrade failed: $upgradeResult", "TestModA")
                    }
                    
                    try {
                        val testModB = getTestModB(modLoader)
                        if (testModB != null) {
                            val method = testModB::class.java.getMethod("doComplete")
                            val result = method.invoke(testModB) as String
                            modLoader.console.printLine(result)
                            modLoader.broadcastManager.broadcast("Hub.Log", result, modId)
                        } else {
                            modLoader.console.printWarning("TestModB not found")
                            modLoader.broadcastManager.broadcast("Hub.Log", "TestModB not found", modId)
                        }
                    } catch (e: Throwable) {
                        modLoader.console.printError("Error calling TestModB: ${e.message}")
                        modLoader.broadcastManager.broadcast("Hub.Log", "Error calling TestModB: ${e.message}", modId)
                    }
                }
            },
            modId,
            "TestModA"
        )
    }
    
    private fun getTestModB(modLoader: ModLoader): KModAPI? {
        return modLoader.kotlinMods.find { it.modId == "TestModB" }
    }
}
