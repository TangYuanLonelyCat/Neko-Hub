package net.lemoncookie.neko.modloader.config;

/**
 * 配置键常量
 * 避免硬编码字符串
 */
public final class ConfigKeys {
    
    /** 配置文件名 */
    public static final String CONFIG_FILE = "neko-hub.config";
    
    /** Boot 文件名配置键 */
    public static final String BOOT_FILE = "bootfile";
    
    /** 模组权限配置键前缀 */
    public static final String MOD_PERMISSION_PREFIX = "modpermission.";
    
    /** 私有构造函数，防止实例化 */
    private ConfigKeys() {
        // 工具类，不应该被实例化
    }
}
