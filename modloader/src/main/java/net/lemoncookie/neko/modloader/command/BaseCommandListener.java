package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

/**
 * 命令监听器基类
 * 所有命令监听器都应继承此类
 */
public abstract class BaseCommandListener implements MessageListener {
    
    protected final ModLoader modLoader;
    protected final String commandName;
    
    public BaseCommandListener(ModLoader modLoader, String commandName) {
        this.modLoader = modLoader;
        this.commandName = commandName;
    }
    
    @Override
    public void onMessageReceived(String domain, String message, String senderModId) {
        // 防御性检查：参数验证
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        try {
            // 解析 JSON 消息
            CommandMessage commandMessage = CommandMessage.fromJson(message);
            if (commandMessage == null) {
                String errorMsg = "Failed to parse command message: " + message;
                modLoader.getConsole().printWarning(errorMsg);
                modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_SYSTEM, "[WARNING] " + errorMsg, "BaseCommandListener");
                return;
            }
            
            // 检查命令名称是否匹配
            if (!commandName.equals(commandMessage.getCommand())) {
                return;
            }
            
            // 执行命令（捕获所有异常）
            try {
                execute(commandMessage, senderModId);
            } catch (Throwable e) {
                String errorMsg = "Error executing command '" + commandName + "': " + e.getMessage();
                modLoader.getConsole().printError(errorMsg);
                modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, commandName);
            }
        } catch (Throwable e) {
            // 最外层防御：捕获所有未处理的异常
            String errorMsg = "Unexpected error in command listener '" + commandName + "': " + e.getMessage();
            modLoader.getConsole().printError(errorMsg);
            modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + errorMsg, "BaseCommandListener");
        }
    }
    
    /**
     * 执行命令
     * @param commandMessage 命令消息
     * @param senderModId 发送者模组 ID
     */
    protected abstract void execute(CommandMessage commandMessage, String senderModId);
}
