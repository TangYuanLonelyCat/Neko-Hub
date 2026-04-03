package net.lemoncookie.neko.modloader.broadcast;

import java.util.HashMap;
import java.util.Map;

/**
 * 模组权限管理器
 */
public class PermissionManager {
    private final Map<String, ModPermission> modPermissions = new HashMap<>();
    
    /**
     * 获取模组权限
     */
    public ModPermission getModPermission(String modId) {
        return modPermissions.getOrDefault(modId, ModPermission.NORMAL_COMPONENT);
    }
    
    /**
     * 设置模组权限
     */
    public void setModPermission(String modId, ModPermission permission) {
        modPermissions.put(modId, permission);
    }
    
    /**
     * 检查模组是否有权限执行操作
     */
    public boolean hasPermission(String modId, ModPermission requiredPermission) {
        ModPermission modPermission = getModPermission(modId);
        return modPermission.getLevel() <= requiredPermission.getLevel();
    }
    
    /**
     * 检查模组是否有足够的权限等级
     */
    public boolean hasLevelPermission(String modId, int requiredLevel) {
        ModPermission modPermission = getModPermission(modId);
        return modPermission.getLevel() <= requiredLevel;
    }
}