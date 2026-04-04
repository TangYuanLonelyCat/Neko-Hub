package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 说命令
 * 向特定广播域发送消息
 */
public class SayCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/say [域名] \"消息内容\"")
            );
            return;
        }

        // 解析参数：域名和消息内容
        // 支持格式：/say Hub.Console "Hello World"
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/say [域名] \"消息内容\"")
            );
            return;
        }

        String domainName = parts[0];
        String message = parts[1];

        // 移除消息的引号
        if ((message.startsWith("\"") && message.endsWith("\"")) ||
            (message.startsWith("'") && message.endsWith("'"))) {
            message = message.substring(1, message.length() - 1);
        }

        try {
            // 发送消息到指定域
            int result = modLoader.getBroadcastManager().broadcast(domainName, message, "Console");
            
            if (result == 0) {
                modLoader.getConsole().printSuccess("Message sent to " + domainName + ": " + message);
            } else {
                modLoader.getConsole().printError("Failed to send message. Error code: " + result);
            }
        } catch (Throwable e) {
            modLoader.getConsole().printError("Failed to send message: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "向特定广播域发送消息";
    }

    @Override
    public String getUsage() {
        return "/say [域名] \"消息内容\"";
    }
}
