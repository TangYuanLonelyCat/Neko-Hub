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
                modLoader.getConsole().printLine("═══════════════════════════════════════");
                modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.help.info.system_title"));
                modLoader.getConsole().printLine();
                
                modLoader.getConsole().printLine("Available commands:");
                modLoader.getConsole().printLine("  /set - Set configuration (modPermission, bootfile, language)");
                modLoader.getConsole().printLine("  /clear - Clear the console");
                modLoader.getConsole().printLine("  /load - Load a mod from JAR file");
                modLoader.getConsole().printLine("  /unload - Unload a mod by name");
                modLoader.getConsole().printLine("  /list - List loaded mods");
                modLoader.getConsole().printLine("  /help - Show this help message");
                modLoader.getConsole().printLine("  /exit - Exit the application");
                modLoader.getConsole().printLine("  /say - Broadcast a message");
                modLoader.getConsole().printLine("  /listen - Listen to a domain");
                modLoader.getConsole().printLine("  /autoboot - Manage auto-boot configuration");
                modLoader.getConsole().printLine();
                modLoader.getConsole().printLine("═══════════════════════════════════════");
            } else {
                boolean found = false;
                for (IModAPI mod : modLoader.getJavaMods()) {
                    if (mod.getModId().equals(targetMod) || mod.getName().equals(targetMod)) {
                        found = true;
                        modLoader.getConsole().printLine();
                        modLoader.getConsole().printLine("═══════════════════════════════════════");
                        modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.help.info.mod_title", mod.getName()));
                        modLoader.getConsole().printLine();
                        modLoader.getConsole().printLine("Mod ID: " + mod.getModId());
                        modLoader.getConsole().printLine("Version: " + mod.getVersion());
                        modLoader.getConsole().printLine("Package: " + mod.getPackageName());
                        modLoader.getConsole().printLine();
                        modLoader.getConsole().printLine("═══════════════════════════════════════");
                        break;
                    }
                }
                
                if (!found) {
                    modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.help.error.mod_not_found", targetMod));
                    modLoader.getConsole().printLine("Use /help to see system commands");
                }
            }
        } catch (Throwable e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.help.error.failed", e.getMessage()));
        }
    }
}
