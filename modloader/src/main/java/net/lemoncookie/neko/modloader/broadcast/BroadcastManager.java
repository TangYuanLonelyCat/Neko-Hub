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

    // 系统广播域
    public static final String HUB_ALL = "Hub.ALL";
    public static final String HUB_SYSTEM = "Hub.System";

    /**
     * 构造函数
     */
    public BroadcastManager(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.domains = new HashMap<>();
        
        // 初始化系统广播域
        initializeSystemDomains();
    }

    /**
     * 初始化系统广播域
     */
    private void initializeSystemDomains() {
        // 公共域
        addDomain(HUB_ALL, false, "system");
        // 系统域
        addDomain(HUB_SYSTEM, false, "system");
    }

    /**
     * 添加广播域
     */
    public boolean addDomain(String name, boolean isPrivate, String ownerModId) {
        if (domains.containsKey(name)) {
            return false; // 广播域已存在
        }
        
        BroadcastDomain domain = new BroadcastDomain(name, isPrivate, ownerModId);
        domains.put(name, domain);
        return true;
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
    public boolean removeDomain(String name, String modId) {
        BroadcastDomain domain = domains.get(name);
        if (domain == null) {
            return false;
        }
        
        // 只有所有者可以删除广播域
        if (!domain.getOwnerModId().equals(modId)) {
            return false;
        }
        
        domains.remove(name);
        return true;
    }

    /**
     * 向广播域发送消息
     */
    public void broadcast(String domainName, String message, String senderModId) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain != null) {
            domain.broadcast(message, senderModId);
        }
    }

    /**
     * 监听广播域
     */
    public boolean listen(String domainName, MessageListener listener, String modId, String modName) {
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            return false;
        }
        
        // 监听 Hub.System 需要用户确认
        if (HUB_SYSTEM.equals(domainName) && !"system".equals(modId)) {
            try {
                modLoader.getConsole().printWarning(
                    modLoader.getLanguageManager().getMessage("broadcast.confirm.system", modName)
                );
                boolean confirmed = modLoader.getConsole().readConfirmation();
                if (!confirmed) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        return domain.addListener(listener, modId);
    }

    /**
     * 监听私有广播域
     */
    public boolean listenPrivate(String modId, MessageListener listener) {
        // 私有域格式：Hub.[modId]
        String domainName = "Hub." + modId;
        BroadcastDomain domain = domains.get(domainName);
        if (domain == null) {
            // 创建私有域
            if (!addDomain(domainName, true, modId)) {
                return false;
            }
            domain = domains.get(domainName);
        }
        
        return domain.addListener(listener, modId);
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
}
