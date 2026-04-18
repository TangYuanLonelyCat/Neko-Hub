package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 自动启动命令
 * 监听 Hub.Command 广播域
 */
public class AutobootCommand extends BaseCommandListener {
    
    public AutobootCommand(ModLoader modLoader) {
        super(modLoader, "autoboot");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        try {
            modLoader.getBootFileManager().generateAutoBoot();
        } catch (Throwable e) {
            modLoader.getConsole().printError("Failed to generate auto.boot: " + e.getMessage());
        }
    }
}
