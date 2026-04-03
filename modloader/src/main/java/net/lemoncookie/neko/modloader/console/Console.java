package net.lemoncookie.neko.modloader.console;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * 控制台类
 * 负责显示信息和处理用户输入
 */
public class Console {

    private final ModLoader modLoader;
    private final BufferedReader reader;
    private final PrintStream out;

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
        } catch (Exception e) {
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
        // 同时通过广播域发送消息
        sendToConsole("error", text);
    }

    /**
     * 打印警告信息（黄色）
     */
    public void printWarning(String text) {
        out.println(ANSI_YELLOW + text + ANSI_RESET);
        // 同时通过广播域发送消息
        sendToConsole("warning", text);
    }

    /**
     * 打印成功信息（绿色）
     */
    public void printSuccess(String text) {
        out.println(ANSI_GREEN + text + ANSI_RESET);
        // 同时通过广播域发送消息
        sendToConsole("success", text);
    }

    /**
     * 打印信息（蓝色）
     */
    public void printInfo(String text) {
        out.println(ANSI_BLUE + text + ANSI_RESET);
        // 同时通过广播域发送消息
        sendToConsole("info", text);
    }

    /**
     * 打印信息（青色）
     */
    public void printCyan(String text) {
        out.println(ANSI_CYAN + text + ANSI_RESET);
        sendToConsole("cyan", text);
    }

    /**
     * 打印信息（紫色）
     */
    public void printMagenta(String text) {
        out.println(ANSI_MAGENTA + text + ANSI_RESET);
        sendToConsole("magenta", text);
    }

    /**
     * 打印信息（白色）
     */
    public void printWhite(String text) {
        out.println(ANSI_WHITE + text + ANSI_RESET);
        sendToConsole("white", text);
    }

    /**
     * 通过广播域发送消息到控制台
     */
    private void sendToConsole(String type, String message) {
        // 只有在广播域管理器初始化后才发送
        if (modLoader.getBroadcastManager() != null) {
            modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_CONSOLE, message, "system");
        }
    }

    /**
     * 启动交互式控制台
     */
    public void startInteractive() {
        Thread consoleThread = new Thread(() -> {
            try {
                while (true) {
                    // 显示提示符
                    String username = System.getProperty("user.name", "User");
                    print("[" + username + "]>");

                    // 读取用户输入
                    String input = reader.readLine();
                    if (input == null) {
                        break;
                    }

                    // 处理输入
                    handleInput(input.trim());
                }
            } catch (IOException e) {
                printLine("Console error: " + e.getMessage());
            }
        });

        consoleThread.setName("Console-Input");
        consoleThread.start();
    }

    /**
     * 处理用户输入
     */
    private void handleInput(String input) {
        if (input.isEmpty()) {
            return;
        }

        // 交给命令系统处理
        modLoader.getCommandSystem().executeCommand(input);
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
            // 清屏失败时，打印空行
            for (int i = 0; i < 50; i++) {
                printLine();
            }
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
            printLine("Please enter Y or N");
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
}
