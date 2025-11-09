package dev.ktml

import java.net.URL
import java.net.URLClassLoader

/**
 * A ClassLoader that loads classes from its own classpath before delegating to the parent.
 * This ensures that recompiled classes are loaded fresh instead of using cached versions from the parent.
 */
class ChildFirstClassLoader(
    urls: Array<URL>,
    parent: ClassLoader
) : URLClassLoader(urls, parent) {

    override fun loadClass(name: String, resolve: Boolean): Class<*> =
        findLoadedClass(name) ?: loadNewClass(name, resolve)

    private fun loadNewClass(name: String, resolve: Boolean): Class<*> = try {
        findClass(name).also {
            if (resolve) resolveClass(it)
        }
    } catch (_: ClassNotFoundException) {
        super.loadClass(name, resolve)
    }
}
