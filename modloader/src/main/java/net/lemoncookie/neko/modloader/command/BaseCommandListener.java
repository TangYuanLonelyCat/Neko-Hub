package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
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
        // 解析 JSON 消息
        CommandMessage commandMessage = CommandMessage.fromJson(message);
        if (commandMessage == null) {
            return;
        }
        
        // 检查命令名称是否匹配
        if (!commandName.equals(commandMessage.getCommand())) {
            return;
        }
        
        // 执行命令
        execute(commandMessage, senderModId);
    }
    
    /**
     * 执行命令
     * @param commandMessage 命令消息
     * @param senderModId 发送者模组 ID
     */
    protected abstract void execute(CommandMessage commandMessage, String senderModId);
}
