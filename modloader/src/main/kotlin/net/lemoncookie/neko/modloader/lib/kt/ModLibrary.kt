package net.lemoncookie.neko.modloader.lib.kt

/**
 * Kotlin版模组库支持类
 * 提供Kotlin友好的DSL风格API
 */
class ModLibrary {
    private val registry = mutableMapOf<String, Any>()

    /**
     * 注册库组件
     */
    fun register(name: String, component: Any) {
        registry[name] = component
        println("[ModLibrary-KT] Registered: $name")
    }

    /**
     * 获取库组件
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(name: String): T? = registry[name] as? T

    /**
     * 检查组件是否存在
     */
    operator fun contains(name: String): Boolean = name in registry

    /**
     * 获取所有已注册组件名称
     */
    fun getRegisteredNames(): Set<String> = registry.keys.toSet()

    /**
     * DSL风格的注册函数
     */
    inline fun <reified T> register(name: String, noinline init: () -> T): T {
        val component = init()
        register(name, component)
        return component
    }

    /**
     * 批量注册
     */
    fun registerAll(vararg pairs: Pair<String, Any>) {
        pairs.forEach { (name, component) ->
            register(name, component)
        }
    }
}

/**
 * 创建ModLibrary的DSL函数
 */
inline fun modLibrary(block: ModLibrary.() -> Unit): ModLibrary {
    return ModLibrary().apply(block)
}
