package net.lemoncookie.neko.modloader.api

interface ModAPI {
    fun onLoad()
    fun onUnload()
}
