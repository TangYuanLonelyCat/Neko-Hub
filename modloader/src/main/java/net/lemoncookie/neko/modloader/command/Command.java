package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;

/**
 * 命令接口
 */
public interface Command {

    /**
     * 执行命令
     */
    void execute(ModLoader modLoader, String args) throws Exception;

    /**
     * 获取命令描述
     */
    String getDescription();

    /**
     * 获取命令用法
     */
    String getUsage();
}
