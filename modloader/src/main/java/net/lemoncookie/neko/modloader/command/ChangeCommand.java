package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 变更命令
 * 监听 Hub.Command 广播域
 * 支持变更各种运行时配置
 */
public class ChangeCommand extends BaseCommandListener {
    
    public ChangeCommand(ModLoader modLoader) {
        super(modLoader, "change");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        if (commandMessage.getPartCount() < 1) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/change [bootfile] [文件名]")
            );
            return;
        }
        
        String subCommand = commandMessage.getPart(0).toLowerCase();
        
        switch (subCommand) {
            case "bootfile":
                changeBootFile(commandMessage);
                break;
            default:
                modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.change.error.unknown_subcommand", subCommand)
                );
                modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.change.error.available", "bootfile")
                );
        }
    }
    
    /**
     * 变更 boot 文件
     * 语法：/change bootfile [文件名]
     */
    private void changeBootFile(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/change bootfile [文件名]")
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
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.set.error.boot_not_found", fileName)
            );
            return;
        }
        
        // 切换 boot 文件并执行
        modLoader.getBootFileManager().switchBootFileAndExecute(fileName);
        modLoader.getConsole().printSuccess(
            modLoader.getLanguageManager().getMessage("command.change.success.bootfile", fileName)
        );
    }
}
