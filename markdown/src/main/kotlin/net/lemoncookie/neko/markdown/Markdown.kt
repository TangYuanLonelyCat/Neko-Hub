package net.lemoncookie.neko.markdown

import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.IModAPI
import net.lemoncookie.neko.markdown.config.MarkdownConfig
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.*
import org.commonmark.ext.gfm.strikethrough.*
import org.commonmark.ext.task.list.items.*
import java.io.File
import java.nio.file.Files

/**
 * Markdown 模块 - 支持 Markdown 解析和 JavaFX 渲染
 * 
 * 功能：
 * - 解析 Markdown 文本为 HTML（支持 GFM）
 * - 使用 JavaFX WebView 渲染 Markdown
 * - 支持从文件读取 Markdown 内容
 * - 支持 GitHub Flavored Markdown (GFM)：任务列表、删除线、表格
 * - 支持代码语法高亮（Highlight.js）
 * - 支持数学公式（KaTeX/LaTeX）
 * - 支持目录生成 (TOC)
 * - 支持主题切换（浅色/深色模式）
 * - 支持图片相对路径解析
 * - 支持导出为 HTML/PDF
 * - 支持配置系统
 */
class Markdown : IModAPI {
    
    private var modLoader: ModLoader? = null
    private var modId: String? = null
    
    /**
     * 配置管理器
     */
    val config = MarkdownConfig()
    
    /**
     * 解析 Markdown 文本为 HTML
     * @param markdown Markdown 源文本
     * @return 渲染后的 HTML 字符串
     */
    fun parse(markdown: String): String {
        return try {
            // 构建支持 GFM 的解析器
            val parser: Parser = Parser.builder()
                .extensions(listOf(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    TaskListItemsExtension.create()
                ))
                .build()
            
            val document: Node = parser.parse(markdown)
            
            // 构建支持 GFM 的 HTML 渲染器
            val renderer: HtmlRenderer = HtmlRenderer.builder()
                .extensions(listOf(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    TaskListItemsExtension.create()
                ))
                .build()
            
            renderer.render(document)
        } catch (e: Exception) {
            modLoader?.console?.printError(
                modLoader?.languageManager?.getMessage("markdown.error.parse_failed", e.message) 
                    ?: "Markdown parse failed: ${e.message}"
            )
            "<html><body>${modLoader?.languageManager?.getMessage("markdown.error.display") ?: "Error parsing markdown"}</body></html>"
        }
    }
    
    /**
     * 从文件读取并解析 Markdown
     * @param filePath Markdown 文件路径
     * @return 渲染后的 HTML 字符串，失败返回 null
     */
    fun parseFile(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                modLoader?.console?.printError(
                    modLoader?.languageManager?.getMessage("markdown.error.file_not_found", filePath)
                        ?: "Markdown file not found: $filePath"
                )
                return null
            }
            val markdown = Files.readString(file.toPath())
            parse(markdown)
        } catch (e: Exception) {
            modLoader?.console?.printError(
                modLoader?.languageManager?.getMessage("markdown.error.io_error", e.message)
                    ?: "IO error reading Markdown file: ${e.message}"
            )
            null
        }
    }
    
    /**
     * 从 HTML 内容生成带目录的完整文档
     * @param htmlContent HTML 正文内容
     * @param title 文档标题
     * @param generateToc 是否生成目录
     * @return 带目录的 HTML 文档
     */
    fun generateWithToc(htmlContent: String, title: String = "", generateToc: Boolean = true): String {
        if (!generateToc || !config.autoTocEnabled) {
            return htmlContent
        }
        
        // 简单实现：提取 h1-h6 标签生成目录
        val tocEntries = mutableListOf<Pair<String, String>>()
        val headingRegex = Regex("<h([1-6])[^>]*>(.*?)</h[1-6]>")
        
        for (match in headingRegex.findAll(htmlContent)) {
            val level = match.groupValues[1].toInt()
            val text = match.groupValues[2].replace(Regex("<[^>]*>"), "") // 移除 HTML 标签
            val id = text.lowercase().replace(Regex("[^a-z0-9]"), "-")
            tocEntries.add(Pair(text, "#$id"))
        }
        
        if (tocEntries.isEmpty()) {
            return htmlContent
        }
        
        // 生成目录 HTML
        val tocHtml = buildString {
            append("<div class=\"toc\">")
            append("<h3>${modLoader?.languageManager?.getMessage("markdown.toc.title") ?: "Table of Contents"}</h3>")
            append("<ul>")
            
            var lastLevel = 0
            for ((text, href) in tocEntries) {
                val level = tocEntries.indexOf(Pair(text, href)).let { idx ->
                    headingRegex.find(htmlContent, headingRegex.find(htmlContent)?.range?.last ?: 0)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                }
                
                // 简化处理，假设按顺序出现
                append("<li><a href=\"$href\">$text</a></li>")
            }
            
            append("</ul>")
            append("</div>")
        }
        
        return tocHtml + htmlContent
    }
    
    override fun getModId(): String {
        return modId ?: "markdown"
    }
    
    override fun getVersion(): String {
        return "2.0.0"
    }
    
    override fun getPackageName(): String {
        return "net.lemoncookie.neko.markdown"
    }
    
    override fun getName(): String {
        return "Markdown Module"
    }
    
    override fun onLoad(modLoader: ModLoader, modId: String) {
        this.modLoader = modLoader
        this.modId = modId
        
        // 加载配置
        val dataDir = File(System.getProperty("user.home"), ".neko-hub")
        config.load(dataDir)
        
        modLoader.console.printInfo(
            modLoader.languageManager.getMessage("markdown.info.initialized")
        )
    }
    
    override fun onUnload() {
        // 保存配置
        config.save()
        
        modLoader?.console?.printInfo(
            modLoader?.languageManager?.getMessage("markdown.info.unloaded")
                ?: "Markdown module unloaded"
        )
        modLoader = null
        modId = null
    }
}
