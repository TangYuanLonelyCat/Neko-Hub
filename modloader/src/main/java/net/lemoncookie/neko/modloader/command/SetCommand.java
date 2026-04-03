package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 设置命令
 * 支持设置模组权限和 boot 文件名
 */
public class SetCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.error.args", "/set [modPermission|bootfile] [参数]")
            );
            return;
        }

        // 解析命令
        String[] parts = args.split("\\s+", 3);
        if (parts.length < 2) {
            modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.error.args", "/set [modPermission|bootfile] [参数]")
            );
            return;
        }

        String subCommand = parts[0].toLowerCase();

        switch (subCommand) {
            case "modpermission":
                setModPermission(modLoader, parts);
                break;
            case "bootfile":
                setBootFile(modLoader, parts);
                break;
            default:
                modLoader.getConsole().printError("Unknown set command: " + subCommand);
                modLoader.getConsole().printError("Available: modPermission, bootfile");
        }
    }

    /**
     * 设置模组权限
     * 语法：/set modPermission [模组名或模组包名] [level 值]
     */
    private void setModPermission(ModLoader modLoader, String[] parts) {
        if (parts.length < 3) {
            modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.error.args", "/set modPermission [模组名] [level 值]")
            );
            return;
        }

        String modName = parts[1];
        // 移除可能的引号
        if ((modName.startsWith("\"") && modName.endsWith("\"")) ||
                (modName.startsWith("'") && modName.endsWith("'"))) {
            modName = modName.substring(1, modName.length() - 1);
        }

        int level;
        try {
            level = Integer.parseInt(parts[2]);
            if (level < 0 || level > 3) {
                modLoader.getConsole().printError("Level must be between 0 and 3");
                return;
            }
        } catch (NumberFormatException e) {
            modLoader.getConsole().printError("Invalid level: " + parts[2]);
            return;
        }

        // 设置权限
        modLoader.getConfigManager().setModPermission(modName, level);
        modLoader.getConsole().printSuccess("Set permission for " + modName + " to level " + level);
    }

    /**
     * 设置 boot 文件
     * 语法：/set bootfile [文件名]
     */
    private void setBootFile(ModLoader modLoader, String[] parts) {
        if (parts.length < 2) {
            modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.error.args", "/set bootfile [文件名]")
            );
            return;
        }

        String fileName = parts[1];
        // 移除可能的引号
        if ((fileName.startsWith("\"") && fileName.endsWith("\"")) ||
                (fileName.startsWith("'") && fileName.endsWith("'"))) {
            fileName = fileName.substring(1, fileName.length() - 1);
        }

        // 检查文件是否存在
        java.io.File bootFile = new java.io.File(fileName);
        if (!bootFile.exists()) {
            modLoader.getConsole().printError("Boot file not found: " + fileName);
            return;
        }

        modLoader.getConfigManager().setBootFile(fileName);
        modLoader.getConsole().printSuccess("Set boot file to: " + fileName);
    }

    @Override
    public String getDescription() {
        return "设置配置（模组权限或 boot 文件）";
    }

    @Override
    public String getUsage() {
        return "/set [modPermission|bootfile] [参数]";
    }
}
