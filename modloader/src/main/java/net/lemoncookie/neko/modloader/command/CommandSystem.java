package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

import java.util.*;

/**
 * 命令系统
 * 负责解析和执行命令
 */
public class CommandSystem {

    private final ModLoader modLoader;
    // 命令名 -> (模组 ID -> Command)
    private final Map<String, Map<String, Command>> commands;
    // 记录命令的注册者
    private final Map<String, Set<String>> commandOwners;

    /**
     * 构造函数
     */
    public CommandSystem(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.commands = new HashMap<>();
        this.commandOwners = new HashMap<>();
        
        // 注册内置命令
        registerCommands();
    }

    /**
     * 注册内置命令
     */
    private void registerCommands() {
        registerCommand("help", "system", new HelpCommand(), false);
        registerCommand("clear", "system", new ClearCommand(), false);
        registerCommand("load", "system", new LoadCommand(), false);
        registerCommand("unload", "system", new UnloadCommand(), false);
        registerCommand("set", "system", new SetCommand(), false);
        registerCommand("autoboot", "system", new AutobootCommand(), false);
        registerCommand("exit", "system", new ExitCommand(), false);
        registerCommand("say", "system", new SayCommand(), false);
        registerCommand("listen", "system", new ListenCommand(), false);
        registerCommand("list", "system", new ListCommand(), false);
    }

    /**
     * 注册命令
     * @param name 命令名称
     * @param modId 注册命令的模组 ID
     * @param command 命令对象
     * @param allowOverride 是否允许覆盖已有命令
     * @return 是否注册成功
     */
    public boolean registerCommand(String name, String modId, Command command, boolean allowOverride) {
        String commandName = name.toLowerCase();
        
        if (!commands.containsKey(commandName)) {
            // 命令不存在，直接注册
            commands.put(commandName, new HashMap<>());
            commandOwners.put(commandName, new HashSet<>());
        } else if (allowOverride && commands.get(commandName).containsKey(modId)) {
            // 允许覆盖且该模组已注册此命令
            modLoader.getConsole().printWarning("Command /" + commandName + " already registered by " + modId + ", updating");
        } else if (!allowOverride && !commandOwners.get(commandName).isEmpty()) {
            // 不允许覆盖且已有其他模组注册
            modLoader.getConsole().printWarning("Command /" + commandName + " already exists, registration from " + modId + " ignored");
            return false;
        }
        
        commands.get(commandName).put(modId, command);
        commandOwners.get(commandName).add(modId);
        return true;
    }
    
    /**
     * 注册命令（默认允许覆盖）
     */
    public void registerCommand(String name, String modId, Command command) {
        registerCommand(name, modId, command, true);
    }

    /**
     * 执行命令
     */
    public void executeCommand(String input) {
        // 解析命令
        String[] parts = parseCommand(input);
        if (parts.length == 0) {
            return;
        }
        
        String commandName = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        
        // 检查是否是斜杠命令
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
            
            // 解析 --模组名 后缀
            String targetModId = "system"; // 默认执行系统命令
            if (args.contains(" --")) {
                int index = args.lastIndexOf(" --");
                String potentialModId = args.substring(index + 4).trim();
                if (!potentialModId.isEmpty()) {
                    targetModId = potentialModId;
                    args = args.substring(0, index).trim();
                }
            }
            
            // 查找命令
            Map<String, Command> modCommands = commands.get(commandName.toLowerCase());
            if (modCommands != null) {
                Command command = modCommands.get(targetModId);
                if (command != null) {
                    // 指定的模组实现存在
                    try {
                        command.execute(modLoader, args);
                    } catch (Throwable e) {
                        modLoader.getConsole().printLine("Command error: " + e.getMessage());
                    }
                } else if (targetModId.equals("system") && modCommands.size() > 0) {
                    // 默认查找 system 但没有，使用第一个实现
                    command = modCommands.values().iterator().next();
                    try {
                        command.execute(modLoader, args);
                    } catch (Throwable e) {
                        modLoader.getConsole().printLine("Command error: " + e.getMessage());
                    }
                } else if (modCommands.containsKey("system")) {
                    // 没有指定模组或指定的模组不存在，但有 system 实现，优先使用 system
                    command = modCommands.get("system");
                    try {
                        command.execute(modLoader, args);
                    } catch (Throwable e) {
                        modLoader.getConsole().printLine("Command error: " + e.getMessage());
                    }
                } else if (modCommands.size() == 1) {
                    // 只有一个实现，直接使用
                    command = modCommands.values().iterator().next();
                    try {
                        command.execute(modLoader, args);
                    } catch (Throwable e) {
                        modLoader.getConsole().printLine("Command error: " + e.getMessage());
                    }
                } else {
                    // 多个实现但没有指定模组且没有 system 实现
                    modLoader.getConsole().printError("Multiple implementations of /" + commandName + " exist");
                    modLoader.getConsole().printLine("Available from: " + String.join(", ", modCommands.keySet()));
                    modLoader.getConsole().printLine("Use: /" + commandName + " args --ModId");
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
            // 非命令输入，发送到 Hub.ALL 广播域
            modLoader.getBroadcastManager().broadcast("Hub.ALL", input, "Console");
        }
    }
    
    /**
     * 解析命令（支持引号参数）
     * 例如：/date now "maomao.txt" --Maomao
     */
    private String[] parseCommand(String input) {
        // 简单分割，后续可以扩展为支持引号的复杂解析
        return input.split("\\s+", 2);
    }

    /**
     * 获取命令列表（返回所有命令的第一个实现）
     */
    public Map<String, Command> getCommands() {
        Map<String, Command> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Command>> entry : commands.entrySet()) {
            // 优先返回 system 的实现
            Map<String, Command> modCommands = entry.getValue();
            if (modCommands.containsKey("system")) {
                result.put(entry.getKey(), modCommands.get("system"));
            } else if (!modCommands.isEmpty()) {
                result.put(entry.getKey(), modCommands.values().iterator().next());
            }
        }
        return result;
    }
    
    /**
     * 获取命令的所有实现
     */
    public Map<String, Set<String>> getCommandOwners() {
        return new HashMap<>(commandOwners);
    }
}
