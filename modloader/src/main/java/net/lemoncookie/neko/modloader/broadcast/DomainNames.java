package net.lemoncookie.neko.modloader.broadcast;

/**
 * 广播域名称常量
 * 避免硬编码字符串
 */
public final class DomainNames {
    
    /** 公开公共域 - 所有模组可访问 */
    public static final String HUB_ALL = "Hub.ALL";
    
    /** 系统域 - 系统级组件可访问 */
    public static final String HUB_SYSTEM = "Hub.System";
    
    /** 控制台域 - 控制台消息显示 */
    public static final String HUB_CONSOLE = "Hub.Console";
    
    /** 私有构造函数，防止实例化 */
    private DomainNames() {
        // 工具类，不应该被实例化
    }
}
