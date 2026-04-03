package net.lemoncookie.neko.modloader.lib

class ModLibrary {
    private val registry = mutableMapOf<String, Any>()

    fun register(name: String, component: Any) {
        registry[name] = component
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(name: String): T? = registry[name] as? T

    operator fun contains(name: String): Boolean = name in registry

    fun getRegisteredNames(): Set<String> = registry.keys.toSet()

    inline fun <reified T> register(name: String, noinline init: () -> T): T {
        val component = init()
        register(name, component)
        return component
    }

    fun registerAll(vararg pairs: Pair<String, Any>) {
        pairs.forEach { (name, component) ->
            register(name, component)
        }
    }
}

inline fun modLibrary(block: ModLibrary.() -> Unit): ModLibrary {
    return ModLibrary().apply(block)
}
