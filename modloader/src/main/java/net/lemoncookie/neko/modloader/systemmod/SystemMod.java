package net.lemoncookie.neko.modloader.systemmod;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;

/**
 * 系统模组
 * 优先加载，默认权限为 SUPER_ADMIN (level 0)
 * 负责创建 Hub.System 域
 */
public class SystemMod implements IModAPI {
    
    private ModLoader modLoader;
    
    @Override
    public String getModId() {
        return "system";
    }
    
    @Override
    public String getVersion() {
        return "3.2.0";
    }
    
    @Override
    public String getPackageName() {
        return "net.lemoncookie.neko.modloader.systemmod";
    }
    
    @Override
    public String getName() {
        return "System Mod";
    }
    
    @Override
    public String getApiVersion() {
        return "2.3.0";
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        
        // SystemMod 的权限已在 ModLoader.registerJavaMod() 中设置为 SUPER_ADMIN
        
        // 创建 Hub.System 域（公开私有域）
        int result = modLoader.getBroadcastManager().createSystemDomain(getModId());
        if (result == BroadcastManager.ERROR_SUCCESS) {
            String msg = modLoader.getLanguageManager().getMessage("systemmod.success.create_system_domain");
            modLoader.getConsole().printSuccess(msg);
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[SUCCESS] " + msg, getModId());
        } else {
            String errorMsg = modLoader.getLanguageManager().getMessage("systemmod.error.create_system_domain", result);
            modLoader.getConsole().printError(errorMsg);
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] " + errorMsg, getModId());
        }
    }
    
    @Override
    public void onUnload() {
        // 系统模组卸载（理论上不会发生）
    }
    
    @Override
    public void registerBroadcastListeners(ModLoader modLoader, String modId) {
        // 不需要注册监听器
    }
}
