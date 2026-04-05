package net.lemoncookie.neko.markdown.javafx

import javafx.scene.web.WebView
import javafx.scene.Scene
import javafx.scene.layout.VBox
import net.lemoncookie.neko.markdown.Markdown
import net.lemoncookie.neko.modloader.ModLoader

/**
 * JavaFX Markdown 渲染器
 * 
 * 使用 JavaFX WebView 组件渲染 Markdown 为 HTML
 * 提供可视化的 Markdown 预览功能
 */
class MarkdownRenderer(private val markdown: Markdown, private val modLoader: ModLoader) {
    
    private var webView: WebView? = null
    
    /**
     * 创建 WebView 组件用于渲染 Markdown
     * @param initialMarkdown 初始 Markdown 内容（可选）
     * @return WebView 组件
     */
    fun createWebView(initialMarkdown: String? = null): WebView {
        webView = WebView()
        
        val htmlContent = if (initialMarkdown != null) {
            wrapHtml(markdown.parse(initialMarkdown))
        } else {
            wrapHtml("<h1>${modLoader.languageManager.getMessage("markdown.ui.preview_title")}</h1><p>${modLoader.languageManager.getMessage("markdown.ui.preview_instruction")}</p>")
        }
        
        webView?.engine?.loadContent(htmlContent)
        return webView!!
    }
    
    /**
     * 更新 WebView 中的 Markdown 内容
     * @param markdownText 新的 Markdown 文本
     */
    fun updateContent(markdownText: String) {
        val htmlContent = wrapHtml(markdown.parse(markdownText))
        webView?.engine?.loadContent(htmlContent)
    }
    
    /**
     * 从文件加载并渲染 Markdown
     * @param filePath Markdown 文件路径
     * @return 是否成功加载
     */
    fun loadFromFile(filePath: String): Boolean {
        val htmlContent = markdown.parseFile(filePath)
        return if (htmlContent != null) {
            webView?.engine?.loadContent(wrapHtml(htmlContent))
            true
        } else {
            false
        }
    }
    
    /**
     * 创建完整的 JavaFX 场景
     * @param width 场景宽度
     * @param height 场景高度
     * @return JavaFX Scene
     */
    fun createScene(width: Double = 800.0, height: Double = 600.0): Scene {
        val root = VBox()
        root.children.add(createWebView())
        return Scene(root, width, height)
    }
    
    /**
     * 获取 WebView 组件
     */
    fun getWebView(): WebView? = webView
    
    /**
     * 包装 HTML 内容为完整的 HTML 文档
     */
    private fun wrapHtml(bodyContent: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        line-height: 1.6;
                        padding: 20px;
                        max-width: 900px;
                        margin: 0 auto;
                        color: #333;
                    }
                    h1, h2, h3, h4, h5, h6 {
                        color: #2c3e50;
                        margin-top: 24px;
                        margin-bottom: 16px;
                    }
                    code {
                        background-color: #f4f4f4;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-family: "Courier New", Courier, monospace;
                    }
                    pre {
                        background-color: #f4f4f4;
                        padding: 16px;
                        border-radius: 6px;
                        overflow-x: auto;
                    }
                    blockquote {
                        border-left: 4px solid #ddd;
                        padding-left: 16px;
                        color: #666;
                        margin-left: 0;
                    }
                    table {
                        border-collapse: collapse;
                        width: 100%;
                        margin: 16px 0;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 8px 12px;
                        text-align: left;
                    }
                    th {
                        background-color: #f4f4f4;
                        font-weight: bold;
                    }
                    a {
                        color: #3498db;
                        text-decoration: none;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    img {
                        max-width: 100%;
                        height: auto;
                    }
                </style>
            </head>
            <body>
                $bodyContent
            </body>
            </html>
        """.trimIndent()
    }
}
