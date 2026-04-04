package net.lemoncookie.neko.modloader.broadcast;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 广播域类
 */
public class BroadcastDomain {

    private final String name;
    private final Set<MessageListener> listeners;
    private final boolean isPrivate;
    private final boolean isPublic;
    private final String ownerModId;

    /**
     * 构造函数
     */
    public BroadcastDomain(String name, boolean isPrivate, boolean isPublic, String ownerModId) {
        this.name = name;
        this.listeners = new CopyOnWriteArraySet<>();
        this.isPrivate = isPrivate;
        this.isPublic = isPublic;
        this.ownerModId = ownerModId;
    }

    /**
     * 获取广播域名称
     */
    public String getName() {
        return name;
    }

    /**
     * 检查是否是私有域
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * 检查是否是公开域
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * 获取所有者模组ID
     */
    public String getOwnerModId() {
        return ownerModId;
    }

    /**
     * 添加监听器
     */
    public boolean addListener(MessageListener listener, String modId) {
        // 私有域只能由所有者监听，除非是公开私有域且有权限
        if (isPrivate && !modId.equals(ownerModId)) {
            return false;
        }
        return listeners.add(listener);
    }

    /**
     * 移除监听器
     */
    public boolean removeListener(MessageListener listener) {
        return listeners.remove(listener);
    }

    /**
     * 广播消息
     */
    public void broadcast(String message, String senderModId) {
        for (MessageListener listener : listeners) {
            try {
                listener.onMessageReceived(name, message, senderModId);
            } catch (Throwable e) {
                // 记录监听器错误到日志
                System.err.println("[" + name + "] Error in message listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取监听器数量
     */
    public int getListenerCount() {
        return listeners.size();
    }
}
