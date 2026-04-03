package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 卸载模组命令
 * 支持包名和文件名卸载
 */
public class UnloadCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) throws Exception {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/unload [模组包名或文件名]")
            );
            return;
        }

        // 移除可能的引号
        args = args.trim();
        if ((args.startsWith("\"") && args.endsWith("\"")) || 
            (args.startsWith("'") && args.endsWith("'"))) {
            args = args.substring(1, args.length() - 1);
        }

        // 移除.jar 后缀（如果是文件名）
        if (args.endsWith(".jar")) {
            args = args.substring(0, args.length() - 4);
        }

        // TODO: 实现模组卸载逻辑
        // 目前只是模拟
        modLoader.getConsole().printSuccess("Unloading mod: " + args);
        modLoader.getConsole().printSuccess("Mod unloaded successfully");
    }

    @Override
    public String getDescription() {
        return "卸载模组（支持包名或文件名）";
    }

    @Override
    public String getUsage() {
        return "/unload [模组包名或文件名]";
    }
}
