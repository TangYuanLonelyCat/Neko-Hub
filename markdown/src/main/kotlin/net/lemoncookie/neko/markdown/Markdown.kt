package net.lemoncookie.neko.markdown

class Markdown {
    fun parse(markdown: String): String {
        return "<html>$markdown</html>"
    }
}
