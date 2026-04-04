package net.lemoncookie.neko.modloader.broadcast;

import net.lemoncookie.neko.modloader.ModLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 广播域管理器
 */
public class BroadcastManager {

    private final ModLoader modLoader;
    private final Map<String, BroadcastDomain> domains;
    private final PermissionManager permissionManager;
    private final Map<String, Map<String, Boolean>> domainPermissions; // 存储模组对特定域的权限

    // 系统广播域
    public static final String HUB_ALL = "Hub.ALL";
    public static final String HUB_SYSTEM = "Hub.System";
    public static final String HUB_CONSOLE = "Hub.Console";
    public static final String HUB_LOG = "Hub.Log";  // 日志专用域（公开公共域）

    // 错误码
    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_PERMISSION_DENIED = 502;
    public static final int ERROR_DOMAIN_NOT_FOUND = 404;
    public static final int ERROR_DOMAIN_EXISTS = 402;

    /**
     * 构造函数
     */
    public BroadcastManager(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.domains = new HashMap<>();
        this.permissionManager = new PermissionManager();
        this.domainPermissions = new HashMap<>();
        
        // 初始化系统广播域
        initializeSystemDomains();
    }

    /**
     * 初始化系统广播域
     */
    private void initializeSystemDomains() {
        // 公开公共域
        addDomain(HUB_ALL, false, true, "system");
        // Hub.System 不再由广播系统自动创建，由控制台模组创建（视为公开私有域）
    }

    /**
     * 添加广播域
     */
    public int addDomain(String name, boolean isPrivate, boolean isPublic, String ownerModId) {
        if (domains.containsKey(name)) {
            return ERROR_DOMAIN_EXISTS; // 广播域已存在
        }
        
        // 检查权限
        ModPermission modPermission = permissionManager.getModPermission(ownerModId);
        if (modPermission.getLevel() >= 3) {
            return ERROR_PERMISSION_DENIED; // 权限不足
        }
        
        BroadcastDomain domain = new BroadcastDomain(name, isPrivate, isPublic, ownerModId);
        domains.put(name, domain);
        
        // 初始化域权限
        domainPermissions.put(name, new HashMap<>());
        // 所有者默认拥有权限
        domainPermissions.get(name).put(ownerModId, true);
        
        return ERROR_SUCCESS;
    }

    /**
     * 获取广播域
     */
    public BroadcastDomain getDomain(String name) {
        return domains.get(name);
    }

    /**
     * 移除广播域
     */
    public int removeDomain(String name, String modId) {
        BroadcastDomain domain = domains.get(name);
        if (domain == null) {
            return ERROR_DOMAIN_NOT_FOUND;
        }
        
        // 只有所有者可以删除广播域
        if (!domain.getOwnerModId().equals(modId)) {
            return ERROR_PERMISSION_DENIED;
        }
        
        domains.remove(name);
        domainPermissions.remove(name);
        return ERROR_SUCCESS;
    }

