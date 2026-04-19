package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 说命令
 * 监听 Hub.Command 广播域
 */
public class SayCommand extends BaseCommandListener {
    
    public SayCommand(ModLoader modLoader) {
        super(modLoader, "say");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/say [域名] \"消息内容\"")
            );
            return;
        }

        String domainName = commandMessage.getPart(0);
        String message = commandMessage.getPart(1);

        // 移除消息的引号
        if ((message.startsWith("\"") && message.endsWith("\"")) ||
            (message.startsWith("'") && message.endsWith("'"))) {
            message = message.substring(1, message.length() - 1);
        }

        try {
            int result = modLoader.getBroadcastManager().broadcast(domainName, message, "Console");
            
            if (result == 0) {
                modLoader.getConsole().printSuccess(
                    modLoader.getLanguageManager().getMessage("say.success.sent", domainName, message)
                );
            } else {
                modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("say.error.send_failed", result)
                );
            }
        } catch (Throwable e) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("say.error.failed", e.getMessage())
            );
        }
    }
}
