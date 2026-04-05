package net.lemoncookie.neko.markdown.config

import java.io.File
import java.nio.file.Files
import java.util.Properties

/**
 * Markdown 模块配置管理
 * 
 * 支持用户自定义选项：
 * - 语法高亮开关
 * - 数学公式支持开关
 * - 自动生成目录开关
 * - 相对图片路径解析开关
 * - 主题选择
 */
class MarkdownConfig {
    
    private val properties = Properties()
    private var configDir: File? = null
    private var configFile: File? = null
    
    // 配置项默认值
    var syntaxHighlightEnabled: Boolean = true
    var mathSupportEnabled: Boolean = true
    var autoTocEnabled: Boolean = true
    var imageRelativePathEnabled: Boolean = true
    var theme: Theme = Theme.SYSTEM
    
    enum class Theme {
        LIGHT, DARK, SYSTEM
    }
    
    /**
     * 加载配置文件
     * @param baseDir 基础目录（通常是模组数据目录）
     */
    fun load(baseDir: File) {
        configDir = File(baseDir, "markdown")
        if (!configDir!!.exists()) {
            configDir!!.mkdirs()
        }
        
        configFile = File(configDir, "config.properties")
        
        if (configFile!!.exists()) {
            try {
                Files.newInputStream(configFile!!.toPath()).use { input ->
                    properties.load(input)
                    syntaxHighlightEnabled = properties.getProperty("syntax.highlight", "true").toBoolean()
                    mathSupportEnabled = properties.getProperty("math.support", "true").toBoolean()
                    autoTocEnabled = properties.getProperty("auto.toc", "true").toBoolean()
                    imageRelativePathEnabled = properties.getProperty("image.relative.path", "true").toBoolean()
                    theme = Theme.valueOf(properties.getProperty("theme", "SYSTEM"))
                }
            } catch (e: Exception) {
                // 使用默认值
                save()
            }
        } else {
            save()
        }
    }
    
    /**
     * 保存配置到文件
     */
    fun save() {
        if (configFile == null) return
        
        properties.setProperty("syntax.highlight", syntaxHighlightEnabled.toString())
        properties.setProperty("math.support", mathSupportEnabled.toString())
        properties.setProperty("auto.toc", autoTocEnabled.toString())
        properties.setProperty("image.relative.path", imageRelativePathEnabled.toString())
        properties.setProperty("theme", theme.name)
        
        Files.newOutputStream(configFile!!.toPath()).use { output ->
            properties.store(output, "Markdown Module Configuration")
        }
    }
    
    /**
     * 更新配置并保存
     */
    fun update(
        syntaxHighlight: Boolean? = null,
        mathSupport: Boolean? = null,
        autoToc: Boolean? = null,
        imageRelativePath: Boolean? = null,
        theme: Theme? = null
    ) {
        syntaxHighlight?.let { syntaxHighlightEnabled = it }
        mathSupport?.let { mathSupportEnabled = it }
        autoToc?.let { autoTocEnabled = it }
        imageRelativePath?.let { imageRelativePathEnabled = it }
        theme?.let { this.theme = it }
        save()
    }
}
