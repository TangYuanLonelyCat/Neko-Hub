package net.lemoncookie.neko.modloader.broadcast;

/**
 * 模组权限等级枚举
 */
public enum ModPermission {
    /**
     * 超级管理员 - 拥有所有域的权限
     */
    SUPER_ADMIN(0, "超级管理员"),
    
    /**
     * 系统级组件 - 拥有大多数域的权限
     */
    SYSTEM_COMPONENT(1, "系统级组件"),
    
    /**
     * 正常组件 - 拥有公共域和自身私有域的权限
     */
    NORMAL_COMPONENT(2, "正常组件"),
    
    /**
     * 限权组件 - 仅拥有监听权限
     */
    RESTRICTED_COMPONENT(3, "限权组件");
    
    private final int level;
    private final String displayName;
    
    ModPermission(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    /**
     * 获取权限等级数值
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * 获取权限等级显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 根据等级数值获取权限枚举
     */
    public static ModPermission fromLevel(int level) {
        for (ModPermission permission : values()) {
            if (permission.level == level) {
                return permission;
            }
        }
        return NORMAL_COMPONENT; // 默认返回正常组件
    }
}