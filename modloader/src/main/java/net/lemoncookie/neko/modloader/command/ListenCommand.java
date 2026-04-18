package net.lemoncookie.neko.modloader.command;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.broadcast.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监听命令
 * 监听/取消监听特定广播域
 * 监听 Hub.Command 广播域
 */
public class ListenCommand extends BaseCommandListener {
    
    private static final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();
    
    public ListenCommand(ModLoader modLoader) {
        super(modLoader, "listen");
    }
    
    @Override
    protected void execute(CommandMessage commandMessage, String senderModId) {
        if (commandMessage.getPartCount() < 2) {
            modLoader.getConsole().printError(
                modLoader.getLanguageManager().getMessage("command.error.args", "/listen [域名] [start|stop]")
            );
            return;
        }

        String domainName = commandMessage.getPart(0).trim();
        String action = commandMessage.getPart(1).trim().toLowerCase();

        switch (action) {
            case "start":
                startListening(modLoader, domainName);
                break;
            case "stop":
                stopListening(modLoader, domainName);
                break;
            default:
                modLoader.getConsole().printError("Unknown action: " + action);
                modLoader.getConsole().printError("Use 'start' to begin listening or 'stop' to stop listening");
        }
    }

    private void startListening(ModLoader modLoader, String domainName) {
        if (listeners.containsKey(domainName)) {
            modLoader.getConsole().printWarning("Already listening to domain: " + domainName);
            return;
        }

        MessageListener listener = new MessageListener() {
            @Override
            public void onMessageReceived(String domain, String message, String senderModId) {
                modLoader.getConsole().printLine("[" + domain + "] (from: " + senderModId + "): " + message);
            }
        };

        int result = modLoader.getBroadcastManager().listen(domainName, listener, "Console", "Console");
        
        if (result == 0) {
            listeners.put(domainName, listener);
            modLoader.getConsole().printSuccess("Started listening to domain: " + domainName);
        } else {
            modLoader.getConsole().printError("Failed to start listening. Error code: " + result);
        }
    }

    private void stopListening(ModLoader modLoader, String domainName) {
        MessageListener listener = listeners.remove(domainName);
        if (listener == null) {
            modLoader.getConsole().printWarning("Not listening to domain: " + domainName);
            return;
        }

        int result = modLoader.getBroadcastManager().unlisten(domainName, listener);
        
        if (result == BroadcastManager.ERROR_SUCCESS) {
            modLoader.getConsole().printSuccess("Stopped listening to domain: " + domainName);
        } else if (result == BroadcastManager.ERROR_DOMAIN_NOT_FOUND) {
            modLoader.getConsole().printError("Domain not found: " + domainName);
        } else {
            modLoader.getConsole().printError("Failed to stop listening. Error code: " + result);
        }
    }
}
