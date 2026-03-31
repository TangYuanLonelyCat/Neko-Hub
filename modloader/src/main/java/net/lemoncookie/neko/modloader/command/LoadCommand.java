package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 加载模组命令
 */
public class LoadCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) throws Exception {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/load [module package name]")
            );
            return;
        }

        // 这里实现模组加载逻辑
        // 暂时只是模拟
        modLoader.getConsole().printSuccess("Loading mod: " + args);
        modLoader.getConsole().printSuccess("Mod loaded successfully");
    }

    @Override
    public String getDescription() {
        return "Load a module";
    }

    @Override
    public String getUsage() {
        return "/load [module package name]";
    }
}
