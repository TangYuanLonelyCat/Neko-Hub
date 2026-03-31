package net.lemoncookie.neko.bookkeeping

class Bookkeeping {
    fun recordTransaction(amount: Double, description: String) {
        println("Recorded: $description - $$amount")
    }
}
