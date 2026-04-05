package net.lemoncookie.neko.markdown

import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.modloader.api.IModAPI
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.File
import java.nio.file.Files

/**
 * Markdown 模块 - 支持 Markdown 解析和 JavaFX 渲染
 * 
 * 功能：
 * - 解析 Markdown 文本为 HTML
 * - 使用 JavaFX WebView 渲染 Markdown
 * - 支持从文件读取 Markdown 内容
 */
class Markdown : IModAPI {
    
    private var modLoader: ModLoader? = null
    private var modId: String? = null
    
    /**
     * 解析 Markdown 文本为 HTML
     * @param markdown Markdown 源文本
     * @return 渲染后的 HTML 字符串
     */
    fun parse(markdown: String): String {
        return try {
            val parser: Parser = Parser.builder().build()
            val document: Node = parser.parse(markdown)
            val renderer: HtmlRenderer = HtmlRenderer.builder().build()
            renderer.render(document)
        } catch (e: Exception) {
            modLoader?.console?.printError(
                modLoader?.languageManager?.getMessage("markdown.error.parse_failed", e.message) 
                    ?: "Markdown parse failed: ${e.message}"
            )
            "<html><body>Error parsing markdown</body></html>"
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
    
    override fun getModId(): String {
        return modId ?: "markdown"
    }
    
    override fun getVersion(): String {
        return "1.0.0"
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
        
        modLoader.console.printInfo(
            modLoader.languageManager.getMessage("markdown.info.initialized")
        )
    }
    
    override fun onUnload() {
        modLoader?.console?.printInfo(
            modLoader?.languageManager?.getMessage("markdown.info.unloaded")
                ?: "Markdown module unloaded"
        )
        modLoader = null
        modId = null
    }
}
