package net.lemoncookie.neko.todolist

class TodoList {
    private val tasks = mutableListOf<String>()

    fun addTask(task: String) {
        tasks.add(task)
        println("Added task: $task")
    }

    fun getTasks(): List<String> = tasks.toList()
}
