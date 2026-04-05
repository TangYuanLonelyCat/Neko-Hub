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
            <html lang="${modLoader?.languageManager?.getCurrentLanguage() ?: "en"}">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    :root {
                        --bg-primary: ${if (isDark) "#0d1117" else "#ffffff"};
                        --bg-secondary: ${if (isDark) "#161b22" else "#f6f8fa"};
                        --bg-tertiary: ${if (isDark) "#21262d" else "#ddf4ff"};
                        --text-primary: ${if (isDark) "#e6edf3" else "#24292f"};
                        --text-secondary: ${if (isDark) "#8b949e" else "#57606a"};
                        --heading-color: ${if (isDark) "#58a6ff" else "#0969da"};
                        --border-primary: ${if (isDark) "#30363d" else "#d0d7de"};
                        --border-secondary: ${if (isDark) "#21262d" else "#d0d7de"};
                        --link-color: ${if (isDark) "#58a6ff" else "#0969da"};
                        --code-bg: ${if (isDark) "#161b22" else "#f6f8fa"};
                        --accent-green: ${if (isDark) "#3fb950" else "#2da44e"};
                        --accent-red: ${if (isDark) "#f85149" else "#cf222e"};
                        --shadow-sm: 0 1px 2px rgba(0,0,0,0.1);
                        --shadow-md: 0 4px 12px rgba(0,0,0,0.15);
                        --radius-sm: 6px;
                        --radius-md: 8px;
                        --radius-lg: 12px;
                    }
                    
                    * {
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Noto Sans", Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji";
                        line-height: 1.6;
                        padding: 32px;
                        max-width: 960px;
                        margin: 0 auto;
                        color: var(--text-primary);
                        background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
                        min-height: 100vh;
                    }
                    
                    h1, h2, h3, h4, h5, h6 {
                        color: var(--heading-color);
                        margin-top: 32px;
                        margin-bottom: 16px;
                        font-weight: 600;
                        line-height: 1.25;
                        scroll-margin-top: 20px;
                    }
                    
                    h1 { 
                        font-size: 2.5em; 
                        border-bottom: 2px solid var(--border-primary);
                        padding-bottom: 8px;
                        margin-top: 0;
                    }
                    
                    h2 { 
                        font-size: 2em; 
                        border-bottom: 1px solid var(--border-secondary);
                        padding-bottom: 6px;
                    }
                    
                    h3 { font-size: 1.5em; }
                    h4 { font-size: 1.25em; }
                    h5 { font-size: 1em; }
                    h6 { font-size: 0.875em; color: var(--text-secondary); }
                    
                    p {
                        margin-bottom: 16px;
                    }
                    
                    code {
                        background-color: var(--code-bg);
                        padding: 3px 8px;
                        border-radius: var(--radius-sm);
                        font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
                        font-size: 0.9em;
                        border: 1px solid var(--border-secondary);
                    }
                    
                    pre {
                        background-color: var(--code-bg);
                        padding: 20px;
                        border-radius: var(--radius-md);
                        overflow-x: auto;
                        border: 1px solid var(--border-primary);
                        box-shadow: var(--shadow-sm);
                        position: relative;
                    }
                    
                    pre::before {
                        content: "Code";
                        position: absolute;
                        top: 8px;
                        right: 12px;
                        font-size: 0.75em;
                        color: var(--text-secondary);
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    
                    pre code {
                        background-color: transparent;
                        padding: 0;
                        border: none;
                        font-size: 0.9em;
                        line-height: 1.5;
                    }
                    
                    blockquote {
                        border-left: 4px solid var(--heading-color);
                        padding: 12px 20px;
                        margin: 16px 0;
                        background: var(--bg-tertiary);
                        border-radius: 0 var(--radius-md) var(--radius-md) 0;
                        color: var(--text-secondary);
                    }
                    
                    blockquote p:last-child {
                        margin-bottom: 0;
                    }
                    
                    table {
                        border-collapse: collapse;
                        width: 100%;
                        margin: 20px 0;
                        border-radius: var(--radius-md);
                        overflow: hidden;
                        box-shadow: var(--shadow-sm);
                    }
                    
                    th {
                        background: linear-gradient(180deg, var(--bg-tertiary), var(--bg-secondary));
                        font-weight: 600;
                        text-align: left;
                        border: 1px solid var(--border-primary);
                        padding: 12px 16px;
                    }
                    
                    td {
                        border: 1px solid var(--border-secondary);
                        padding: 12px 16px;
                    }
                    
                    tr:nth-child(even) {
                        background-color: var(--bg-secondary);
                    }
                    
                    tr:hover {
                        background-color: var(--bg-tertiary);
                    }
                    
                    a {
                        color: var(--link-color);
                        text-decoration: none;
                        transition: all 0.2s ease;
                        position: relative;
                    }
                    
                    a::after {
                        content: "";
                        position: absolute;
                        bottom: -2px;
                        left: 0;
                        width: 0;
                        height: 1px;
                        background: var(--link-color);
                        transition: width 0.2s ease;
                    }
                    
                    a:hover::after {
                        width: 100%;
                    }
                    
                    a:hover {
                        opacity: 0.8;
                    }
                    
                    img {
                        max-width: 100%;
                        height: auto;
                        border-radius: var(--radius-md);
                        box-shadow: var(--shadow-md);
                        margin: 16px 0;
                    }
                    
                    /* Task list items */
                    ul.contains-task-list {
                        list-style-type: none;
                        padding-left: 8px;
                    }
                    
                    .task-list-item {
                        margin: 8px 0;
                        display: flex;
                        align-items: center;
                        gap: 10px;
                    }
                    
                    .task-list-item input[type="checkbox"] {
                        width: 18px;
                        height: 18px;
                        cursor: pointer;
                        accent-color: var(--accent-green);
                    }
                    
                    .task-list-item.checked {
                        opacity: 0.7;
                    }
                    
                    .task-list-item.checked label {
                        text-decoration: line-through;
                        color: var(--text-secondary);
                    }
                    
                    /* Table of Contents - Enhanced */
                    .toc {
                        background: linear-gradient(135deg, var(--bg-secondary), var(--bg-primary));
                        padding: 24px;
                        border-radius: var(--radius-lg);
                        margin-bottom: 32px;
                        border: 1px solid var(--border-primary);
                        box-shadow: var(--shadow-md);
                    }
                    
                    .toc-title {
                        margin-top: 0;
                        margin-bottom: 16px;
                        font-size: 1.2em;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }
                    
                    .toc-title::before {
                        content: "📑";
                        font-size: 1.3em;
                    }
                    
                    .toc-list {
                        list-style-type: none;
                        padding-left: 0;
                        margin: 0;
                    }
                    
                    .toc-list .toc-list {
                        padding-left: 20px;
                        margin-top: 8px;
                        border-left: 2px solid var(--border-secondary);
                    }
                    
                    .toc-item {
                        margin: 6px 0;
                        transition: all 0.2s ease;
                    }
                    
                    .toc-item:hover {
                        transform: translateX(4px);
                    }
                    
                    .toc-link {
                        color: var(--text-primary);
                        padding: 6px 12px;
                        border-radius: var(--radius-sm);
                        display: block;
                        transition: all 0.2s ease;
                    }
                    
                    .toc-link:hover {
                        background-color: var(--bg-tertiary);
                        color: var(--heading-color);
                        text-decoration: none;
                    }
                    
                    .toc-link::after {
                        display: none;
                    }
                    
                    /* Strikethrough */
                    del {
                        color: var(--text-secondary);
                        text-decoration-color: var(--accent-red);
                        text-decoration-thickness: 2px;
                    }
                    
                    /* Math formulas */
                    .katex-display {
                        overflow-x: auto;
                        overflow-y: hidden;
                        padding: 16px;
                        background: var(--code-bg);
                        border-radius: var(--radius-md);
                        border: 1px solid var(--border-secondary);
                    }
                    
                    /* Horizontal rule */
                    hr {
                        border: none;
                        border-top: 2px solid var(--border-primary);
                        margin: 32px 0;
                    }
                    
                    /* Scrollbar styling */
                    ::-webkit-scrollbar {
                        width: 10px;
                        height: 10px;
                    }
                    
                    ::-webkit-scrollbar-track {
                        background: var(--bg-secondary);
                        border-radius: var(--radius-sm);
                    }
                    
                    ::-webkit-scrollbar-thumb {
                        background: var(--border-primary);
                        border-radius: var(--radius-sm);
                    }
                    
                    ::-webkit-scrollbar-thumb:hover {
                        background: var(--text-secondary);
                    }
                    
                    /* Selection */
                    ::selection {
                        background: var(--link-color);
                        color: var(--bg-primary);
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
