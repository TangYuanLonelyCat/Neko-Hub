package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

/**
 * 设置命令
 * 支持设置模组权限和 boot 文件名
 * 监听 Hub.Command 广播域
 */
public class SetCommand implements MessageListener {
    
    private final ModLoader modLoader;
    
    public SetCommand(ModLoader modLoader) {
        this.modLoader = modLoader;
    }
    
    @Override
    public void onMessageReceived(String domain, String message, String senderModId) {
        // 解析 JSON 消息
        CommandMessage commandMessage = CommandMessage.fromJson(message);
        if (commandMessage == null || !"set".equals(commandMessage.getCommand())) {
            return;
        }
        
        execute(commandMessage);
    }
    
    private void execute(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 1) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/set [modPermission|bootfile|language] [参数]")
            );
            return;
        }

        String subCommand = commandMessage.getPart(0).toLowerCase();
        
        switch (subCommand) {
            case "modpermission":
                setModPermission(commandMessage);
                break;
            case "bootfile":
                setBootFile(commandMessage);
                break;
            case "language":
                setLanguage(commandMessage);
                break;
            default:
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.unknown_subcommand", subCommand));
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.available", "modPermission, bootfile, language"));
        }
    }

    /**
     * 设置模组权限
     * 语法：/set modpermission [模组名] [level 值]
     */
    private void setModPermission(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 3) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/set modpermission [模组名] [level 值]")
            );
            return;
        }

        // 跳过第一个参数 "modpermission"
        String modName = commandMessage.getPart(1);
        if ((modName.startsWith("\"") && modName.endsWith("\"")) || 
            (modName.startsWith("'") && modName.endsWith("'"))) {
            modName = modName.substring(1, modName.length() - 1);
        }

        // 防护：禁止修改 system 模组的权限
        if ("system".equals(modName)) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.cannot_modify_system"));
            modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + modLoader.getLanguageManager().getMessage("command.set.log.attempted_modify_system"), "SetCommand");
            return;
        }
        
        // 防护：禁止修改控制台模组的权限
        if ("console-mod".equals(modName)) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.cannot_modify_console"));
            modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_SYSTEM, "[ERROR] " + modLoader.getLanguageManager().getMessage("command.set.log.attempted_modify_console"), "SetCommand");
            return;
        }

        int level;
        try {
            level = Integer.parseInt(commandMessage.getPart(2));
            if (level < 0 || level > 3) {
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.level_range"));
                return;
            }
        } catch (NumberFormatException e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.set.error.invalid_level", commandMessage.getPart(2)));
            return;
        }

        // 更新内存中的权限
        modLoader.getBroadcastManager().getPermissionManager().setModPermission(modName, net.lemoncookie.neko.modloader.broadcast.ModPermission.fromLevel(level));
        // 持久化到 boot 文件
        modLoader.getBootFileManager().insertCommandAtTail("/set modpermission " + modName + " " + level);
        modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("command.set.success.permission", modName, level));
    }

    /**
     * 设置 boot 文件
     * 语法：/set bootfile [文件名]
     */
    private void setBootFile(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/set bootfile [文件名]")
            );
            return;
        }

        String fileName = commandMessage.getPart(1);
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

        // 持久化到 boot 文件（替换旧的 /change bootfile 命令）
        modLoader.getBootFileManager().insertCommandAtHeadWithReplace("/change bootfile " + fileName, "/change bootfile");
        modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("command.set.success.bootfile", fileName));
    }

    /**
     * 设置语言
     * 语法：/set language [en/zh/其他语言文件名（不含后缀）]
     */
    private void setLanguage(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/set language [en/zh]")
            );
            return;
        }

        String lang = commandMessage.getPart(1).toLowerCase();
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
        modLoader.getBootFileManager().insertCommandAtHead("/set language " + lang);
        modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("command.set.success.language", lang));
    }
}
