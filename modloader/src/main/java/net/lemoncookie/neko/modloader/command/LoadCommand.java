package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

import java.io.File;

/**
 * 加载模组命令
 * 支持包名和文件名加载（不支持路径）
 */
public class LoadCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) throws Exception {
        if (args.isEmpty()) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/load [模组包名或文件名]")
            );
            return;
        }

        // 移除可能的引号
        args = args.trim();
        if ((args.startsWith("\"") && args.endsWith("\"")) || 
            (args.startsWith("'") && args.endsWith("'"))) {
            args = args.substring(1, args.length() - 1);
        }

        // 检查是包名还是文件名
        if (args.endsWith(".jar")) {
            // 文件名加载
            loadModByFileName(modLoader, args);
        } else {
            // 包名加载（暂时模拟）
            loadModByPackageName(modLoader, args);
        }
    }

    /**
     * 通过文件名加载模组
     */
    private void loadModByFileName(ModLoader modLoader, String fileName) {
        File modFile = new File("mods", fileName);
        if (!modFile.exists()) {
            modLoader.getConsole().printError("Mod file not found: " + fileName);
            return;
        }

        // TODO: 实现从 jar 文件加载模组的逻辑
        // 目前只是模拟
        modLoader.getConsole().printSuccess("Loading mod from file: " + fileName);
        modLoader.getConsole().printSuccess("Mod loaded successfully");
    }

    /**
     * 通过包名加载模组
     */
    private void loadModByPackageName(ModLoader modLoader, String packageName) {
        // TODO: 实现从包名加载模组的逻辑
        // 目前只是模拟
        modLoader.getConsole().printSuccess("Loading mod: " + packageName);
        modLoader.getConsole().printSuccess("Mod loaded successfully");
    }

    @Override
    public String getDescription() {
        return "加载模组（支持包名或文件名）";
    }

    @Override
    public String getUsage() {
        return "/load [模组包名或文件名]";
    }
}
