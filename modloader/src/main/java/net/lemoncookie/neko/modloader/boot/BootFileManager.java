package net.lemoncookie.neko.modloader.boot;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.ModDependency;
import net.lemoncookie.neko.modloader.util.VersionComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        File bootFile;
        try {
            File projectRoot = new File(".").getCanonicalFile();
            bootFile = new File(fileName).getCanonicalFile();
            
            // 确保 boot 文件在项目根目录内 - 使用 Path API 进行更严格的路径检查
            String bootFilePath = bootFile.getCanonicalPath();
            String projectRootPath = projectRoot.getCanonicalPath();
            
            // 使用 Path API 进行规范化路径比较
            java.nio.file.Path bootPath = java.nio.file.Paths.get(bootFilePath);
            java.nio.file.Path rootPath = java.nio.file.Paths.get(projectRootPath);
            java.nio.file.Path normalizedBoot = bootPath.normalize();
            java.nio.file.Path normalizedRoot = rootPath.normalize();
            
            if (!normalizedBoot.startsWith(normalizedRoot)) {
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.invalid_path", fileName));
                return null;
            }
        } catch (IOException e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.invalid_path", fileName));
            return null;
        }
        
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
            // 模拟控制台输入，发送到 Hub.Command 广播域
            if (command.startsWith("/")) {
                modLoader.getConsole().handleCommand(command.substring(1));
            } else {
                // 非命令输入发送到 Hub.Console
                modLoader.getBroadcastManager().broadcast("Hub.Console", command, "BootFile");
            }
        }
        
        return true;
    }
    
    /**
     * 切换 boot 文件并立即执行
     */
    public void switchBootFileAndExecute(String fileName) {
        currentBootFile = fileName;
        executeBootFile(fileName);
    }
    
    /**
     * 在 boot 文件开头插入命令
     */
    public void insertCommandAtHead(String command) {
        List<String> commands = readBootFile(currentBootFile);
        if (commands == null) {
            commands = new ArrayList<>();
        }
        
        // 避免重复插入
        if (!commands.contains(command)) {
            commands.add(0, command);
            writeBootFile(commands);
        }
    }
    
    /**
     * 在 boot 文件开头插入命令，并移除同类型的旧命令
     * @param command 新命令
     * @param commandPrefix 要移除的旧命令前缀（如 "/change bootfile"）
     */
    public void insertCommandAtHeadWithReplace(String command, String commandPrefix) {
        List<String> commands = readBootFile(currentBootFile);
        if (commands == null) {
            commands = new ArrayList<>();
        }
        
        // 移除同类型的旧命令
        commands.removeIf(cmd -> cmd.startsWith(commandPrefix));
        
        // 插入新命令到开头
        commands.add(0, command);
        writeBootFile(commands);
    }
    
    /**
     * 在 boot 文件末尾插入命令
     */
    public void insertCommandAtTail(String command) {
        List<String> commands = readBootFile(currentBootFile);
        if (commands == null) {
            commands = new ArrayList<>();
        }
        
        // 避免重复插入
        if (!commands.contains(command)) {
            commands.add(command);
            writeBootFile(commands);
        }
    }
    
    /**
     * 写入 boot 文件
     */
    private void writeBootFile(List<String> commands) {
        File bootFile = new File(currentBootFile);
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(bootFile), StandardCharsets.UTF_8))) {
            
            writer.println("# Auto-generated boot file");
            for (String command : commands) {
                writer.println(command);
            }
        } catch (IOException e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.generate_failed", e.getMessage()));
        }
    }
    
    /**
     * 生成 auto.boot 文件
     */
    public void generateAutoBoot() {
        File modsDir = new File("mods");
        if (!modsDir.exists() || !modsDir.isDirectory()) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.mods_not_found"));
            return;
        }
        
        // 扫描 mods 文件夹下的所有 jar 文件
        File[] modFiles = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (modFiles == null || modFiles.length == 0) {
            modLoader.getConsole().printWarning(modLoader.getLanguageManager().getMessage("boot.warning.no_mods"));
            // 创建空的 auto.boot 文件
            File bootFile = new File("auto.boot");
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(bootFile), StandardCharsets.UTF_8))) {
                writer.println("# Auto-generated boot file");
                writer.println("# No mods found in mods folder");
                modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("boot.success.generated_empty"));
            } catch (IOException e) {
                modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.generate_failed", e.getMessage()));
            }
            return;
        }
        
        // 解析每个模组的依赖信息
        Map<String, ModInfo> modInfoMap = new HashMap<>();
        for (File modFile : modFiles) {
            ModInfo modInfo = extractModInfo(modFile);
            if (modInfo != null) {
                modInfoMap.put(modInfo.modId, modInfo);
            }
        }
        
        // 拓扑排序
        List<String> sortedMods;
        try {
            sortedMods = topologicalSort(modInfoMap);
        } catch (Exception e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.circular_dependency", e.getMessage()));
            return;
        }
        
        // 写入 auto.boot 文件
        File bootFile = new File("auto.boot");
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(bootFile), StandardCharsets.UTF_8))) {
            
            for (String modId : sortedMods) {
                ModInfo modInfo = modInfoMap.get(modId);
                if (modInfo != null) {
                    writer.println("/load " + modInfo.fileName);
                }
            }
            
            modLoader.getConsole().printSuccess(modLoader.getLanguageManager().getMessage("boot.success.generated"));
        } catch (IOException e) {
            modLoader.getConsole().printError(modLoader.getLanguageManager().getMessage("boot.error.generate_failed", e.getMessage()));
        }
    }
    
    /**
     * 从 JAR 文件中提取模组信息
     * 
     * 通过读取 JAR 文件的 MANIFEST.MF 来获取模组信息
     * 期望的 manifest 属性：
     * - Mod-Id: 模组 ID
     * - Mod-Version: 模组版本
     * - Mod-Dependencies: 依赖列表（格式：mod1:1.0.0,mod2:2.0.0）
     */
    private ModInfo extractModInfo(File modFile) {
        try (JarFile jarFile = new JarFile(modFile)) {
            String fileName = modFile.getName();
            
            // 从 manifest 读取模组信息
            var manifest = jarFile.getManifest();
            if (manifest == null) {
                // 没有 manifest，使用文件名作为模组 ID
                String modId = fileName.substring(0, fileName.lastIndexOf(".jar"));
                return new ModInfo(modId, fileName, new ArrayList<>());
            }
            
            String modId = manifest.getMainAttributes().getValue("Mod-Id");
            String modVersion = manifest.getMainAttributes().getValue("Mod-Version");
            String dependenciesStr = manifest.getMainAttributes().getValue("Mod-Dependencies");
            
            // 如果没有指定 Mod-Id，使用文件名
            if (modId == null || modId.trim().isEmpty()) {
                modId = fileName.substring(0, fileName.lastIndexOf(".jar"));
            }
            
            // 解析依赖
            List<ModDependency> dependencies = new ArrayList<>();
            if (dependenciesStr != null && !dependenciesStr.trim().isEmpty()) {
                // 依赖格式：mod1:1.0.0,mod2:2.0.0
                String[] depPairs = dependenciesStr.split(",");
                for (String depPair : depPairs) {
                    depPair = depPair.trim();
                    if (!depPair.isEmpty()) {
                        String[] parts = depPair.split(":");
                        if (parts.length == 2) {
                            String depModId = parts[0].trim();
                            String depMinVersion = parts[1].trim();
                            if (!depModId.isEmpty() && !depMinVersion.isEmpty()) {
                                dependencies.add(new ModDependency(depModId, depMinVersion));
                            }
                        }
                    }
                }
            }
            
            return new ModInfo(modId, fileName, dependencies);
        } catch (Exception e) {
            modLoader.getConsole().printWarning(modLoader.getLanguageManager().getMessage("boot.warning.read_mod_failed", modFile.getName(), e.getMessage()));
            return null;
        }
    }
    
    /**
     * 拓扑排序
     * 
     * @param modInfoMap 模组信息映射表
     * @return 排序后的模组 ID 列表
     * @throws CircularDependencyException 如果存在循环依赖
     */
    private List<String> topologicalSort(Map<String, ModInfo> modInfoMap) throws CircularDependencyException {
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>(); // 正在访问的节点（用于检测循环依赖）
        
        // 使用有序的键列表，确保遍历顺序稳定
        List<String> sortedModIds = new ArrayList<>(modInfoMap.keySet());
        java.util.Collections.sort(sortedModIds); // 按模组 ID 字母顺序排序，确保稳定性
        
        for (String modId : sortedModIds) {
            if (!visited.contains(modId)) {
                visit(modId, modInfoMap, visited, visiting, result);
            }
        }
        
        return result;
    }
    
    /**
     * 访问单个节点（深度优先搜索）
     */
    private void visit(String modId, Map<String, ModInfo> modInfoMap, 
                       Set<String> visited, Set<String> visiting, List<String> result) 
            throws CircularDependencyException {
        
        if (visited.contains(modId)) {
            return; // 已访问过
        }
        
        if (visiting.contains(modId)) {
            // 检测到循环依赖
            throw new CircularDependencyException("Circular dependency detected involving mod: " + modId);
        }
        
        visiting.add(modId); // 标记为正在访问
        
        // 访问依赖的模组
        ModInfo modInfo = modInfoMap.get(modId);
        if (modInfo != null) {
            for (ModDependency dependency : modInfo.dependencies) {
                String depModId = dependency.getModId();
                if (modInfoMap.containsKey(depModId)) {
                    visit(depModId, modInfoMap, visited, visiting, result);
                }
                // 如果依赖的模组不在当前扫描的模组列表中，忽略（可能是外部依赖或已加载的系统模组）
            }
        }
        
        visiting.remove(modId); // 标记为访问完成
        visited.add(modId);
        result.add(modId); // 添加到结果列表
    }
    
    /**
     * 模组信息内部类
     */
    private static class ModInfo {
        String modId;
        String fileName;
        List<ModDependency> dependencies;
        
        ModInfo(String modId, String fileName, List<ModDependency> dependencies) {
            this.modId = modId;
            this.fileName = fileName;
            this.dependencies = dependencies;
        }
    }
    
    /**
     * 循环依赖异常
     */
    private static class CircularDependencyException extends Exception {
        CircularDependencyException(String message) {
            super(message);
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
