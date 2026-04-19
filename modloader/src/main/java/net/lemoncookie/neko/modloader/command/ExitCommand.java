package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 退出命令
 * 监听 Hub.Command 广播域
 */
public class ExitCommand extends BaseCommandListener {
    
    public ExitCommand(ModLoader modLoader) {
        super(modLoader, "exit");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        modLoader.getConsole().printInfo(
            modLoader.getLanguageManager().getMessage("command.exit.info.shutting_down")
        );
        modLoader.getBroadcastManager().broadcast("Hub.Console", 
            "[SYSTEM] " + modLoader.getLanguageManager().getMessage("command.exit.info.shutting_down"), "ExitCommand");
        modLoader.unloadAll();
        modLoader.getSimpleLogger().close();
        modLoader.getConsole().close();
        System.exit(0);
    }
}
