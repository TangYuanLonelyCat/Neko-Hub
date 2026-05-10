package net.lemoncookie.neko.modloader.core;

import net.lemoncookie.neko.modloader.api.IModAPI;
import net.lemoncookie.neko.modloader.api.KModAPI;
import net.lemoncookie.neko.modloader.broadcast.BroadcastManager;
import net.lemoncookie.neko.modloader.console.Console;
import net.lemoncookie.neko.modloader.lang.LanguageManager;

import java.util.List;

/**
 * ModCore 配置类
 * 封装 ModCore 所需的所有依赖组件
 */
public class ModCoreConfig {
    
    private final Console console;
    private final LanguageManager languageManager;
    private final BroadcastManager broadcastManager;
    private final String loaderVersion;
    private final String minApiVersion;
    private final List<IModAPI> javaMods;
    private final List<KModAPI> kotlinMods;

    private ModCoreConfig(Builder builder) {
        this.console = builder.console;
        this.languageManager = builder.languageManager;
        this.broadcastManager = builder.broadcastManager;
        this.loaderVersion = builder.loaderVersion;
        this.minApiVersion = builder.minApiVersion;
        this.javaMods = builder.javaMods;
        this.kotlinMods = builder.kotlinMods;
    }

    // Getters
    public Console getConsole() {
        return console;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

    public String getLoaderVersion() {
        return loaderVersion;
    }

    public String getMinApiVersion() {
        return minApiVersion;
    }

    public List<IModAPI> getJavaMods() {
        return javaMods;
    }

    public List<KModAPI> getKotlinMods() {
        return kotlinMods;
    }

    /**
     * Builder 模式构建器
     */
    public static class Builder {
        private Console console;
        private LanguageManager languageManager;
        private BroadcastManager broadcastManager;
        private String loaderVersion;
        private String minApiVersion;
        private List<IModAPI> javaMods;
        private List<KModAPI> kotlinMods;

        public Builder console(Console console) {
            this.console = console;
            return this;
        }

        public Builder languageManager(LanguageManager languageManager) {
            this.languageManager = languageManager;
            return this;
        }

        public Builder broadcastManager(BroadcastManager broadcastManager) {
            this.broadcastManager = broadcastManager;
            return this;
        }

        public Builder loaderVersion(String loaderVersion) {
            this.loaderVersion = loaderVersion;
            return this;
        }

        public Builder minApiVersion(String minApiVersion) {
            this.minApiVersion = minApiVersion;
            return this;
        }

        public Builder javaMods(List<IModAPI> javaMods) {
            this.javaMods = javaMods;
            return this;
        }

        public Builder kotlinMods(List<KModAPI> kotlinMods) {
            this.kotlinMods = kotlinMods;
            return this;
        }

        public ModCoreConfig build() {
            // 验证必填参数
            if (console == null) {
                throw new IllegalArgumentException("Console cannot be null");
            }
            if (languageManager == null) {
                throw new IllegalArgumentException("LanguageManager cannot be null");
            }
            if (broadcastManager == null) {
                throw new IllegalArgumentException("BroadcastManager cannot be null");
            }
            if (loaderVersion == null || minApiVersion == null) {
                throw new IllegalArgumentException("Version parameters cannot be null");
            }
            if (javaMods == null) {
                throw new IllegalArgumentException("javaMods list cannot be null");
            }
            if (kotlinMods == null) {
                throw new IllegalArgumentException("kotlinMods list cannot be null");
            }
            return new ModCoreConfig(this);
        }
    }
}
