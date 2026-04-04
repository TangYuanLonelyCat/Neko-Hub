package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;

import java.util.Map;

/**
 * 帮助命令
 */
public class HelpCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        try {
            String targetMod = "system";
            if (args.startsWith("--")) {
                targetMod = args.substring(2).trim();
            } else if (!args.isEmpty()) {
                targetMod = args.trim();
            }
            
            if ("system".equals(targetMod)) {
                modLoader.getConsole().printLine();
                modLoader.getConsole().printLine("═══════════════════════════════════════");
                modLoader.getConsole().printCyan("? " + modLoader.getLanguageManager().getMessage("command.help.info.system_title"));
                modLoader.getConsole().printLine();
                
                modLoader.getCommandSystem().getCommands().forEach((name, command) -> {
                    modLoader.getConsole().printLine("/" + name + " - " + command.getDescription());
                    modLoader.getConsole().printLine("  " + modLoader.getLanguageManager().getMessage("command.help.info.usage", command.getUsage()));
                    modLoader.getConsole().printLine();
                });
                
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
                        
                        Map<String, Command> commands = modLoader.getCommandSystem().getCommands();
                        boolean hasCommands = false;
                        for (Map.Entry<String, Command> entry : commands.entrySet()) {
                            // 简单判断：如果命令描述或用法中包含模组 ID 或名称
                            Command cmd = entry.getValue();
                            if (cmd.getDescription().contains(mod.getModId()) || 
                                cmd.getDescription().contains(mod.getName()) ||
                                cmd.getUsage().contains(mod.getModId()) ||
                                cmd.getUsage().contains(mod.getName())) {
                                modLoader.getConsole().printLine("/" + entry.getKey() + " - " + cmd.getDescription());
                                modLoader.getConsole().printLine("  " + modLoader.getLanguageManager().getMessage("command.help.info.usage", cmd.getUsage()));
                                modLoader.getConsole().printLine();
                                hasCommands = true;
                            }
                        }
                        
                        if (!hasCommands) {
                            modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.info.no_commands"));
                            modLoader.getConsole().printLine();
                        }
                        
                        modLoader.getConsole().printLine("═══════════════════════════════════════");
                        break;
                    }
                }
                
                if (!found) {
                    modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.help.error.mod_not_found", targetMod));
                    modLoader.getConsole().printLine(modLoader.getLanguageManager().getMessage("command.help.info.use_system"));
                }
            }
        } catch (Throwable e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("command.help.error.failed", e.getMessage()));
        }
    }

    @Override
    public String getDescription() {
        return "Show available commands (use --system or --modname to specify)";
    }

    @Override
    public String getUsage() {
        return "/help [--system|--modname]";
    }
}
