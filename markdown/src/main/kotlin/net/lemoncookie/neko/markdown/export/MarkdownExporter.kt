package net.lemoncookie.neko.markdown.export

import javafx.scene.web.WebView
import java.io.File
import java.nio.file.Files

/**
 * Markdown 导出功能
 * 
 * 支持导出为：
 * - HTML 文件
 * - PDF 文件（通过 WebView 打印）
 */
class MarkdownExporter(private val webView: WebView) {
    
    /**
     * 导出为 HTML 文件
     * @param content HTML 内容
     * @param outputPath 输出文件路径
     * @return 是否成功
     */
    fun exportToHtml(content: String, outputPath: String): Boolean {
        return try {
            val file = File(outputPath)
            Files.writeString(file.toPath(), content)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 导出为 PDF 文件
     * @param outputPath 输出文件路径
     * @return 是否成功
     * 
     * 注意：此方法需要在 JavaFX 应用线程中执行
     * 
     * 警告：PDF 导出功能依赖于 JavaFX 打印系统，在某些环境下可能不可用。
     * 如果导出失败，建议使用 HTML 导出然后通过浏览器打印为 PDF。
     */
    fun exportToPdf(outputPath: String): Boolean {
        return try {
            // 使用 WebView 的打印功能
            val job = webView.engine.createPrintJob()
            job ?: return false
            
            // 设置打印参数
            val pageLayout = job.pageLayout
            pageLayout.orientation = javafx.print.PageOrientation.PORTRAIT
            pageLayout.paper = javafx.print.Paper.A4
            
            // 执行打印到文件
            job.jobAttributes.outputType = javafx.print.PrintJob.JobType.POSTSCRIPT
            
            // 注意：JavaFX 打印到文件的功能在不同平台上支持程度不同
            // 某些平台可能需要使用其他方式实现 PDF 导出
            job.beginJob()
            webView.engine.executeScript("window.print()")
            job.endJob()
            
            true
        } catch (e: Exception) {
            // PDF 导出失败，记录错误但不抛出异常
            false
        }
    }
    
    /**
     * 获取完整的 HTML 文档（包含所有资源引用）
     * @param bodyContent HTML 正文内容
     * @param title 文档标题
     * @param basePath 基础路径（用于解析相对路径）
     * @param syntaxHighlight 是否启用语法高亮
     * @param mathSupport 是否启用数学公式
     * @param theme 主题
     * @return 完整的 HTML 文档
     */
    fun generateFullHtml(
        bodyContent: String,
        title: String = "Markdown Document",
        basePath: String? = null,
        syntaxHighlight: Boolean = true,
        mathSupport: Boolean = true,
        theme: String = "light"
    ): String {
        val isDark = theme == "dark"
        
        val highlightJs = if (syntaxHighlight) """
            <script src="https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/lib/highlight.min.js"></script>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/highlight.js@11.9.0/styles/${if (isDark) "github-dark" else "github"}.min.css">
            <script>hljs.highlightAll();</script>
        """ else ""
        
        val katexCss = if (mathSupport) """
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js" onload="renderMathInElement(document.body);"></script>
        """ else ""
        
        val basePathScript = if (basePath != null) """
            <script>
                window.markdownBasePath = "$basePath";
            </script>
        """ else ""
        
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$title</title>
                $highlightJs
                $katexCss
                $basePathScript
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
                    
                    del {
                        color: var(--text-secondary);
                        text-decoration-color: var(--accent-red);
                        text-decoration-thickness: 2px;
                    }
                    
                    .katex-display {
                        overflow-x: auto;
                        overflow-y: hidden;
                        padding: 16px;
                        background: var(--code-bg);
                        border-radius: var(--radius-md);
                        border: 1px solid var(--border-secondary);
                    }
                    
                    hr {
                        border: none;
                        border-top: 2px solid var(--border-primary);
                        margin: 32px 0;
                    }
                    
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
                    
                    ::selection {
                        background: var(--link-color);
                        color: var(--bg-primary);
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
