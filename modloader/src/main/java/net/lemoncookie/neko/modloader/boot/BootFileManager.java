package net.lemoncookie.neko.modloader.boot;

import net.lemoncookie.neko.modloader.ModLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Boot 文件管理器
 * 负责 boot 文件的读取、解析和生成
 */
public class BootFileManager {
    
    private final ModLoader modLoader;
    private String currentBootFile = "auto.boot";
    
    public BootFileManager(ModLoader modLoader) {
        this.modLoader = modLoader;
    }
    
    /**
     * 读取 boot 文件
     * @return 命令列表，如果读取失败返回 null
     */
    public List<String> readBootFile(String fileName) {
        File bootFile = new File(fileName);
        if (!bootFile.exists() || !bootFile.canRead()) {
            return null;
        }
        
        List<String> commands = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(bootFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 跳过空行和注释
                if (!line.isEmpty() && !line.startsWith("#")) {
                    commands.add(line);
                }
            }
        } catch (IOException e) {
            return null;
        }
        
        return commands;
    }
    
    /**
     * 执行 boot 文件
     */
    public boolean executeBootFile(String fileName) {
        List<String> commands = readBootFile(fileName);
        if (commands == null) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("boot.error.not_found")
            );
            return false;
        }
        
        for (String command : commands) {
            modLoader.getCommandSystem().executeCommand(command);
        }
        
        return true;
    }
    
    /**
     * 生成 auto.boot 文件
     */
    public void generateAutoBoot() {
        File modsDir = new File("mods");
        if (!modsDir.exists() || !modsDir.isDirectory()) {
            modLoader.getConsole().printError("mods folder not found");
            return;
        }
        
        File bootFile = new File("auto.boot");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(bootFile), StandardCharsets.UTF_8))) {
            
            // 扫描 mods 文件夹下的所有 jar 文件
            File[] modFiles = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (modFiles != null) {
                for (File modFile : modFiles) {
                    // 使用文件名（不含路径）
                    writer.println("/load " + modFile.getName());
                }
            }
            
            modLoader.getConsole().printSuccess("Generated auto.boot file");
        } catch (IOException e) {
            modLoader.getConsole().printError("Failed to generate auto.boot: " + e.getMessage());
        }
    }
    
    /**
     * 设置当前 boot 文件名
     */
    public void setCurrentBootFile(String fileName) {
        this.currentBootFile = fileName;
    }
    
    /**
     * 获取当前 boot 文件名
     */
    public String getCurrentBootFile() {
        return currentBootFile;
    }
    
    /**
     * 执行当前 boot 文件
     */
    public boolean executeCurrentBootFile() {
        return executeBootFile(currentBootFile);
    }
}
