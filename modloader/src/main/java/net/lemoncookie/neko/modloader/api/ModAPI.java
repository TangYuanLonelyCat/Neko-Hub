package net.lemoncookie.neko.modloader.api;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.command.Command;
import net.lemoncookie.neko.modloader.command.CommandSystem;
import net.lemoncookie.neko.modloader.console.Console;

/**
 * 模组 API 工具类
 * 提供便捷的 API 访问方法，简化模组开发
 * 
 * @author LemonCookie
 * @version 1.0.0
 */
public class ModAPI {
    
    private final ModLoader modLoader;
    private final String modId;
    
    /**
     * 构造函数
     * @param modLoader ModLoader 实例
     * @param modId 模组 ID
     */
    public ModAPI(ModLoader modLoader, String modId) {
        this.modLoader = modLoader;
        this.modId = modId;
    }
    
    // ==================== 命令注册 ====================
    
    /**
     * 注册命令
     * @param name 命令名称
     * @param command 命令对象
     * @param allowOverride 是否允许覆盖其他模组的命令
     * @return 是否注册成功
     */
    public boolean registerCommand(String name, Command command, boolean allowOverride) {
        return modLoader.getCommandSystem().registerCommand(name, modId, command, allowOverride);
    }
    
    /**
     * 注册命令（默认允许覆盖）
     * @param name 命令名称
     * @param command 命令对象
     */
    public void registerCommand(String name, Command command) {
        modLoader.getCommandSystem().registerCommand(name, modId, command);
    }
    
    // ==================== 广播系统 ====================
    
    /**
     * 广播消息到指定域
     * @param domain 域名
     * @param message 消息内容
     */
    public void broadcast(String domain, String message) {
        modLoader.getBroadcastManager().broadcast(domain, message, modId);
    }
    
    /**
     * 广播消息到 Hub.ALL 域
     * @param message 消息内容
     */
    public void broadcastAll(String message) {
        modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_ALL, message, modId);
    }
    
    /**
     * 广播消息到 Hub.Console 域（显示在控制台）
     * @param message 消息内容
     */
    public void broadcastConsole(String message) {
        modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_CONSOLE, message, modId);
    }
    
    /**
     * 广播消息到 Hub.Log 域（仅记录日志）
     * @param message 消息内容
     */
    public void broadcastLog(String message) {
        modLoader.getBroadcastManager().broadcast(BroadcastManager.HUB_LOG, message, modId);
    }
    
    /**
     * 监听指定域的消息
     * @param domain 域名
     * @param listener 监听器
     */
    public void listen(String domain, net.lemoncookie.neko.modloader.broadcast.MessageListener listener) {
        modLoader.getBroadcastManager().listen(domain, listener, modId, getModName());
    }
    
    /**
     * 创建广播域
     * @param name 域名
     * @param isPrivate 是否为私有域
     * @param isPublic 是否为公开域
     * @return 错误码（0 表示成功）
     */
    public int createDomain(String name, boolean isPrivate, boolean isPublic) {
        return modLoader.getBroadcastManager().addDomain(name, isPrivate, isPublic, modId);
    }
    
    /**
     * 创建私有域（仅自己可访问）
     * @param name 域名
     * @return 错误码（0 表示成功）
     */
    public int createPrivateDomain(String name) {
        return createDomain(name, true, false);
    }
    
    /**
     * 创建公开公共域（所有模组可访问）
     * @param name 域名
     * @return 错误码（0 表示成功）
     */
    public int createPublicDomain(String name) {
        return createDomain(name, false, true);
    }
    
    // ==================== 控制台输出 ====================
    
    /**
     * 打印普通消息（白色）
     * @param text 消息内容
     */
    public void print(String text) {
        modLoader.getConsole().printLine(text);
    }
    
    /**
     * 打印错误消息（红色）
     * @param text 消息内容
     */
    public void printError(String text) {
        modLoader.getConsole().printError(text);
    }
    
    /**
     * 打印警告消息（黄色）
     * @param text 消息内容
     */
    public void printWarning(String text) {
        modLoader.getConsole().printWarning(text);
    }
    
    /**
     * 打印成功消息（绿色）
     * @param text 消息内容
     */
    public void printSuccess(String text) {
        modLoader.getConsole().printSuccess(text);
    }
    
    /**
     * 打印信息消息（蓝色）
     * @param text 消息内容
     */
    public void printInfo(String text) {
        modLoader.getConsole().printInfo(text);
    }
    
    // ==================== 快捷方法 ====================
    
    /**
     * 获取模组 ID
     * @return 模组 ID
     */
    public String getModId() {
        return modId;
    }
    
    /**
     * 获取模组名称
     * @return 模组名称
     */
    public String getModName() {
        return modLoader.getJavaMods().stream()
            .filter(mod -> mod.getModId().equals(modId))
            .findFirst()
            .map(IModAPI::getName)
            .orElse(modId);
    }
    
    /**
     * 获取 ModLoader 实例
     * @return ModLoader 实例
     */
    public ModLoader getModLoader() {
        return modLoader;
    }
    
    /**
     * 获取广播管理器
     * @return 广播管理器
     */
    public BroadcastManager getBroadcastManager() {
        return modLoader.getBroadcastManager();
    }
    
    /**
     * 获取命令系统
     * @return 命令系统
     */
    public CommandSystem getCommandSystem() {
        return modLoader.getCommandSystem();
    }
    
    /**
     * 获取控制台
     * @return 控制台
     */
    public Console getConsole() {
        return modLoader.getConsole();
    }
}
