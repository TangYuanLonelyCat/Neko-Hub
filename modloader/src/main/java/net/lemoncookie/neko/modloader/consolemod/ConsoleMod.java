package net.lemoncookie.neko.modloader.consolemod;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;
import net.lemoncookie.neko.modloader.broadcast.ModPermission;

/**
 * 控制台模组
 * 优先加载，默认权限为 SUPER_ADMIN (level 0)
 * 负责创建 Hub.System 和 Hub.Console 域，并显示消息
 */
public class ConsoleMod implements IModAPI {
    
    private ModLoader modLoader;
    
    @Override
    public String getModId() {
        return "console-mod";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getPackageName() {
        return "net.lemoncookie.neko.modloader.consolemod";
    }
    
    @Override
    public String getName() {
        return "Console Mod";
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        
        // 设置权限为 SUPER_ADMIN
        modLoader.getBroadcastManager().getPermissionManager().setModPermission(getModId(), ModPermission.SUPER_ADMIN);
        
        // 创建 Hub.System 域（公开私有域）
        int result = modLoader.getBroadcastManager().createSystemDomain(getModId());
        if (result == BroadcastManager.ERROR_SUCCESS) {
            modLoader.getConsole().printSuccess("Created Hub.System domain");
        } else {
            modLoader.getConsole().printError("Failed to create Hub.System domain: " + result);
        }
        
        // 创建 Hub.Console 域（公开公共域）
        result = modLoader.getBroadcastManager().createConsoleDomain(getModId());
        if (result == BroadcastManager.ERROR_SUCCESS) {
            modLoader.getConsole().printSuccess("Created Hub.Console domain");
        } else {
            modLoader.getConsole().printError("Failed to create Hub.Console domain: " + result);
        }
        
        // 监听 Hub.Console 域，显示接收到的消息
        modLoader.getBroadcastManager().listen(BroadcastManager.HUB_CONSOLE, new MessageListener() {
            @Override
            public void onMessageReceived(String domain, String message, String senderModId) {
                displayMessage(domain, message, senderModId);
            }
        }, getModId(), getName());
    }
    
    /**
     * 显示消息
     */
    private void displayMessage(String domain, String message, String senderModId) {
        String source;
        if ("system".equals(senderModId)) {
            source = "System";
        } else if (getModId().equals(senderModId)) {
            source = "Console";
        } else {
            source = senderModId;
        }
        
        modLoader.getConsole().printLine("[" + source + "] " + message);
    }
    
    @Override
    public void onUnload() {
        // 控制台模组卸载（理论上不会发生）
    }
    
    @Override
    public void registerCommands(ModLoader modLoader) {
        // 控制台模组不需要注册命令
    }
    
    @Override
    public void registerBroadcastListeners(ModLoader modLoader) {
        // 已经在 onLoad 中注册
    }
}
