package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 清屏命令
 * 监听 Hub.Command 广播域
 */
public class ClearCommand extends BaseCommandListener {
    
    public ClearCommand(ModLoader modLoader) {
        super(modLoader, "clear");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        try {
            modLoader.getConsole().clear();
            modLoader.getConsole().printLine("Console cleared");
        } catch (Throwable e) {
            modLoader.getConsole().printError("Failed to clear console: " + e.getMessage());
        }
    }
}
