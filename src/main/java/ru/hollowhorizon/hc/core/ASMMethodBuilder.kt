package ru.hollowhorizon.hc.core


object ASMMethodBuilder {
    val LOADER = ASMClassLoader()


    class ASMClassLoader() : ClassLoader() {
        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            return Class.forName(name, resolve, Thread.currentThread().contextClassLoader)
        }

        fun define(name: String, data: ByteArray): Class<*> {
            return defineClass(name, data, 0, data.size)
        }
    }

}