package net.lemoncookie.neko.modloader.lang;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 语言管理器
 */
public class LanguageManager {

    private static final String DEFAULT_LANG = "en";
    private final Map<String, String> messages;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     */
    public LanguageManager() {
        this.messages = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        loadLanguage(DEFAULT_LANG);
    }

    /**
     * 加载语言
     */
    public void loadLanguage(String lang) {
        try (InputStream is = getClass().getResourceAsStream("/lang/" + lang + ".json")) {
            if (is != null) {
                Map<String, String> langMessages = objectMapper.readValue(is, Map.class);
                messages.clear();
                messages.putAll(langMessages);
            } else {
                // 语言文件不存在，加载默认语言
                loadLanguage(DEFAULT_LANG);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 加载失败，使用默认语言
            loadLanguage(DEFAULT_LANG);
        }
    }

    /**
     * 获取消息
     */
    public String getMessage(String key, Object... args) {
        String message = messages.get(key);
        if (message == null) {
            return key;
        }
        if (args.length > 0) {
            return String.format(message, args);
        }
        return message;
    }

    /**
     * 检查消息是否存在
     */
    public boolean hasMessage(String key) {
        return messages.containsKey(key);
    }
}
