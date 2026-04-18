package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 卸载模组命令
 * 监听 Hub.Command 广播域
 */
public class UnloadCommand extends BaseCommandListener {
    
    public UnloadCommand(ModLoader modLoader) {
        super(modLoader, "unload");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        if (commandMessage.getPartCount() == 0) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/unload [模组名称]")
            );
            return;
        }

        // 获取模组名称参数
        String modName = commandMessage.getPart(0);
        
        // 移除可能的引号
        modName = modName.trim();
        if ((modName.startsWith("\"") && modName.endsWith("\"")) || 
            (modName.startsWith("'") && modName.endsWith("'"))) {
            modName = modName.substring(1, modName.length() - 1);
        }

        // 移除.jar 后缀（如果用户输入了）
        if (modName.endsWith(".jar")) {
            modName = modName.substring(0, modName.length() - 4);
        }

        try {
            // 卸载模组
            modLoader.unloadMod(modName);
        } catch (Throwable e) {
            String errorMsg = modLoader.getLanguageManager().getMessage("command.unload.error.failed", e.getMessage());
            modLoader.getConsole().printError(errorMsg);
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] " + errorMsg, "UnloadCommand");
        }
    }
}
