package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 退出命令
 * 用于优雅地关闭 ModLoader
 */
public class ExitCommand implements Command {

    @Override
    public void execute(ModLoader modLoader, String args) {
        modLoader.getConsole().printInfo("Shutting down Neko-Hub...");
        
        // 通过广播域发送退出消息
        modLoader.getBroadcastManager().broadcast("Hub.Console", "[SYSTEM] Neko-Hub is shutting down...", "ExitCommand");
        
        // 卸载所有模组
        modLoader.unloadAll();
        
        // 关闭日志系统
        modLoader.getSimpleLogger().close();
        
        // 关闭配置管理器
        modLoader.getConfigManager().shutdown();
        
        // 关闭控制台
        modLoader.getConsole().close();
        
        // 退出程序
        System.exit(0);
    }

    @Override
    public String getDescription() {
        return "优雅地关闭 Neko-Hub";
    }

    @Override
    public String getUsage() {
        return "/exit";
    }
}
