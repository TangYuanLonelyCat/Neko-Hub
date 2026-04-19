package net.lemoncookie.neko.modloader.consolemod;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;
import net.lemoncookie.neko.modloader.broadcast.ModPermission;

/**
 * 控制台模组
 * 优先加载，默认权限为 SUPER_ADMIN (level 0)
 * 负责创建 Hub.Console 域，并显示消息
 */
public class ConsoleMod implements IModAPI {
    
    private ModLoader modLoader;
    
    @Override
    public String getModId() {
        return "console-mod";
    }
    
    @Override
    public String getVersion() {
        return "2.1.0";
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
    public String getApiVersion() {
        return "2.3.0";
    }
    
    @Override
    public void onLoad(ModLoader modLoader) {
        this.modLoader = modLoader;
        
        // ConsoleMod 的权限已在 ModLoader.registerJavaMod() 中设置为 SUPER_ADMIN
        
        // 创建 Hub.Console 域（公开公共域）
        int result = modLoader.getBroadcastManager().createConsoleDomain(getModId());
        if (result == BroadcastManager.ERROR_SUCCESS) {
            String msg = modLoader.getLanguageManager().getMessage("consolemod.success.create_console_domain");
            modLoader.getConsole().printSuccess(msg);
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[SUCCESS] " + msg, getModId());
        } else {
            String errorMsg = modLoader.getLanguageManager().getMessage("consolemod.error.create_console_domain", result);
            modLoader.getConsole().printError(errorMsg);
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] " + errorMsg, getModId());
        }
        
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
        
        // 根据消息类型选择颜色
        if (message.startsWith("[ERROR]")) {
            modLoader.getConsole().printError("[" + source + "] " + message);
        } else if (message.startsWith("[WARNING]") || message.startsWith("[WARN]")) {
            modLoader.getConsole().printWarning("[" + source + "] " + message);
        } else if (message.startsWith("[INFO]") || message.startsWith("[SUCCESS]")) {
            modLoader.getConsole().printSuccess("[" + source + "] " + message);
        } else {
            modLoader.getConsole().printLine("[" + source + "] " + message);
        }
    }
    
    @Override
    public void onUnload() {
        // 控制台模组卸载（理论上不会发生）
    }
    
    @Override
    public void registerBroadcastListeners(ModLoader modLoader, String modId) {
        // 已经在 onLoad 中注册
        // modId 参数未使用，但必须实现接口
    }
}
