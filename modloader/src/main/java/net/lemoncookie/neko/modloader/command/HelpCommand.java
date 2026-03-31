package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 帮助命令
 */
public class HelpCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) throws Exception {
        modLoader.getConsole().printLine("Available commands:");
        modLoader.getConsole().printLine();
        
        // 显示所有命令
        modLoader.getCommandSystem().getCommands().forEach((name, command) -> {
            modLoader.getConsole().printLine("/" + name + " - " + command.getDescription());
            modLoader.getConsole().printLine("  Usage: " + command.getUsage());
            modLoader.getConsole().printLine();
        });
        
        modLoader.getConsole().printLine("Neko-Hub v." + ModLoader.getVersion() + " " + ModLoader.getGithubVersion());
        modLoader.getConsole().printLine("Type commands with '/' prefix");
    }

    @Override
    public String getDescription() {
        return "Show available commands";
    }

    @Override
    public String getUsage() {
        return "/help";
    }
}
