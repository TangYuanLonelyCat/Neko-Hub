package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 变更命令
 * 监听 Hub.Command 广播域
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
            case "inputboxview":
                changeInputBoxView(commandMessage);
                break;
            default:
                modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.change.error.unknown_subcommand", subCommand)
                );
                modLoader.getConsole().printError(
                    modLoader.getLanguageManager().getMessage("command.change.error.available", "bootfile, inputboxview")
                );
        }
    }
    
    private void changeBootFile(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/change bootfile [文件名]")
            );
            return;
        }
        
        String fileName = commandMessage.getPart(1);
        if ((fileName.startsWith("\"") && fileName.endsWith("\"")) || 
            (fileName.startsWith("'") && fileName.endsWith("'"))) {
            fileName = fileName.substring(1, fileName.length() - 1);
        }
        
        java.io.File bootFile = new java.io.File(fileName);
        if (!bootFile.exists()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.set.error.boot_not_found", fileName)
            );
            return;
        }
        
        modLoader.getBootFileManager().switchBootFileAndExecute(fileName);
        modLoader.getConsole().printSuccess(
            modLoader.getLanguageManager().getMessage("command.change.success.bootfile", fileName)
        );
    }
    
    private void changeInputBoxView(CommandMessage commandMessage) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/change inputboxview [true/false]")
            );
            return;
        }
        
        String value = commandMessage.getPart(1);
        String normalizedValue = value.trim().toLowerCase();
        
        if (!normalizedValue.equals("true") && !normalizedValue.equals("false")) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.change.error.invalid_boolean", value)
            );
            return;
        }
        
        try {
            boolean enabled = normalizedValue.equals("true");
            modLoader.getConsole().setInputEnabled(enabled);
            
            if (enabled) {
                modLoader.getConsole().printSuccess(
                    modLoader.getLanguageManager().getMessage("command.change.success.inputboxview_enabled")
                );
            } else {
                modLoader.getConsole().printSuccess(
                    modLoader.getLanguageManager().getMessage("command.change.success.inputboxview_disabled")
                );
            }
        } catch (Exception e) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("console.error.processing_error", e.getMessage())
            );
            modLoader.getBroadcastManager().broadcast(
                "Hub.Log",
                "[ERROR] Failed to change input box view: " + e.getMessage(),
                "ChangeCommand"
            );
        }
    }
}
