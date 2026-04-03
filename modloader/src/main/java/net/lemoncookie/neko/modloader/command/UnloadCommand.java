package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 卸载模组命令
 * 通过模组名称卸载
 */
public class UnloadCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.error.args", "/unload [模组名称]")
            );
            return;
        }

        // 移除可能的引号
        args = args.trim();
        if ((args.startsWith("\"") && args.endsWith("\"")) ||
                (args.startsWith("'") && args.endsWith("'"))) {
            args = args.substring(1, args.length() - 1);
        }

        // 移除.jar 后缀（如果用户输入了）
        if (args.endsWith(".jar")) {
            args = args.substring(0, args.length() - 4);
        }

        try {
            // 卸载模组
            modLoader.unloadMod(args);
        } catch (Exception e) {
            String errorMsg = "Failed to unload mod: " + e.getMessage();
            modLoader.getConsole().printError(errorMsg);
            // 通过广播域发送错误消息
            modLoader.getBroadcastManager().broadcast("Hub.Console", "[ERROR] " + errorMsg, "UnloadCommand");
        }
    }

    @Override
    public String getDescription() {
        return "卸载模组（通过模组名称）";
    }

    @Override
    public String getUsage() {
        return "/unload [模组名称]";
    }
}
