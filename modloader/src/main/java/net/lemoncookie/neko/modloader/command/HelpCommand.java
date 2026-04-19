package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;

import java.util.Map;

/**
 * 帮助命令
 * 监听 Hub.Command 广播域
 */
public class HelpCommand extends BaseCommandListener {
    
    public HelpCommand(ModLoader modLoader) {
        super(modLoader, "help");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        try {
            String targetMod = "system";
            if (commandMessage.getPartCount() > 0) {
                String arg = commandMessage.getPart(0);
                if (arg.startsWith("--")) {
                    targetMod = arg.substring(2).trim();
                } else {
                    targetMod = arg.trim();
                }
            }
            
            if ("system".equals(targetMod)) {
                modLoader.getConsole().printLine();
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.divider"));
                modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.help.info.system_title"));
                modLoader.getConsole().printLine();
                
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command_list"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.set"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.change"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.clear"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.load"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.unload"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.list"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.help"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.exit"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.say"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.listen"));
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.command.autoboot"));
                modLoader.getConsole().printLine();
                modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.divider"));
            } else {
                boolean found = false;
                for (IModAPI mod : modLoader.getJavaMods()) {
                    if (mod.getModId().equals(targetMod) || mod.getName().equals(targetMod)) {
                        found = true;
                        modLoader.getConsole().printLine();
                        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.divider"));
                        modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.help.info.mod_title", mod.getName()));
                        modLoader.getConsole().printLine();
                        modLoader.getConsole().printLine(
                            modLoader.getLanguageManager().getMessage("command.help.mod_id", mod.getModId())
                        );
                        modLoader.getConsole().printLine(
                            modLoader.getLanguageManager().getMessage("command.help.mod_version", mod.getVersion())
                        );
                        modLoader.getConsole().printLine(
                            modLoader.getLanguageManager().getMessage("command.help.mod_package", mod.getPackageName())
                        );
                        modLoader.getConsole().printLine();
                        modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.divider"));
                        break;
                    }
                }
                
                if (!found) {
                    modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.help.error.mod_not_found", targetMod));
                    modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.use_help"));
                }
            }
        } catch (Throwable e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.help.error.failed", e.getMessage()));
        }
    }
}
