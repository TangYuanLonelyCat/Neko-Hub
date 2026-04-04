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
                modLoader.getLanguageManager().getMessage("command.error.args", "/set [modPermission|bootfile|language] [参数]")
            );
            return;
        }

        String[] parts = args.split("\\s+", 3);
        if (parts.length < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/set [modPermission|bootfile|language] [参数]")
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
            case "language":
                setLanguage(modLoader, parts);
                break;
            default:
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.unknown_subcommand", subCommand));
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.available", "modPermission, bootfile, language"));
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
        if ((modName.startsWith("\"") && modName.endsWith("\"")) || 
            (modName.startsWith("'") && modName.endsWith("'"))) {
            modName = modName.substring(1, modName.length() - 1);
        }

        int level;
        try {
            level = Integer.parseInt(parts[2]);
            if (level < 0 || level > 3) {
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.level_range"));
                return;
            }
        } catch (NumberFormatException e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.invalid_level", parts[2]));
            return;
        }

        modLoader.getConfigManager().setModPermission(modName, level);
        modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("command.set.success.permission", modName, level));
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
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.boot_not_found", fileName));
            return;
        }

        modLoader.getConfigManager().setBootFile(fileName);
        modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("command.set.success.bootfile", fileName));
    }

    /**
     * 设置语言
     * 语法：/set language [en/zh/其他语言文件名（不含后缀）]
     */
    private void setLanguage(ModLoader modLoader, String[] parts) {
        if (parts.length < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/set language [en/zh]")
            );
            return;
        }

        String lang = parts[1].toLowerCase();
        try (java.io.InputStream is = modLoader.getLanguageManager().getClass().getResourceAsStream("/lang/" + lang + ".json")) {
            if (is == null) {
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.language_not_found", lang));
                return;
            }
        } catch (Exception e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.language_not_found", lang));
            return;
        }

        modLoader.getLanguageManager().loadLanguage(lang);
        modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("command.set.success.language", lang));
    }

    @Override
    public String getDescription() {
        return "设置配置（模组权限、boot 文件或语言）";
    }

    @Override
    public String getUsage() {
        return "/set [modPermission|bootfile|language] [参数]";
    }
}
