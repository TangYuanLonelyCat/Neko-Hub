package net.lemoncookie.neko.modloader.broadcast;

/**
 * 消息监听器接口
 */
public interface MessageListener {

    /**
     * 接收到消息时调用
     */
    void onMessageReceived(String domain, String message, String senderModId);

}
