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
            modLoader.getConsole().printSuccess(
                modLoader.getLanguageManager().getMessage("command.clear.success")
            );
        } catch (Throwable e) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.clear.error.failed", e.getMessage())
            );
        }
    }
}
