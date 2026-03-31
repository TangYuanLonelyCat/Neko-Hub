package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 卸载模组命令
 */
public class UnloadCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) throws Exception {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/unload [module package name]")
            );
            return;
        }

        // 这里实现模组卸载逻辑
        // 暂时只是模拟
        modLoader.getConsole().printSuccess("Unloading mod: " + args);
        modLoader.getConsole().printSuccess("Mod unloaded successfully");
    }

    @Override
    public String getDescription() {
        return "Unload a module";
    }

    @Override
    public String getUsage() {
        return "/unload [module package name]";
    }
}
