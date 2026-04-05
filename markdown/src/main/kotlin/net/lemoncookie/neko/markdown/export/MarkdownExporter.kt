package net.lemoncookie.neko.markdown.export

import javafx.scene.web.WebView
import org.w3c.dom.Document
import java.io.File
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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
     */
    fun exportToPdf(outputPath: String): Boolean {
        return try {
            val file = File(outputPath)
            
            // 使用 WebView 的打印功能
            val job = webView.engine.createPrintJob()
            job?.pageLayout ?: return false
            
            // 设置打印参数
            val pageLayout = job.pageLayout
            pageLayout.orientation = javafx.print.PageOrientation.PORTRAIT
            pageLayout.paper = javafx.print.Paper.A4
            
            // 执行打印到文件
            job.jobAttributes.outputType = javafx.print.PrintJob.JobType.POSTSCRIPT
            job.jobSettings.outputOptions = javafx.print.OutputType(file.toPath())
            
            job.beginJob()
            webView.engine.executeScript("window.print()")
            job.endJob()
            
            true
        } catch (e: Exception) {
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
                    /* Table of Contents */
                    .toc {
                        background-color: var(--code-bg);
                        padding: 16px;
                        border-radius: 6px;
                        margin-bottom: 24px;
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
            </head>
            <body>
                $bodyContent
            </body>
            </html>
        """.trimIndent()
    }
}
