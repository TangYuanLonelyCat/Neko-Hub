package net.lemoncookie.neko.modloader.console;

import net.lemoncookie.neko.modloader.ModLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 控制台类
 * 负责显示信息和处理用户输入
 */
public class Console {

    private final ModLoader modLoader;
    private final BufferedReader reader;
    private final PrintStream out;
    
    // 输入框可见性控制 - 使用 AtomicBoolean 保证原子操作
    private final AtomicBoolean inputEnabled = new AtomicBoolean(true);
    // 输入控制锁 - 用于 wait/notify 机制
    private final Object inputLock = new Object();
    
    // ANSI 颜色代码
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_WHITE = "\u001B[37m";

    /**
     * 构造函数
     */
    public Console(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.out = System.out;
        
        // 设置控制台编码为 UTF-8 以支持中文
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Throwable e) {
            // 忽略编码设置错误
        }
    }

    /**
     * 打印一行文本
     */
    public void printLine(String text) {
        out.println(text);
    }

    /**
     * 打印空行
     */
    public void printLine() {
        out.println();
    }

    /**
     * 打印文本（不换行）
     */
    public void print(String text) {
        out.print(text);
        out.flush();
    }

    /**
     * 打印错误信息（红色）
     */
    public void printError(String text) {
        out.println(ANSI_RED + text + ANSI_RESET);
    }

    /**
     * 打印警告信息（黄色）
     */
    public void printWarning(String text) {
        out.println(ANSI_YELLOW + text + ANSI_RESET);
    }

    /**
     * 打印成功信息（绿色）
     */
    public void printSuccess(String text) {
        out.println(ANSI_GREEN + text + ANSI_RESET);
    }

    /**
     * 打印信息（蓝色）
     */
    public void printInfo(String text) {
        out.println(ANSI_BLUE + text + ANSI_RESET);
    }

    /**
     * 打印信息（青色）
     */
    public void printCyan(String text) {
        out.println(ANSI_CYAN + text + ANSI_RESET);
    }

    /**
     * 打印信息（紫色）
     */
    public void printMagenta(String text) {
        out.println(ANSI_MAGENTA + text + ANSI_RESET);
    }

    /**
     * 打印信息（白色）
     */
    public void printWhite(String text) {
        out.println(ANSI_WHITE + text + ANSI_RESET);
    }

    /**
     * 启动交互式控制台
     * 使用守护线程，永不退出（除非 JVM 关闭）
     * 当输入禁用时，完全跳过输入处理，不提供任何输入机制
     */
    public void startInteractive() {
        Thread consoleThread = new Thread(() -> {
            try {
                // 主循环 - 使用 while(true) 确保永不退出
                while (true) {
                    try {
                        // 检查输入是否启用 - 禁用时使用 wait/notify 机制高效等待
                        while (!inputEnabled.get()) {
                            // 禁用输入时，使用 wait() 高效等待，不占用 CPU
                            synchronized (inputLock) {
                                try {
                                    // 添加超时机制，避免永久等待（最长 1 秒）
                                    inputLock.wait(1000);
                                } catch (InterruptedException e) {
                                    // 被中断，恢复中断状态
                                    Thread.currentThread().interrupt();
                                    // 中断后直接继续循环，重新检查 inputEnabled
                                }
                            }
                            // 超时或中断后，重新检查 inputEnabled 状态
                        }
                        
                        // 仅在启用输入时显示提示符
                        String username = System.getProperty("user.name", "User");
                        print("[" + username + "]>");
                        
                        // 读取用户输入（阻塞式，直到用户输入或 EOF）
                        String input = reader.readLine();
                        if (input == null) {
                            // EOF reached，记录日志但不退出
                            modLoader.getBroadcastManager().broadcast(
                                "Hub.Log",
                                "[WARNING] Console input stream reached EOF",
                                "Console"
                            );
                            continue;
                        }
                        
                        // 处理输入
                        handleInput(input.trim());
                        
                    } catch (Throwable t) {
                        // 捕获所有未处理的异常，确保控制台线程永不退出
                        String errorMsg = "Unexpected error in console input loop: " + 
                                         (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
                        printError(errorMsg);
                        modLoader.getBroadcastManager().broadcast(
                            "Hub.Log",
                            "[ERROR] " + errorMsg,
                            "Console"
                        );
                        // 不退出，继续下一轮循环
                    }
                }
            } catch (Throwable t) {
                // 最外层异常捕获（理论上不会到达这里）
                String errorMsg = "Critical error in console thread: " + 
                                 (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
                printError(errorMsg);
                modLoader.getBroadcastManager().broadcast(
                    "Hub.Log",
                    "[CRITICAL] " + errorMsg,
                    "Console"
                );
                // 即使发生严重错误也不退出，继续尝试
            }
        });
        
        consoleThread.setName("Console-Input");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }
    
    /**
     * 处理用户输入
     */
    private void handleInput(String input) {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        try {
            // 检查是否是命令（以 '/' 开头）
            if (input.startsWith("/")) {
                // 解析命令并发送到 Hub.Command 广播域
                sendCommand(input.substring(1));
            } else {
                // 普通消息发送到 Hub.Console
                modLoader.getBroadcastManager().broadcast("Hub.Console", input, "Console");
            }
        } catch (Exception e) {
            printError(modLoader.getLanguageManager().getMessage("console.error.processing_error", e.getMessage()));
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] Error processing input '" + input + "': " + e.getMessage(), "Console");
        }
    }
    
    /**
     * 解析命令并发送到 Hub.Command 广播域（公共访问，供 BootFileManager 使用）
     * 格式：/command part1 part2 part3 ...
     * 发送到 Hub.Command 的 JSON:
     * {
     *     "command": "command",
     *     "parts": ["part1", "part2", "part3"],
     *     "sender": "Console"
     * }
     */
    public void handleCommand(String commandInput) {
        if (commandInput == null || commandInput.trim().isEmpty()) {
            return;
        }
        
        try {
            sendCommand(commandInput);
        } catch (Throwable e) {
            printError(modLoader.getLanguageManager().getMessage("console.error.command_error", e.getMessage()));
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] " + modLoader.getLanguageManager().getMessage("console.error.command_error", e.getMessage()), "Console");
        }
    }
    
    /**
     * 解析命令并发送到 Hub.Command 广播域
     */
    private void sendCommand(String commandInput) {
        if (commandInput == null || commandInput.trim().isEmpty()) {
            return;
        }
        
        try {
            String[] tokens = parseCommandTokens(commandInput);
            if (tokens.length == 0) {
                return;
            }
            
            String commandName = tokens[0];
            String[] parts = tokens.length > 1 ? 
                java.util.Arrays.copyOfRange(tokens, 1, tokens.length) : new String[0];
            
            // 创建命令消息
            net.lemoncookie.neko.modloader.command.CommandMessage commandMessage = 
                new net.lemoncookie.neko.modloader.command.CommandMessage(commandName, parts, "Console");
            
            // 发送到 Hub.Command 广播域
            int result = modLoader.getBroadcastManager().broadcast(
                net.lemoncookie.neko.modloader.broadcast.BroadcastManager.HUB_COMMAND,
                commandMessage.toJson(),
                "Console"
            );
            
            if (result != net.lemoncookie.neko.modloader.broadcast.BroadcastManager.ERROR_SUCCESS) {
                printError(modLoader.getLanguageManager().getMessage("console.error.send_command_failed", result));
            }
        } catch (Throwable e) {
            printError(modLoader.getLanguageManager().getMessage("console.error.send_error", e.getMessage()));
            modLoader.getBroadcastManager().broadcast("Hub.Log", "[ERROR] " + modLoader.getLanguageManager().getMessage("console.error.send_error", e.getMessage()), "Console");
        }
    }
    
    /**
     * 解析命令令牌（支持引号参数）
     */
    private String[] parseCommandTokens(String input) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (!inQuotes && (c == '"' || c == '\'')) {
                inQuotes = true;
                quoteChar = c;
            } else if (inQuotes && c == quoteChar) {
                inQuotes = false;
            } else if (!inQuotes && Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result.toArray(new String[0]);
    }

    /**
     * 清空控制台
     */
    public void clear() {
        // 不同操作系统的清屏命令
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix-like systems
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            modLoader.getConsole().printWarning(modLoader.getLanguageManager().getMessage("console.warning.clear_failed", e.getMessage()));
        }
        // Fallback: print empty lines
        for (int i = 0; i < 50; i++) {
            printLine();
        }
    }

    /**
     * 读取用户输入
     */
    public String readLine() throws IOException {
        return reader.readLine();
    }

    /**
     * 读取用户确认（Y/N）
     */
    public boolean readConfirmation() throws IOException {
        while (true) {
            print("[Y/N]: ");
            String input = reader.readLine();
            if (input != null) {
                input = input.trim().toUpperCase();
                if (input.equals("Y")) {
                    return true;
                } else if (input.equals("N")) {
                    return false;
                }
            }
            printLine(modLoader.getLanguageManager().getMessage("console.confirm.invalid_input"));
        }
    }

    /**
     * 关闭控制台
     */
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            // 忽略关闭错误
        }
    }
    
    /**
     * 设置输入框可见性
     * @param enabled true 显示输入框，false 隐藏输入框
     */
    public void setInputEnabled(boolean enabled) {
        boolean oldEnabled = inputEnabled.getAndSet(enabled);
        
        // 记录状态变化到日志
        if (oldEnabled != enabled) {
            String status = enabled ? "enabled" : "disabled";
            String logMessage = "[INFO] Input box " + status + " by command";
            
            // 广播日志消息
            modLoader.getBroadcastManager().broadcast(
                "Hub.Log",
                logMessage,
                "Console"
            );
            
            // 唤醒等待的线程（如果状态从禁用变为启用）
            if (enabled) {
                synchronized (inputLock) {
                    inputLock.notifyAll();
                }
            }
        }
    }
    
    /**
     * 获取输入框可见性状态
     * @return true 表示输入框已启用，false 表示已禁用
     */
    public boolean isInputEnabled() {
        return inputEnabled.get();
    }
}
