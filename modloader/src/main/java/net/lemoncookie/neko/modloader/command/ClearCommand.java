package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 清屏命令
 */
public class ClearCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) throws Exception {
        modLoader.getConsole().clear();
        modLoader.getConsole().printLine("Console cleared");
    }

    @Override
    public String getDescription() {
        return "Clear the console";
    }

    @Override
    public String getUsage() {
        return "/clear";
    }
}
