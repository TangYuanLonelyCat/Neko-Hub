package net.lemoncookie.neko.modloader.config;

import net.lemoncookie.neko.modloader.ModLoader;
import net.lemoncookie.neko.modloader.broadcast.ModPermission;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置管理器
 * 负责配置的读取和保存
 */
public class ConfigManager {
    
    private final ModLoader modLoader;
    private final Properties config;
    private final File configFile;
    
    public ConfigManager(ModLoader modLoader) {
        this.modLoader = modLoader;
        this.config = new Properties();
        this.configFile = new File("neko-hub.config");
        
        // 加载配置
        loadConfig();
    }
    
    /**
     * 加载配置
     */
    private void loadConfig() {
        if (configFile.exists()) {
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                config.load(reader);
            } catch (IOException e) {
                modLoader.getConsole().printWarning("Failed to load config: " + e.getMessage());
            }
        }
    }
    
    /**
     * 保存配置
     */
    private void saveConfig() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            config.store(writer, "Neko-Hub Configuration");
        } catch (IOException e) {
            modLoader.getConsole().printError("Failed to save config: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置项
     */
    public String getConfig(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    /**
     * 设置配置项
     */
    public void setConfig(String key, String value) {
        config.setProperty(key, value);
        saveConfig();
    }
    
    /**
     * 获取 boot 文件名
     */
    public String getBootFile() {
        return getConfig("bootfile", "auto.boot");
    }
    
    /**
     * 设置 boot 文件名
     */
    public void setBootFile(String fileName) {
        setConfig("bootfile", fileName);
    }
    
    /**
     * 获取模组权限等级
     */
    public ModPermission getModPermission(String modId) {
        String levelStr = getConfig("modpermission." + modId, null);
        if (levelStr != null) {
            try {
                int level = Integer.parseInt(levelStr);
                return ModPermission.fromLevel(level);
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }
        }
        return ModPermission.NORMAL_COMPONENT; // 默认返回正常组件
    }
    
    /**
     * 设置模组权限等级
     */
    public void setModPermission(String modId, int level) {
        setConfig("modpermission." + modId, String.valueOf(level));
    }
    
    /**
     * 获取所有模组权限配置
     */
    public Map<String, Integer> getAllModPermissions() {
        Map<String, Integer> permissions = new HashMap<>();
        for (String key : config.stringPropertyNames()) {
            if (key.startsWith("modpermission.")) {
                String modId = key.substring("modpermission.".length());
                try {
                    int level = Integer.parseInt(config.getProperty(key));
                    permissions.put(modId, level);
                } catch (NumberFormatException e) {
                    // 忽略格式错误
                }
            }
        }
        return permissions;
    }
}