    /**
     * 向广播域发送消息
     */
    public int broadcast(String domainName, String message, String senderModId) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            return ERROR_DOMAIN_NOT_FOUND;
        }
        
        // 检查权限
        if (!hasPermissionToAccessDomain(domainName, senderModId)) {
            return ERROR_PERMISSION_DENIED;
        }
        
        domain.broadcast(message, senderModId);
        return ERROR_SUCCESS;
    }

    /**
     * 监听广播域
     */
    public int listen(String domainName, MessageListener listener, String modId, String modName) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            return ERROR_DOMAIN_NOT_FOUND;
        }
        
        // 检查权限
        if (!hasPermissionToAccessDomain(domainName, modId)) {
            return ERROR_PERMISSION_DENIED;
        }
        
        domain.addListener(listener, modId);
        return ERROR_SUCCESS;
    }

    /**
     * 监听私有广播域
     */
    public int listenPrivate(String modId, MessageListener listener) {
        // 私有域格式：Hub.[modId]
        String domainName = "Hub." + modId;
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            // 创建私有域
            int result = addDomain(domainName, true, false, modId);
            if (result != ERROR_SUCCESS) {
                return result;
            }
            domain = domains.get(domainName);
        }
        
        domain.addListener(listener, modId);
        return ERROR_SUCCESS;
    }

    /**
     * 尝试获取域权限
     */
    public int requestDomainPermission(String domainName, String modId, String modName) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            return ERROR_DOMAIN_NOT_FOUND;
        }
        
        // Hub.System 和公开私有域都需要用户确认
        if (HUB_SYSTEM.equals(domainName) || (domain.isPrivate() && domain.isPublic())) {
            try {
                if (HUB_SYSTEM.equals(domainName)) {
                    modLoader.getConsole().printWarning(
                        modLoader.getLanguageManager().getMessage("broadcast.confirm.system", modName)
                    );
                } else {
                    modLoader.getConsole().printWarning(
                        modLoader.getLanguageManager().getMessage("broadcast.confirm.private", modName, domainName)
                    );
                }
                boolean confirmed = modLoader.getConsole().readConfirmation();
                if (!confirmed) {
                    return ERROR_PERMISSION_DENIED;
                }
            } catch (Throwable e) {
                return ERROR_PERMISSION_DENIED;
            }
        }
        
        // 授予权限
        domainPermissions.computeIfAbsent(domainName, k -> new HashMap<>()).put(modId, true);
        return ERROR_SUCCESS;
    }

    /**
     * 尝试提权
     */
    public int requestPermissionUpgrade(String modId, String modName, int targetLevel) {
        // 向下提权不需要确认
        ModPermission currentPermission = permissionManager.getModPermission(modId);
        if (targetLevel >= currentPermission.getLevel()) {
            permissionManager.setModPermission(modId, ModPermission.fromLevel(targetLevel));
            return ERROR_SUCCESS;
        }
        
        // 向上提权需要用户确认
        try {
            modLoader.getConsole().printWarning(
                modLoader.getLanguageManager().getMessage("broadcast.confirm.upgrade", modName, 
                    ModPermission.fromLevel(targetLevel).getDisplayName())
            );
            boolean confirmed = modLoader.getConsole().readConfirmation();
            if (!confirmed) {
                return ERROR_PERMISSION_DENIED;
            }
        } catch (Throwable e) {
            return ERROR_PERMISSION_DENIED;
        }
        
        permissionManager.setModPermission(modId, ModPermission.fromLevel(targetLevel));
        return ERROR_SUCCESS;
    }

    /**
     * 检查模组是否有权限访问域
     * 
     * 权限规则：
     * 1. 超级管理员（level 0）：所有权限
     * 2. 系统级组件（level 1）：可访问公共域和系统域
     * 3. 正常组件（level 2）：只能访问公共域
     * 4. 限权组件（level 3）：只能监听，不能发送
     */
    private boolean hasPermissionToAccessDomain(String domainName, String modId) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            return false;
        }
        
        ModPermission modPermission = permissionManager.getModPermission(modId);
        int level = modPermission.getLevel();
        
        // 规则 1：超级管理员拥有所有权限
        if (level == 0) {
            return true;
        }
        
        // 规则 4：限权组件只能监听，不能发送
        if (level == 3) {
            return false;
        }
        
        // 检查是否是域所有者（所有者始终可以访问自己的域）
        if (domain.getOwnerModId().equals(modId)) {
            return true;
        }
        
        // 规则 2：系统级组件可访问公共域和系统域
        if (level == 1) {
            // 系统域需要额外检查是否已授权
            if (HUB_SYSTEM.equals(domainName)) {
                Map<String, Boolean> permissions = domainPermissions.get(domainName);
                return permissions != null && permissions.getOrDefault(modId, false);
            }
            // 公共域（包括公开公共域和普通公共域）
            return !domain.isPrivate();
        }
        
        // 规则 3：正常组件只能访问公共域
        if (level == 2) {
            // 只能访问公开公共域（Hub.ALL）
            return !domain.isPrivate() && domain.isPublic();
        }
        
        return false;
    }

    /**
     * 获取所有广播域
     */
    public Map<String, BroadcastDomain> getDomains() {
        return new HashMap<>(domains);
    }

    /**
     * 获取广播域数量
     */
    public int getDomainCount() {
        return domains.size();
    }

    /**
     * 检查广播域是否存在
     */
    public boolean hasDomain(String name) {
        return domains.containsKey(name);
    }

    /**
     * 获取权限管理器
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * 创建系统域（由控制台模组调用）
     */
    public int createSystemDomain(String ownerModId) {
        // Hub.System 作为公开私有域创建
        return addDomain(HUB_SYSTEM, true, true, ownerModId);
    }

    /**
     * 创建控制台域（公开公共域）
     */
    public int createConsoleDomain(String ownerModId) {
        return addDomain("Hub.Console", false, true, ownerModId);
    }
    
    /**
     * 移除监听器
     * 
     * @param domainName 域名
     * @param listener 监听器
     * @return 错误码（0 表示成功）
     */
    public int unlisten(String domainName, MessageListener listener) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            return ERROR_DOMAIN_NOT_FOUND;
        }
        
        domain.removeListener(listener);
        return ERROR_SUCCESS;
    }
}
