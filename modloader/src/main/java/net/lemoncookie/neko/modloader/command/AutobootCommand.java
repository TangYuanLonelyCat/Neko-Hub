package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 自动启动命令
 * 扫描 mods 文件夹并生成 auto.boot 文件
 */
public class AutobootCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        try {
            // 生成 auto.boot 文件
            modLoader.getBootFileManager().generateAutoBoot();
        } catch (Exception e) {
            modLoader.getConsole().printError("Failed to generate auto.boot: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "扫描 mods 文件夹并生成 auto.boot 文件";
    }

    @Override
    public String getUsage() {
        return "/autoboot";
    }
}
