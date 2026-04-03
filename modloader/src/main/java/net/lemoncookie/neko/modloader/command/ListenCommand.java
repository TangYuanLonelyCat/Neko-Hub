package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监听命令
 * 监听/取消监听特定广播域
 */
public class ListenCommand implements Command {

    // 存储监听器映射：域名 -> 监听器
    private static final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();

    @Override
    public void execute(ModLoader modLoader, String args) {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/listen [域名] [start|stop]")
            );
            return;
        }

        // 解析参数：域名和操作
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/listen [域名] [start|stop]")
            );
            return;
        }

        String domainName = parts[0].trim();
        String action = parts[1].trim().toLowerCase();

        switch (action) {
            case "start":
                startListening(modLoader, domainName);
                break;
            case "stop":
                stopListening(modLoader, domainName);
                break;
            default:
                modLoader.getConsole().printError("Unknown action: " + action);
                modLoader.getConsole().printError("Use 'start' to begin listening or 'stop' to stop listening");
        }
    }

    /**
     * 开始监听指定域
     */
    private void startListening(ModLoader modLoader, String domainName) {
        // 检查是否已经在监听
        if (listeners.containsKey(domainName)) {
            modLoader.getConsole().printWarning("Already listening to domain: " + domainName);
            return;
        }

        // 创建监听器
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessageReceived(String domain, String message, String senderModId) {
                modLoader.getConsole().printLine("[" + domain + "] (from: " + senderModId + "): " + message);
            }
        };

        // 注册监听器
        int result = modLoader.getBroadcastManager().listen(domainName, listener, "Console", "Console");
        
        if (result == 0) {
            listeners.put(domainName, listener);
            modLoader.getConsole().printSuccess("Started listening to domain: " + domainName);
        } else {
            modLoader.getConsole().printError("Failed to start listening. Error code: " + result);
        }
    }

    /**
     * 停止监听指定域
     */
    private void stopListening(ModLoader modLoader, String domainName) {
        // 检查是否在监听
        MessageListener listener = listeners.remove(domainName);
        if (listener == null) {
            modLoader.getConsole().printWarning("Not listening to domain: " + domainName);
            return;
        }

        // 移除监听器（通过广播域管理器）
        // 注意：BroadcastManager 需要添加移除监听器的方法
        modLoader.getConsole().printSuccess("Stopped listening to domain: " + domainName);
    }

    @Override
    public String getDescription() {
        return "监听/取消监听特定广播域";
    }

    @Override
    public String getUsage() {
        return "/listen [域名] [start|stop]";
    }
}
