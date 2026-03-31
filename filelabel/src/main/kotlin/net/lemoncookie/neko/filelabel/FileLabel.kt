package net.lemoncookie.neko.filelabel

class FileLabel {
    fun labelFile(filePath: String, labels: List<String>) {
        println("Labeling $filePath with: ${labels.joinToString(", ")}")
    }
}
