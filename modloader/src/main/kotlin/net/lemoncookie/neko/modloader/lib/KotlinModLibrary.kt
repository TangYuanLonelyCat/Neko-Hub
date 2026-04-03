package net.lemoncookie.neko.modloader.lib

/**
 * Kotlin 版模组库支持类
 * 提供 Kotlin 友好的 DSL 风格 API
 */
class KotlinModLibrary {
    private val registry = mutableMapOf<String, Any>()

    /**
     * 注册库组件
     */
    fun register(name: String, component: Any) {
        registry[name] = component
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
     * DSL 风格的注册函数
     */
    inline fun <reified T> register(name: String, noinline init: () -> T): T {
        val component = init()
        register(name, component as Any)
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
 * 创建 KotlinModLibrary 的 DSL 函数
 */
inline fun kotlinModLibrary(block: KotlinModLibrary.() -> Unit): KotlinModLibrary {
    return KotlinModLibrary().apply(block)
}
