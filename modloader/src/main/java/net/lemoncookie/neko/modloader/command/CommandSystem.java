package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令系统
 * 负责解析和执行命令
 */
public class CommandSystem {

    private final ModLoader modLoader;
    private final Map<String, Command> commands;

    /**
     * 构造函数
     */
    public CommandSystem(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.commands = new HashMap<>();
        
        // 注册内置命令
        registerCommands();
    }

    /**
     * 注册内置命令
     */
    private void registerCommands() {
        registerCommand("help", new HelpCommand());
        registerCommand("clear", new ClearCommand());
        registerCommand("load", new LoadCommand());
        registerCommand("unload", new UnloadCommand());
    }

    /**
     * 注册命令
     */
    public void registerCommand(String name, Command command) {
        commands.put(name.toLowerCase(), command);
    }

    /**
     * 执行命令
     */
    public void executeCommand(String input) {
        // 解析命令
        String[] parts = input.split("\\s+", 2);
        if (parts.length == 0) {
            return;
        }
        
        String commandName = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        
        // 检查是否是斜杠命令
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
            
            // 查找命令
            Command command = commands.get(commandName.toLowerCase());
            if (command != null) {
                try {
                    command.execute(modLoader, args);
                } catch (Exception e) {
                    modLoader.getConsole().printLine("Command error: " + e.getMessage());
                }
            } else {
                modLoader.getConsole().printLine(
                    modLoader.getLanguageManager().getMessage("command.error.unknown", "/" + commandName)
                );
                modLoader.getConsole().printLine(
                    modLoader.getLanguageManager().getMessage("command.error.usage")
                );
            }
        } else {
            // 非命令输入，可能是其他指令
            modLoader.getConsole().printLine(
                modLoader.getLanguageManager().getMessage("command.error.unknown", input)
            );
            modLoader.getConsole().printLine(
                modLoader.getLanguageManager().getMessage("command.error.usage")
            );
        }
    }

    /**
     * 获取命令列表
     */
    public Map<String, Command> getCommands() {
        return new HashMap<>(commands);
    }
}
