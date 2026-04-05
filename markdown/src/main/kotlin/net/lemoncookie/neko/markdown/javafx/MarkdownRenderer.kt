package net.lemoncookie.neko.markdown.javafx

import javafx.scene.web.WebView
import javafx.scene.Scene
import javafx.scene.layout.VBox
import net.lemoncookie.neko.markdown.Markdown
import net.lemoncookie.neko.modloader.ModLoader
import net.lemoncookie.neko.markdown.export.MarkdownExporter
import java.io.File

/**
 * JavaFX Markdown 渲染器
 * 
 * 使用 JavaFX WebView 组件渲染 Markdown 为 HTML
 * 提供可视化的 Markdown 预览功能
 * 
 * 支持功能：
 * - GitHub Flavored Markdown (GFM)
 * - 代码语法高亮（Highlight.js）
 * - 数学公式（KaTeX）
 * - 目录生成 (TOC)
 * - 主题切换（浅色/深色）
 * - 图片相对路径解析
 * - 导出为 HTML/PDF
 */
class MarkdownRenderer(
    private val markdown: Markdown, 
    private val modLoader: ModLoader,
    private val basePath: String? = null
) {
    
    private var webView: WebView? = null
    private var currentTheme: String = "light"
    private var currentMarkdown: String = ""
    
    /**
     * 创建 WebView 组件用于渲染 Markdown
     * @param initialMarkdown 初始 Markdown 内容（可选）
     * @return WebView 组件
     */
    fun createWebView(initialMarkdown: String? = null): WebView {
        webView = WebView()
        
        // 启用 JavaScript
        webView?.engine?.isJavaScriptEnabled = true
        
        val htmlContent = if (initialMarkdown != null) {
            currentMarkdown = initialMarkdown
            renderMarkdown(initialMarkdown)
        } else {
            val placeholder = "<h1>${modLoader.languageManager.getMessage("markdown.ui.preview_title")}</h1><p>${modLoader.languageManager.getMessage("markdown.ui.preview_instruction")}</p>"
            renderHtml(placeholder)
        }
        
        webView?.engine?.loadContent(htmlContent)
        return webView!!
    }
    
    /**
     * 更新 WebView 中的 Markdown 内容
     * @param markdownText 新的 Markdown 文本
     */
    fun updateContent(markdownText: String) {
        currentMarkdown = markdownText
        val htmlContent = renderMarkdown(markdownText)
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
            currentMarkdown = htmlContent
            webView?.engine?.loadContent(renderHtml(htmlContent, File(filePath).parent))
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
     * 切换主题
     * @param theme 主题名称：light, dark, system
     */
    fun setTheme(theme: String) {
        currentTheme = when (theme.lowercase()) {
            "dark" -> "dark"
            "system" -> {
                // 检测系统主题（简化实现，默认浅色）
                "light"
            }
            else -> "light"
        }
        
        // 重新渲染当前内容
        if (currentMarkdown.isNotEmpty()) {
            updateContent(currentMarkdown)
        }
    }
    
    /**
     * 获取导出器
     */
    fun getExporter(): MarkdownExporter? {
        return webView?.let { MarkdownExporter(it) }
    }
    
    /**
     * 导出为 HTML 文件
     * @param outputPath 输出路径
     * @return 是否成功
     */
    fun exportToHtml(outputPath: String): Boolean {
        val exporter = getExporter() ?: return false
        val fullHtml = generateFullHtml()
        return exporter.exportToHtml(fullHtml, outputPath)
    }
    
    /**
     * 导出为 PDF 文件
     * @param outputPath 输出路径
     * @return 是否成功
     */
    fun exportToPdf(outputPath: String): Boolean {
        val exporter = getExporter() ?: return false
        return exporter.exportToPdf(outputPath)
    }
    
    /**
     * 渲染 Markdown 为完整 HTML
     */
    private fun renderMarkdown(markdownText: String): String {
        val htmlBody = markdown.parse(markdownText)
        val withToc = markdown.generateWithToc(htmlBody, "", markdown.config.autoTocEnabled)
        return renderHtml(withToc, basePath?.let { File(it).parent })
    }
    
    /**
     * 包装 HTML 内容为完整的 HTML 文档
     */
    private fun renderHtml(bodyContent: String, imageBasePath: String? = null): String {
        val config = markdown.config
        val isDark = currentTheme == "dark"
        
        val highlightJs = if (config.syntaxHighlightEnabled) """
            <script src="https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/lib/highlight.min.js"></script>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/${if (isDark) "github-dark" else "github"}.min.css">
            <script>hljs.highlightAll();</script>
        """ else ""
        
        val katexSupport = if (config.mathSupportEnabled) """
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js" onload="renderMathInElement(document.body);"></script>
        """ else ""
        
        val imagePathScript = if (imageBasePath != null && config.imageRelativePathEnabled) """
            <script>
                window.markdownBasePath = "$imageBasePath";
                
                // 处理相对路径图片
                document.addEventListener('DOMContentLoaded', function() {
                    const images = document.querySelectorAll('img');
                    images.forEach(img => {
                        const src = img.getAttribute('src');
                        if (src && !src.startsWith('http') && !src.startsWith('/')) {
                            img.setAttribute('src', 'file://' + window.markdownBasePath + '/' + src);
                        }
                    });
                });
            </script>
        """ else ""
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    :root {
                        --bg-color: ${if (isDark) "#1e1e1e" else "#ffffff"};
                        --text-color: ${if (isDark) "#d4d4d4" else "#333333"};
                        --heading-color: ${if (isDark) "#569cd6" else "#2c3e50"};
                        --code-bg: ${if (isDark) "#2d2d2d" else "#f4f4f4"};
                        --border-color: ${if (isDark) "#404040" else "#dddddd"};
                        --link-color: ${if (isDark) "#569cd6" else "#3498db"};
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        line-height: 1.6;
                        padding: 20px;
                        max-width: 900px;
                        margin: 0 auto;
                        color: var(--text-color);
                        background-color: var(--bg-color);
                    }
                    h1, h2, h3, h4, h5, h6 {
                        color: var(--heading-color);
                        margin-top: 24px;
                        margin-bottom: 16px;
                    }
                    code {
                        background-color: var(--code-bg);
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-family: "Courier New", Courier, monospace;
                    }
                    pre {
                        background-color: var(--code-bg);
                        padding: 16px;
                        border-radius: 6px;
                        overflow-x: auto;
                    }
                    pre code {
                        background-color: transparent;
                        padding: 0;
                    }
                    blockquote {
                        border-left: 4px solid var(--border-color);
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
                        border: 1px solid var(--border-color);
                        padding: 8px 12px;
                        text-align: left;
                    }
                    th {
                        background-color: var(--code-bg);
                        font-weight: bold;
                    }
                    a {
                        color: var(--link-color);
                        text-decoration: none;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    img {
                        max-width: 100%;
                        height: auto;
                    }
                    /* Task list items */
                    ul.contains-task-list {
                        list-style-type: none;
                        padding-left: 0;
                    }
                    .task-list-item {
                        margin: 4px 0;
                    }
                    .task-list-item input[type="checkbox"] {
                        margin-right: 8px;
                    }
                    /* Table of Contents */
                    .toc {
                        background-color: var(--code-bg);
                        padding: 16px;
                        border-radius: 6px;
                        margin-bottom: 24px;
                    }
                    .toc h3 {
                        margin-top: 0;
                    }
                    .toc ul {
                        list-style-type: none;
                        padding-left: 20px;
                    }
                    .toc > ul {
                        padding-left: 0;
                    }
                    /* Strikethrough */
                    del {
                        color: #888;
                    }
                    /* Math formulas */
                    .katex-display {
                        overflow-x: auto;
                        overflow-y: hidden;
                    }
                </style>
                $highlightJs
                $katexSupport
                $imagePathScript
            </head>
            <body>
                $bodyContent
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * 生成完整的 HTML 文档（用于导出）
     */
    fun generateFullHtml(): String {
        val htmlBody = markdown.parse(currentMarkdown)
        val withToc = markdown.generateWithToc(htmlBody, "", markdown.config.autoTocEnabled)
        return renderHtml(withToc, basePath?.let { File(it).parent })
    }
}
